package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.PleaValue;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.updatepleas.Plea;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePleaCommandHandlerTest {

    @InjectMocks
    private UpdatePleaCommandHandler hearingCommandHandler;

    @Mock
    private EventStream hearingAggregateEventStream;

    @Mock
    private EventStream offenceAggregateEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(HearingInitiated.class,
            PleaUpsert.class,
            OffencePleaUpdated.class,
            ConvictionDateAdded.class,
            ConvictionDateRemoved.class);

    private static CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testHearingAggregateUpdatePlea_toNotGuilty_shouldNotHaveConvictionDate() throws Throwable {

        PleaValue pleaValue = PleaValue.NOT_GUILTY;
        LocalDate pleaDate = PAST_LOCAL_DATE.next();

        UpdatePleaCommand updatePleaCommand = UpdatePleaCommand.updatePleaCommand()
                .setPleas(Arrays.asList(
                        Plea.plea()
                                .setOriginatingHearingId(hearing.getHearingId())
                                .setPleaDate(pleaDate)
                                .setValue(pleaValue)
                                .setOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                ));

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);
        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"), objectToJsonObjectConverter.convert(updatePleaCommand));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream).collect(Collectors.toList());

        assertThat(events.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(jsonEnvelop)
                                .withName("hearing.hearing-offence-plea-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearing.getHearingId().toString())),
                                withJsonPath("$.offenceId", is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                                withJsonPath("$.pleaDate", is(pleaDate.toString())),
                                withJsonPath("$.value", is(pleaValue.name()))
                        )))
        );

        assertThat(events.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(jsonEnvelop)
                                .withName("hearing.conviction-date-removed"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                                withoutJsonPath("$.convictionDate")
                        )))
        );
    }

    @SuppressWarnings("serial")
    @Test
    public void testHearingAggregateUpdatePlea_toGuilty_shouldHaveConvictionDateDelegated() throws Throwable {


        PleaValue pleaValue = PleaValue.GUILTY;
        LocalDate pleaDate = PAST_LOCAL_DATE.next();

        final DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withUserId(UUID.randomUUID())
                .withFirstName("David")
                .withLastName("Bowie").build();

        final UpdatePleaCommand updatePleaCommand = UpdatePleaCommand.updatePleaCommand().setPleas(
                Arrays.asList(
                        Plea.plea()
                                .setOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                .setOriginatingHearingId(hearing.getHearingId())
                                .setPleaDate(pleaDate)
                                .setDelegatedPowers(delegatedPowers)
                                .setValue(pleaValue)
                )
        );

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);
        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"), objectToJsonObjectConverter.convert(updatePleaCommand));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream).collect(Collectors.toList());

        JsonEnvelope jsonEnvelope;

        jsonEnvelope = events.get(0);
        assertThat(jsonEnvelope.metadata().name(), is("hearing.hearing-offence-plea-updated"));
        assertThat(asPojo(jsonEnvelope, PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId()))
                .with(PleaUpsert::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(PleaUpsert::getPleaDate, is(pleaDate))
                .with(PleaUpsert::getValue, is(pleaValue))
                .with(PleaUpsert::getDelegatedPowers, isBean(DelegatedPowers.class)
                        .with(DelegatedPowers::getFirstName, is(delegatedPowers.getFirstName()))
                        .with(DelegatedPowers::getLastName, is(delegatedPowers.getLastName()))
                        .with(DelegatedPowers::getUserId, is(delegatedPowers.getUserId()))

                )
        );

        jsonEnvelope = events.get(1);
        assertThat(jsonEnvelope.metadata().name(), is("hearing.conviction-date-added"));
        assertThat(asPojo(jsonEnvelope, ConvictionDateAdded.class), isBean(ConvictionDateAdded.class)
                .with(ConvictionDateAdded::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateAdded::getConvictionDate, is(pleaDate))
        );
    }

    @Test
    public void testOffenceAggregateUpdatePlea_toNotGuilty() throws Throwable {

        PleaValue pleaValue = PleaValue.NOT_GUILTY;
        LocalDate pleaDate = PAST_LOCAL_DATE.next();

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);
        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated").build(),
                createObjectBuilder()
                        .add("hearingId", hearing.getHearingId().toString())
                        .add("offenceId", hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())
                        .add("pleaDate", pleaDate.toString())
                        .add("value", pleaValue.name())
                        .build());

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        JsonEnvelope jsonEnvelope;

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());
        jsonEnvelope = events.get(0);
        assertThat(jsonEnvelope.metadata().name(), is("hearing.offence-plea-updated"));
        assertThat(asPojo(jsonEnvelope, PleaUpsert.class),
                isBean(PleaUpsert.class)
                        .with(PleaUpsert::getHearingId, is(hearing.getHearingId()))
                        .with(PleaUpsert::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(PleaUpsert::getPleaDate, is(pleaDate))
                        .with(PleaUpsert::getValue, is(pleaValue))
        );
    }

    @Test
    public void testOffenceAggregateUpdatePleaToGuilty() throws Throwable {

        PleaValue pleaValue = PleaValue.NOT_GUILTY;
        LocalDate pleaDate = PAST_LOCAL_DATE.next();

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);
        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated").build(),
                createObjectBuilder()
                        .add("hearingId", hearing.getHearingId().toString())
                        .add("offenceId", hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())
                        .add("pleaDate", pleaDate.toString())
                        .add("value", pleaValue.name())
                        .build());

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        assertThat(verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(jsonEnvelop)
                                .withName("hearing.offence-plea-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearing.getHearingId().toString())),
                                withJsonPath("$.offenceId", is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                                withJsonPath("$.pleaDate", is(pleaDate.toString())),
                                withJsonPath("$.value", is(pleaValue.name()))
                        )))
        ));
    }
}