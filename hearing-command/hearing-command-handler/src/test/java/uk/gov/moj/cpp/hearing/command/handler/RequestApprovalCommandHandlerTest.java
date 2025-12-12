package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.command.result.SaveDraftResultV2Command.saveDraftResultCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultV2Command;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.ResultsError;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequested;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequestedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ManageResultsFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancellationFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancelledV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestApprovalCommandHandlerTest {

    private static final String HEARING_EVENTS_RESULT_AMENDMENTS_CANCELLED_V2 = "hearing.events.result-amendments-cancelled-v2";
    private static final String HEARING_MANAGE_RESULTS_FAILED = "hearing.manage-results-failed";
    private static final String HEARING_EVENT_APPROVAL_REQUESTED_V2 = "hearing.event.approval-requestedV2";
    private static final String HEARING_COMMAND_APPROVAL_REQUESTED = "hearing.command.approval-requested";
    private static final String HEARING_COMMAND_CHANGE_CANCEL_AMENDMENTS = "hearing.command.change-cancel-amendments";

    private static final String HEARING_COMMAND_SAVE_DRAFT_RESULT_V2 = "hearing.command.save-draft-result-v2";
    private static final String DRAFT_RESULT_SAVED_V2_EVENT_NAME = "hearing.draft-result-saved-v2";
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(ApprovalRequested.class, ResultAmendmentsCancellationFailed.class,
            ManageResultsFailed.class, ApprovalRequestedV2.class, ResultAmendmentsCancelledV2.class, DraftResultSavedV2.class);

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private RequestApprovalCommandHandler requestApprovalCommandHandler;

    @InjectMocks
    private ShareResultsCommandHandler shareResultsCommandHandler;

    private static InitiateHearingCommand initiateHearingCommand;

    @BeforeEach
    public void setup() {
        initiateHearingCommand = standardInitiateHearingTemplate();
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void givenResultsSavedForHearingDay_whenRequestApprovalAndHearingNotInAmendedState_shouldRaiseApprovalRejectedEvent() throws EventStreamException {

        //Given
        final UUID userId = UUID.randomUUID();
        final int initialVersion = 1;
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, initialVersion);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(initialDraftResultSavedV2));
        }};
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDay", hearingDay.toString())
                .add("version", initialVersion)
                .add("userId", userId.toString())
                .build();
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND_APPROVAL_REQUESTED).withUserId(userId.toString()), objectToJsonObjectConverter.convert(requestApproval));

        requestApprovalCommandHandler.requestApproval(commandEnvelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(commandEnvelope).withName(HEARING_MANAGE_RESULTS_FAILED),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())))))
        ));
    }

    @Test
    public void givenResultsSavedForHearingDay_whenRequestApprovalAndHearingInAmendedState_shouldRaiseApprovalRequestedEvent() throws EventStreamException {
        //Given
        final UUID userId = UUID.randomUUID();
        final int initialVersion = 1;
        final Hearing hearing = initiateHearingCommand.getHearing();
        final UUID hearingId = hearing.getId();
        final LocalDate hearingDay = hearing.getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, initialVersion);
        final HearingAmended hearingAmended = new HearingAmended(hearingId, userId, HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
            apply(initialDraftResultSavedV2);
            apply(hearingAmended);
        }};
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDay", hearingDay.toString())
                .add("version", initialVersion)
                .add("userId", userId.toString())
                .build();
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND_APPROVAL_REQUESTED).withUserId(userId.toString()), objectToJsonObjectConverter.convert(requestApproval));

        requestApprovalCommandHandler.requestApproval(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_EVENT_APPROVAL_REQUESTED_V2.equals(e.metadata().name())), Matchers.is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), ApprovalRequestedV2.class), isBean(ApprovalRequestedV2.class)
                .with(ApprovalRequestedV2::getHearingId, Matchers.is(hearingId))
                .with(ApprovalRequestedV2::getUserId, Matchers.is(userId))
        );
    }

    @Test
    public void givenResultsSavedWithVersionRecordedForHearingDay_whenRequestApprovalWithInCorrectVersion_shouldRaiseManageResultsFailedEvent() throws EventStreamException {
        //Given
        final UUID userId = UUID.randomUUID();
        final int version = 2;
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, 1);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(initiateHearingCommand.getHearing()));
            apply(initialDraftResultSavedV2);
        }};
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDay", hearingDay.toString())
                .add("version", version)
                .add("userId", userId.toString())
                .build();
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND_APPROVAL_REQUESTED).withUserId(UUID.randomUUID().toString()), objectToJsonObjectConverter.convert(requestApproval));

        requestApprovalCommandHandler.requestApproval(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), Matchers.is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class), isBean(ManageResultsFailed.class)
                .with(ManageResultsFailed::getHearingId, Matchers.is(hearingId))
                .with(ManageResultsFailed::getHearingDay, Matchers.is(hearingDay))
                .with(ManageResultsFailed::getLastUpdatedVersion, Matchers.is(1))
                .with(ManageResultsFailed::getLastUpdatedByUserId, Matchers.is(userId))
                .with(ManageResultsFailed::getVersion, Matchers.is(2))
                .with(ManageResultsFailed::getResultsError, isBean(ResultsError.class)
                        .with(ResultsError::getType, Matchers.is(ResultsError.ErrorType.VERSION))
                        .with(ResultsError::getCode, Matchers.is("102"))
                        .with(ResultsError::getReason, Matchers.is("Approval Request failed for version: 2, lastUpdatedVersion: 1")))
        );
    }

    @Test
    public void givenResultsSavedWithVersionMultipleHearingDays_whenRequestApprovalWithInCorrectVersion_shouldRaiseManageResultsFailedEventForCorrectHearingDay() throws EventStreamException {
        //Given
        final UUID userId = UUID.randomUUID();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultsDay1 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, 1);
        final DraftResultSavedV2 initialDraftResultsDay2V1 = new DraftResultSavedV2(hearingId, hearingDay.plusDays(1), someJsonObject, userId, 1);
        final DraftResultSavedV2 initialDraftResultsDay2V2 = new DraftResultSavedV2(hearingId, hearingDay.plusDays(1), someJsonObject, userId, 2);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(initiateHearingCommand.getHearing()));
            apply(initialDraftResultsDay1);
            apply(initialDraftResultsDay2V1);
            apply(initialDraftResultsDay2V2);
        }};
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDay", hearingDay.toString())
                .add("version", 2)
                .add("userId", userId.toString())
                .build();
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND_APPROVAL_REQUESTED).withUserId(UUID.randomUUID().toString()), objectToJsonObjectConverter.convert(requestApproval));

        requestApprovalCommandHandler.requestApproval(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), Matchers.is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class), isBean(ManageResultsFailed.class)
                .with(ManageResultsFailed::getHearingId, Matchers.is(hearingId))
                .with(ManageResultsFailed::getHearingDay, Matchers.is(hearingDay))
                .with(ManageResultsFailed::getLastUpdatedVersion, Matchers.is(1))
                .with(ManageResultsFailed::getLastUpdatedByUserId, Matchers.is(userId))
                .with(ManageResultsFailed::getVersion, Matchers.is(2))
                .with(ManageResultsFailed::getResultsError, isBean(ResultsError.class)
                        .with(ResultsError::getType, Matchers.is(ResultsError.ErrorType.VERSION))
                        .with(ResultsError::getCode, Matchers.is("102"))
                        .with(ResultsError::getReason, Matchers.is("Approval Request failed for version: 2, lastUpdatedVersion: 1")))
        );
    }

    @Test
    public void shouldEmitCancelAmendmentsFailedEvent() throws EventStreamException {
        //Given
        final UUID hearingId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.parse("2023-10-10");
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultsDay1 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, 1);
        final HearingAmended hearingAmended = new HearingAmended(hearingId, userId, HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR);
        final ApprovalRequestedV2 approvalRequested = new ApprovalRequestedV2(hearingId, userId);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(initiateHearingCommand.getHearing()));
            apply(initialDraftResultsDay1);
            apply(hearingAmended);
            apply(approvalRequested);
        }};

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDay", hearingDay.toString())
                .build();
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND_CHANGE_CANCEL_AMENDMENTS).withUserId(UUID.randomUUID().toString()), objectToJsonObjectConverter.convert(requestApproval));
        requestApprovalCommandHandler.cancelAmendments(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), Matchers.is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class), isBean(ManageResultsFailed.class)
                .with(ManageResultsFailed::getHearingId, Matchers.is(hearingId))
                .with(ManageResultsFailed::getHearingDay, Matchers.is(hearingDay))
                .with(ManageResultsFailed::getResultsError, isBean(ResultsError.class)
                        .with(ResultsError::getType, Matchers.is(ResultsError.ErrorType.STATE))
                        .with(ResultsError::getCode, Matchers.is("203"))
                        .with(ResultsError::getReason, Matchers.is("Cancel amendments not permitted! Hearing is in APPROVAL_REQUESTED state")))
        );


    }

    @Test
    public void givenResultsSavedWithVersion_thenSharedAndAmended_whenCancelAmendmentForHearingDay_andAbleToSaveDraftResultsWithPreviouslySharedResults() throws EventStreamException {
        //Given
        final UUID userId = UUID.randomUUID();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultsDay1 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, 1);
        final ResultsSharedV3 resultsSharedV3 = ResultsSharedV3.builder()
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withHearing(initiateHearingCommand.getHearing())
                .withNewAmendmentResults(List.of())
                .withTargets(List.of())
                .withSharedTime(ZonedDateTime.now()).build();
        final HearingAmended hearingAmended = new HearingAmended(hearingId, userId, HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR);

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(initiateHearingCommand.getHearing()));
            apply(initialDraftResultsDay1);
            apply(resultsSharedV3);
            apply(hearingAmended);
        }};
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDay", hearingDay.toString())
                .add("version", 1)
                .add("userId", userId.toString())
                .build();
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND_CHANGE_CANCEL_AMENDMENTS).withUserId(userId.toString()), objectToJsonObjectConverter.convert(requestApproval));
        requestApprovalCommandHandler.cancelAmendments(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_EVENTS_RESULT_AMENDMENTS_CANCELLED_V2.equals(e.metadata().name())), Matchers.is(true));

        //save draft results after cancel amendments
        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand()
                .setHearingId(initiateHearingCommand.getHearing().getId())
                .setHearingDay(hearingDay)
                .setVersion(5);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(UUID.randomUUID(), HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));
        this.shareResultsCommandHandler.saveDraftResultV2(envelope);

        ArgumentCaptor<Stream> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
        ((EventStream) Mockito.verify(this.hearingEventStream, times(2))).append((Stream) argumentCaptor.capture());
        final List<JsonEnvelope> eventList = argumentCaptor.getValue().toList();

        assertThat(eventList.stream().anyMatch(e -> DRAFT_RESULT_SAVED_V2_EVENT_NAME.equals(e.metadata().name())), Matchers.is(true));
    }
}