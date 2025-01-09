package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZonedDateTime.now;
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
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultV2Command;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.ResultsError;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequestedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ManageResultsFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsRejectedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsValidated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

@ExtendWith(MockitoExtension.class)
public class ValidateResultAmendmentsCommandHandlerTest {

    private static final String HEARING_COMMAND_SAVE_DRAFT_RESULT_V2 = "hearing.command.save-draft-result-v2";
    private static final String DRAFT_RESULT_SAVED_V2_EVENT_NAME = "hearing.draft-result-saved-v2";

    private static final String HEARING_MANAGE_RESULTS_FAILED = "hearing.manage-results-failed";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(ResultAmendmentsValidated.class,
            ResultAmendmentsRejectedV2.class, DraftResultSavedV2.class, ManageResultsFailed.class);

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
    private ValidateResultAmendmentsCommandHandler validateResultAmendmentsCommandHandler;

    @InjectMocks
    private ShareResultsCommandHandler shareResultsCommandHandler;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void givenAmendmentsRejected_whenHearingStateNotApprovalRequest_shouldReturnManageResultsFailed() throws EventStreamException {

        //Given
        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();
        final LocalDate hearingDay = LocalDate.parse("2020-08-21");
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
            apply(new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId1, 1));
            apply(ResultsSharedV3.builder().withHearingId(hearingId)
                    .withHearingDay(hearingDay)
                    .withSharedTime(ZonedDateTime.now())
                    .withHearing(hearing.getHearing())
                    .withNewAmendmentResults(new ArrayList<>())
                    .withTargets(new ArrayList<>()).build());
        }};

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonObject validateResultAmendments = createObjectBuilder()
                .add("id", hearingId.toString())
                .add("validateAction", "REJECT")
                .add("hearingDay", hearingDay.toString())
                .add("userId", userId2.toString()).build();

        final JsonObject validateResultAmendmentsJsonObject = objectToJsonObjectConverter.convert(validateResultAmendments);
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.validate-result-amendments").withUserId(UUID.randomUUID().toString()), validateResultAmendmentsJsonObject);

        validateResultAmendmentsCommandHandler.validateResultAmendments(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), Matchers.is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class), isBean(ManageResultsFailed.class)
                .with(ManageResultsFailed::getHearingId, Matchers.is(hearingId))
                .with(ManageResultsFailed::getHearingDay, Matchers.is(hearingDay))
                .with(ManageResultsFailed::getLastUpdatedVersion, Matchers.is(1))
                .with(ManageResultsFailed::getLastUpdatedByUserId, Matchers.is(userId1))
                .with(ManageResultsFailed::getResultsError, isBean(ResultsError.class)
                        .with(ResultsError::getType, Matchers.is(ResultsError.ErrorType.STATE))
                        .with(ResultsError::getCode, Matchers.is("205"))
                        .with(ResultsError::getReason, Matchers.is("REJECT not permitted! Hearing is in SHARED state")))
        );
    }

    @Test
    public void givenAmendmentsValidated_whenAmendmentRejectRequest_shouldReturnManageResultsFailed() throws EventStreamException {

        //Given
        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();
        final LocalDate hearingDay = LocalDate.parse("2020-08-21");
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final HearingAmended hearingAmended = new HearingAmended(hearingId, userId1, HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR);
        final ApprovalRequestedV2 approvalRequested = new ApprovalRequestedV2(hearingId, userId1);
        final ResultAmendmentsValidated resultAmendmentsValidated = ResultAmendmentsValidated.resultAmendmentsRequested()
                .withHearingId(hearingId)
                .withUserId(userId1)
                .withValidateResultAmendmentsTime(now())
                .build();

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
            apply(new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId1, 1));
            apply(ResultsSharedV3.builder().withHearingId(hearingId)
                    .withHearingDay(hearingDay)
                    .withSharedTime(ZonedDateTime.now())
                    .withHearing(hearing.getHearing())
                    .withNewAmendmentResults(new ArrayList<>())
                    .withTargets(new ArrayList<>()).build());
            apply(hearingAmended);
            apply(approvalRequested);
            apply(resultAmendmentsValidated);
        }};

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonObject validateResultAmendments = createObjectBuilder()
                .add("id", hearingId.toString())
                .add("validateAction", "REJECT")
                .add("hearingDay", hearingDay.toString())
                .add("userId", userId2.toString()).build();

        final JsonObject validateResultAmendmentsJsonObject = objectToJsonObjectConverter.convert(validateResultAmendments);
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.validate-result-amendments").withUserId(UUID.randomUUID().toString()), validateResultAmendmentsJsonObject);

        validateResultAmendmentsCommandHandler.validateResultAmendments(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), Matchers.is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class), isBean(ManageResultsFailed.class)
                .with(ManageResultsFailed::getHearingId, Matchers.is(hearingId))
                .with(ManageResultsFailed::getHearingDay, Matchers.is(hearingDay))
                .with(ManageResultsFailed::getLastUpdatedVersion, Matchers.is(1))
                .with(ManageResultsFailed::getLastUpdatedByUserId, Matchers.is(userId1))
                .with(ManageResultsFailed::getResultsError, isBean(ResultsError.class)
                        .with(ResultsError::getType, Matchers.is(ResultsError.ErrorType.STATE))
                        .with(ResultsError::getCode, Matchers.is("205"))
                        .with(ResultsError::getReason, Matchers.is("REJECT not permitted! Hearing is in VALIDATED state")))
        );
    }

    @Test
    public void givenAmendmentsRejected_whenHearingStateInApprovalRequested_shouldReturnResultAmendmentsRejectedEvent() throws EventStreamException {

        //Given
        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();
        final LocalDate hearingDay = LocalDate.parse("2020-08-21");
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
            apply(new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId1, 1));
            apply(ResultsSharedV3.builder().withHearingId(hearingId)
                    .withHearingDay(hearingDay)
                    .withSharedTime(ZonedDateTime.now())
                    .withHearing(hearing.getHearing())
                    .withNewAmendmentResults(new ArrayList<>())
                    .withTargets(new ArrayList<>()).build());
            apply(new ApprovalRequestedV2(hearingId, userId1));
        }};

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonObject validateResultAmendments = createObjectBuilder()
                .add("id", hearingId.toString())
                .add("validateAction", "REJECT")
                .add("hearingDay", hearingDay.toString())
                .add("userId", userId2.toString()).build();

        final JsonObject validateResultAmendmentsJsonObject = objectToJsonObjectConverter.convert(validateResultAmendments);
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.validate-result-amendments").withUserId(UUID.randomUUID().toString()), validateResultAmendmentsJsonObject);

        validateResultAmendmentsCommandHandler.validateResultAmendments(commandEnvelope);

        assertThat(
                verifyAppendAndGetArgumentFrom(this.hearingEventStream),
                streamContaining(jsonEnvelope(withMetadataEnvelopedFrom(commandEnvelope).withName("hearing.event.result-amendments-rejected-v2"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())))))
                ));
    }

    @Test
    public void givenAmendmentsApproved_whenHearingStateInApprovalRequested_shouldReturnResultAmendmentsApprovedEvent() throws EventStreamException {

        //Given
        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();
        final LocalDate hearingDay = LocalDate.parse("2020-08-21");
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
            apply(new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId1, 1));
            apply(ResultsSharedV3.builder().withHearingId(hearingId)
                    .withHearingDay(hearingDay)
                    .withSharedTime(ZonedDateTime.now())
                    .withHearing(hearing.getHearing())
                    .withNewAmendmentResults(new ArrayList<>())
                    .withTargets(new ArrayList<>()).build());
            apply(new ApprovalRequestedV2(hearingId, userId1));
        }};

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonObject validateResultAmendments = createObjectBuilder()
                .add("id", hearingId.toString())
                .add("validateAction", "APPROVE")
                .add("hearingDay", hearingDay.toString())
                .add("userId", userId2.toString()).build();

        final JsonObject validateResultAmendmentsJsonObject = objectToJsonObjectConverter.convert(validateResultAmendments);
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.validate-result-amendments").withUserId(UUID.randomUUID().toString()), validateResultAmendmentsJsonObject);

        validateResultAmendmentsCommandHandler.validateResultAmendments(commandEnvelope);

        assertThat(
                verifyAppendAndGetArgumentFrom(this.hearingEventStream),
                streamContaining(jsonEnvelope(withMetadataEnvelopedFrom(commandEnvelope).withName("hearing.event.result-amendments-validated"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())))))
                ));
    }

    @Test
    public void givenHearingStateInApprovalRequested_whenAmendmentsRejectedRequested_shouldBeAbleToSaveDraftResultsForHearingDay() throws EventStreamException {

        //Given
        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();
        final LocalDate hearingDay = LocalDate.parse("2020-08-21");
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
            apply(new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId1, 1));
            apply(ResultsSharedV3.builder().withHearingId(hearingId)
                    .withHearingDay(hearingDay)
                    .withSharedTime(ZonedDateTime.now())
                    .withHearing(hearing.getHearing())
                    .withNewAmendmentResults(new ArrayList<>())
                    .withTargets(new ArrayList<>()).build());
            apply(new ApprovalRequestedV2(hearingId, userId1));
        }};

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonObject validateResultAmendments = createObjectBuilder()
                .add("id", hearingId.toString())
                .add("validateAction", "REJECT")
                .add("hearingDay", hearingDay.toString())
                .add("userId", userId2.toString()).build();

        final JsonObject validateResultAmendmentsJsonObject = objectToJsonObjectConverter.convert(validateResultAmendments);
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.validate-result-amendments").withUserId(UUID.randomUUID().toString()), validateResultAmendmentsJsonObject);

        validateResultAmendmentsCommandHandler.validateResultAmendments(commandEnvelope);

        assertThat(
                verifyAppendAndGetArgumentFrom(this.hearingEventStream),
                streamContaining(jsonEnvelope(withMetadataEnvelopedFrom(commandEnvelope).withName("hearing.event.result-amendments-rejected-v2"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())))))
                ));

        //save draft results after cancel amendments
        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand()
                .setHearingId(hearing.getHearingId())
                .setHearingDay(hearingDay)
                .setVersion(1);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(UUID.randomUUID(), HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId2.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));
        this.shareResultsCommandHandler.saveDraftResultV2(envelope);

        ArgumentCaptor<Stream> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
        ((EventStream) Mockito.verify(this.hearingEventStream, times(2))).append((Stream) argumentCaptor.capture());
        final List<JsonEnvelope> eventList = argumentCaptor.getValue().toList();

        assertThat(eventList.stream().anyMatch(e -> DRAFT_RESULT_SAVED_V2_EVENT_NAME.equals(e.metadata().name())), Matchers.is(true));
    }

}