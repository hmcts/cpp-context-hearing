package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingOffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;

@RunWith(MockitoJUnitRunner.class)
public class NewModelUpdatePleaCommandHandlerTest {

    // InjectMocks 
    @InjectMocks private NewModelUpdatePleaCommandHandler hearingCommandHandler;
    // Mocks
    @Mock private EventStream hearingAggregateEventStream;
    @Mock private EventStream offenceAggregateEventStream;
    @Mock private EventSource eventSource;
    @Mock private AggregateService aggregateService;
    // Spys
    @Spy private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Spy private final Enveloper enveloper = createEnveloperWithEvents(Initiated.class, HearingOffencePleaUpdated.class,
            OffencePleaUpdated.class, ConvictionDateAdded.class, ConvictionDateRemoved.class);

    private static InitiateHearingCommand initiateHearingCommand;
    private static UUID caseId;
    private static UUID hearingId;
    private static UUID defendantId;
    private static UUID personId;
    private static UUID offenceId;
    private static UUID medatadaId;
    private static LocalDate pleaDate;

    private enum PleaValueType {GUILTY, NOT_GUILTY};

    @BeforeClass
    public static void init() {
        initiateHearingCommand = initiateHearingCommandTemplate().build();
        caseId = initiateHearingCommand.getCases().get(0).getCaseId();
        hearingId = initiateHearingCommand.getHearing().getId();
        defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        personId = initiateHearingCommand.getHearing().getDefendants().get(0).getPersonId();
        offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        medatadaId = UUID.randomUUID();
        pleaDate = PAST_LOCAL_DATE.next();
    }
    
    
    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testHearingAggregateUpdatePleaToNotGuilty() throws Throwable {
        assertHearingAggregate(PleaValueType.NOT_GUILTY);
    }
    
    @Test
    public void testHearingAggregateUpdatePleaToGuilty() throws Throwable {
         assertHearingAggregate(PleaValueType.GUILTY);
    }
    
    @Test
    public void testOffenceAggregateUpdatePleaToNotGuilty() throws Throwable {
        assertOffenceAggregate(PleaValueType.NOT_GUILTY);
    }
    
    @Test
    public void testOffenceAggregateUpdatePleaToGuilty() throws Throwable {
         assertOffenceAggregate(PleaValueType.GUILTY);
    }

    private void assertHearingAggregate(final PleaValueType pleaValue) throws Throwable {

        final HearingUpdatePleaCommand updatePleacommand = HearingUpdatePleaCommand.builder()
                .withCaseId(caseId)
                .withHearingId(hearingId)
                .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                        .withId(defendantId)
                        .withPersonId(personId)
                        .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                .withId(offenceId)
                                .withPlea(uk.gov.moj.cpp.hearing.command.plea.Plea.builder()
                                        .withId(randomUUID())
                                        .withPleaDate(pleaDate)
                                        .withValue(pleaValue.name())
                                )
                        )
                )
                .build();
        
        final NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingAggregateEventStream);
        when(this.aggregateService.get(this.hearingAggregateEventStream, NewModelHearingAggregate.class)).thenReturn(newModelHearingAggregate);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataOf(medatadaId, "hearing.update-plea"), objectToJsonObjectConverter.convert(updatePleacommand));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);
        
        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream).collect(Collectors.toList());
        
        assertThat(events.get(0), 
                jsonEnvelope(
                        withMetadataEnvelopedFrom(jsonEnvelop)
                                .withName("hearing.hearing-offence-plea-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.pleaDate", is(pleaDate.toString())),
                                withJsonPath("$.value", is(pleaValue.name()))
                        )))
        );
        
        switch (pleaValue) {
        case GUILTY:
            assertThat(events.get(1),
                    jsonEnvelope(
                            withMetadataEnvelopedFrom(jsonEnvelop)
                                    .withName("hearing.conviction-date-added"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.offenceId", is(offenceId.toString())),
                                    withJsonPath("$.convictionDate", is(pleaDate.toString()))

                            )))
            );
            break;
        case NOT_GUILTY:
            assertThat(events.get(1),
                    jsonEnvelope(
                            withMetadataEnvelopedFrom(jsonEnvelop)
                                    .withName("hearing.conviction-date-removed"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.offenceId", is(offenceId.toString())),
                                    withoutJsonPath("$.convictionDate")
                            )))
            );
            break;
        }
    }

    private void assertOffenceAggregate(final PleaValueType pleaValue) throws EventStreamException {

        when(this.eventSource.getStreamById(offenceId)).thenReturn(this.offenceAggregateEventStream);
        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());
        
        final JsonEnvelope jsonEnvelop = JsonEnvelopeBuilder.envelopeFrom(
                metadataOf(medatadaId, "hearing.offence-plea-updated")
                        .build(), 
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("offenceId", offenceId.toString())
                        .add("pleaDate", pleaDate.toString())
                        .add("value", pleaValue.name())
                        .build());

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);
        
        assertThat(verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(jsonEnvelop)
                                .withName("hearing.offence-plea-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.pleaDate", is(pleaDate.toString())),
                                withJsonPath("$.value", is(pleaValue.name()))
                        )))
        ));
    }
}
