package uk.gov.moj.cpp.hearing.command.handler;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
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
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdateOffencePleaCommand;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePleaCommandHandlerTest {

    private static CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingInitiated.class,
            PleaUpsert.class,
            OffencePleaUpdated.class,
            ConvictionDateAdded.class,
            ConvictionDateRemoved.class);
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

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testHearingAggregateUpdatePlea_toNotGuilty_shouldNotHaveConvictionDate() throws Throwable {

        final PleaValue pleaValue = PleaValue.NOT_GUILTY;
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final UpdatePleaCommand updatePleaCommand = UpdatePleaCommand.updatePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleas(asList(
                        Plea.plea()
                                .withOriginatingHearingId(hearing.getHearingId())
                                .withPleaDate(pleaDate)
                                .withPleaValue(pleaValue)
                                .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                .build()));

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};


        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);

        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"), objectToJsonObjectConverter.convert(updatePleaCommand));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream)
                .collect(Collectors.toList());

        assertThat(asPojo((JsonEnvelope) events.get(0), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId()))
                .with(PleaUpsert::getPlea, isBean(Plea.class)
                        .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Plea::getPleaDate, is(pleaDate))
                        .with(Plea::getPleaValue, is(pleaValue))
                ));

        assertThat(((JsonEnvelope) events.get(1)).metadata().name(), is("hearing.conviction-date-removed"));
        assertThat(asPojo((JsonEnvelope) events.get(1), ConvictionDateRemoved.class), isBean(ConvictionDateRemoved.class)
                .with(ConvictionDateRemoved::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
        );
    }

    @SuppressWarnings("serial")
    @Test
    public void testHearingAggregateUpdatePlea_toGuilty_shouldHaveConvictionDateDelegated() throws Throwable {

        final PleaValue pleaValue = PleaValue.GUILTY;
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withUserId(UUID.randomUUID())
                .withFirstName("David")
                .withLastName("Bowie").build();

        final UpdatePleaCommand updatePleaCommand = UpdatePleaCommand.updatePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleas(
                        Arrays.asList(
                                Plea.plea()
                                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                        .withOriginatingHearingId(hearing.getHearingId())
                                        .withPleaDate(pleaDate)
                                        .withDelegatedPowers(delegatedPowers)
                                        .withPleaValue(pleaValue)
                                        .build()
                        )
                );

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);
        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"),
                objectToJsonObjectConverter.convert(updatePleaCommand));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream).collect(Collectors.toList());

        JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.hearing-offence-plea-updated"));

        assertThat(asPojo(events.get(0), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId()))
                .with(PleaUpsert::getPlea, isBean(Plea.class)
                        .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Plea::getPleaDate, is(pleaDate))
                        .with(Plea::getPleaValue, is(pleaValue))
                        .with(Plea::getDelegatedPowers, isBean(DelegatedPowers.class)
                                .with(DelegatedPowers::getFirstName, is(delegatedPowers.getFirstName()))
                                .with(DelegatedPowers::getLastName, is(delegatedPowers.getLastName()))
                                .with(DelegatedPowers::getUserId, is(delegatedPowers.getUserId())))));

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

        final UpdateOffencePleaCommand command = UpdateOffencePleaCommand.
                updateOffencePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPlea(Plea.plea()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withPleaDate(pleaDate)
                        .withPleaValue(pleaValue)
                        .build());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        final JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.offence-plea-updated"));

        assertThat(asPojo(events.get(0), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId()))
                .with(PleaUpsert::getPlea, isBean(Plea.class)
                        .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Plea::getPleaDate, is(pleaDate))
                        .with(Plea::getPleaValue, is(pleaValue))));
    }

    @Test
    public void testOffenceAggregateUpdatePleaToGuilty() throws Throwable {

        final PleaValue pleaValue = PleaValue.NOT_GUILTY;

        final LocalDate pleaDate = PAST_LOCAL_DATE.next();

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);

        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final UpdateOffencePleaCommand command = UpdateOffencePleaCommand.
                updateOffencePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPlea(Plea.plea()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withPleaDate(pleaDate)
                        .withPleaValue(pleaValue)
                        .build());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId()))
                .with(PleaUpsert::getPlea, isBean(Plea.class)
                        .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Plea::getPleaDate, is(pleaDate))
                        .with(Plea::getPleaValue, is(pleaValue))));
    }
}