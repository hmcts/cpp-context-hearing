package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Before;
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
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class NewModelUpdateVerdictCommandHandlerTest {

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
    private NewModelUpdateVerdictCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    //TODO - handle multiple plea updates in a single call
    //TODO - add assertions around split jurors and unanimous

    @Test
    public void updateVerdict() throws EventStreamException {

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(randomUUID())
                .withHearingId(randomUUID())
                .addDefendant(Defendant.builder()
                        .withId(randomUUID())
                        .withPersonId(randomUUID())
                        .addOffence(Offence.builder()
                                .withId(randomUUID())
                                .withVerdict(Verdict.builder()
                                        .withId(randomUUID())
                                        .withValue(
                                                VerdictValue.builder()
                                                        .withId(randomUUID())
                                                        .withCategory("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription(STRING.next())
                                        )
                                )
                        )
                )
                .build();

        setupMockedEventStream(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getId(), this.offenceEventStream, new OffenceAggregate());

        final JsonEnvelope addVerdictCommand = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"), objectToJsonObjectConverter.convert(hearingUpdateVerdictCommand));

        this.hearingCommandHandler.updateVerdict(addVerdictCommand);

        assertThat(verifyAppendAndGetArgumentFrom(this.offenceEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.offence-verdict-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", equalTo(hearingUpdateVerdictCommand.getCaseId().toString())),
                                withJsonPath("$.originHearingId", equalTo(hearingUpdateVerdictCommand.getHearingId().toString())),
                                withJsonPath("$.offenceId", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.verdictId", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getId().toString())),
                                withJsonPath("$.verdictValueId", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue().getId().toString())),
                                withJsonPath("$.category", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue().getCategory())),
                                withJsonPath("$.code", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue().getCode())),
                                withJsonPath("$.description", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue().getDescription()))

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