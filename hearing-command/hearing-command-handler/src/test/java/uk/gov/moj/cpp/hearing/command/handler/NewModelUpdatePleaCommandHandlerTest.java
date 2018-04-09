package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjournDateUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.JudgeAssigned;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.PleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;
import uk.gov.moj.cpp.hearing.domain.event.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;
import uk.gov.moj.cpp.hearing.domain.event.VerdictAdded;

import java.util.UUID;
import java.util.function.Consumer;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;

@RunWith(MockitoJUnitRunner.class)
public class NewModelUpdatePleaCommandHandlerTest {

    @Mock
    private EventStream offenceEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            //new events.
            Initiated.class,

            OffencePleaUpdated.class,
            OffenceVerdictUpdated.class,

            //TODO - GPE-3032 CLEANUP - remove old events.
            DraftResultSaved.class, HearingInitiated.class, CaseAssociated.class, CourtAssigned.class,
            RoomBooked.class, ProsecutionCounselAdded.class, DefenceCounselAdded.class,
            HearingAdjournDateUpdated.class, ResultsShared.class, ResultAmended.class, PleaAdded.class, PleaChanged.class,
            HearingPleaAdded.class, HearingPleaChanged.class, HearingPleaUpdated.class, JudgeAssigned.class,
            VerdictAdded.class, ConvictionDateAdded.class, HearingVerdictUpdated.class, ConvictionDateRemoved.class);

    @Mock
    private HearingCommandHandler oldHandler;

    @InjectMocks
    private NewModelUpdatePleaCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    //TODO - handle multiple plea updates in a single call

    @Ignore
    @Test
    public void updatePlea() throws Throwable {

        HearingUpdatePleaCommand hearingUpdatePleaCommand = HearingUpdatePleaCommand.builder()
                .withCaseId(randomUUID())
                .withHearingId(randomUUID())
                .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                        .withId(randomUUID())
                        .withPersonId(randomUUID())
                        .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                .withId(randomUUID())
                                .withPlea(uk.gov.moj.cpp.hearing.command.plea.Plea.builder()
                                        .withId(randomUUID())
                                        .withPleaDate(PAST_LOCAL_DATE.next())
                                        .withValue("GUILTY")
                                )
                        )
                )
                .build();

        setupMockedEventStream(hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getId(), this.offenceEventStream, new NewModelHearingAggregate());

        final JsonEnvelope addPleaCommand = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"), objectToJsonObjectConverter.convert(hearingUpdatePleaCommand));

        this.hearingCommandHandler.updatePlea(addPleaCommand);

        assertThat(verifyAppendAndGetArgumentFrom(this.offenceEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addPleaCommand)
                                .withName("hearing.offence-plea-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.originHearingId", equalTo(hearingUpdatePleaCommand.getHearingId().toString())),
                                withJsonPath("$.offenceId", equalTo(hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.pleaDate", equalTo(hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getPlea().getPleaDate().toString())),
                                withJsonPath("$.value", equalTo(hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getPlea().getValue()))
                        )))
        ));
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        setupMockedEventStream(id, eventStream, aggregate, a -> {
        });
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate, Consumer<T> consumer) {
        consumer.accept(aggregate);
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }

    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

}