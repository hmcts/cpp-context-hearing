package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.plea.HearingPlea;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.HearingDetails;
import uk.gov.moj.cpp.hearing.domain.ResultLine;
import uk.gov.moj.cpp.hearing.domain.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingsPleaAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"WeakerAccess", "CdiInjectionPointsInspection"})
@ServiceComponent(COMMAND_HANDLER)
public class HearingCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingCommandHandler.class);

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_JUDGE_ID = "judgeId";
    private static final String FIELD_JUDGE_TITLE = "judgeTitle";
    private static final String FIELD_JUDGE_FIRST_NAME = "judgeFirstName";
    private static final String FIELD_JUDGE_LAST_NAME = "judgeLastName";

    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_COURT_ROOM_ID = "roomId";
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_DURATION = "duration";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_ATTENDEE_ID = "attendeeId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_DEFENDANT_IDS = "defendantIds";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_SHARED_TIME = "sharedTime";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_LEVEL = "level";
    private static final String RESULT_LABEL = "resultLabel";
    private static final String FIELD_RESULT_PROMPTS = "prompts";
    private static final String FIELD_RESULT_LABEL = "label";
    private static final String FIELD_RESULT_VALUE = "value";
    private static final String FIELD_COURT = "court";
    private static final String FIELD_COURT_ROOM = "courtRoom";
    private static final String FIELD_CLERK_OF_THE_COURT_ID = "clerkOfTheCourtId";
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME = "clerkOfTheCourtFirstName";
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME = "clerkOfTheCourtLastName";

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.initiate-hearing")
    public void initiateHearing(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final ZonedDateTime startDateTime = fromJsonString(payload.getJsonString(FIELD_START_DATE_TIME));
        final int duration = payload.getInt(FIELD_DURATION);
        final String hearingType = payload.getString(FIELD_HEARING_TYPE);
        final UUID courtCentreId = getUUID(payload, FIELD_COURT_CENTRE_ID).orElse(null);
        final String courtCentreName = payload.getString(FIELD_COURT_CENTRE_NAME, null);
        final UUID roomId = getUUID(payload, FIELD_COURT_ROOM_ID).orElse(null);
        final String roomName = payload.getString(FIELD_ROOM_NAME, null);
        final UUID caseId = getUUID(payload, FIELD_CASE_ID).orElse(null);
        final String judgeId = payload.getString(FIELD_JUDGE_ID, null);
        final String judgeFirstName = payload.getString(FIELD_JUDGE_FIRST_NAME, null);
        final String judgeLastName = payload.getString(FIELD_JUDGE_LAST_NAME, null);
        final String judgeTitle = payload.getString(FIELD_JUDGE_TITLE, null);
        final HearingDetails hearingDetails = new HearingDetails.Builder().withHearingId(hearingId).withStartDateTime(startDateTime).withDuration(duration).withHearingType(hearingType)
                .withCourtCentreId(courtCentreId).withCourtCentreName(courtCentreName).withRoomId(roomId).withRoomName(roomName).withCaseId(caseId)
                .withJudgeId(judgeId).withJudgeFirstName(judgeFirstName).withJudgeLastName(judgeLastName).withJudgeTitle(judgeTitle).build();

        applyToHearingAggregate(hearingId, aggregate -> aggregate.initiateHearing(hearingDetails), command);
    }

    @Handles("hearing.allocate-court")
    public void allocateCourt(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String courtCentreName = payload.getString(FIELD_COURT_CENTRE_NAME);

        applyToHearingAggregate(hearingId, aggregate -> aggregate.allocateCourt(hearingId, courtCentreName), command);
    }

    @Handles("hearing.book-room")
    public void bookRoom(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String roomName = payload.getString(FIELD_ROOM_NAME);

        applyToHearingAggregate(hearingId, aggregate -> aggregate.bookRoom(hearingId, roomName), command);
    }

    @Handles("hearing.adjourn-date")
    public void adjournHearingDate(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final LocalDate startDate = LocalDate.parse(payload.getString(FIELD_START_DATE));

        applyToHearingAggregate(hearingId, aggregate -> aggregate.adjournHearingDate(hearingId, startDate), command);
    }

    @Handles("hearing.add-case")
    public void addCase(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));

        applyToHearingAggregate(hearingId, aggregate -> aggregate.addCaseToHearing(hearingId, caseId), command);
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID personId = fromString(payload.getString(FIELD_PERSON_ID));
        final UUID attendeeId = fromString(payload.getString(FIELD_ATTENDEE_ID));
        final String status = payload.getString(FIELD_STATUS);

        applyToHearingAggregate(hearingId, aggregate -> aggregate.addProsecutionCounsel(hearingId, attendeeId, personId, status), command);
    }

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID personId = fromString(payload.getString(FIELD_PERSON_ID));
        final UUID attendeeId = fromString(payload.getString(FIELD_ATTENDEE_ID));
        final String status = payload.getString(FIELD_STATUS);
        final JsonArray jsonArray = payload.getJsonArray(FIELD_DEFENDANT_IDS);
        final List<UUID> defendantIds = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final String defendantIdString = jsonArray.getJsonObject(i).getString(FIELD_DEFENDANT_ID);
            defendantIds.add(fromString(defendantIdString));
        }

        applyToHearingAggregate(hearingId, aggregate -> aggregate.addDefenceCounsel(hearingId, attendeeId, personId, defendantIds, status), command);
    }

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID defendantId = fromString(payload.getString(FIELD_DEFENDANT_ID));
        final UUID targetId = fromString(payload.getString("targetId"));
        final UUID offenceId = fromString(payload.getString(FIELD_OFFENCE_ID));
        final String draftResult = payload.getString("draftResult");
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));

        final Stream<Object> events = streamOf(new DraftResultSaved(targetId, defendantId, offenceId, draftResult, hearingId));
        this.eventSource.getStreamById(hearingId).append(events.map(this.enveloper.withMetadataFrom(command)));
    }

    @Handles("hearing.command.share-results")
    public void shareResult(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final ZonedDateTime sharedTime = ZonedDateTimes.fromJsonString(payload.getJsonString(FIELD_SHARED_TIME));

        final List<ResultLine> resultLines = payload.getJsonArray(FIELD_RESULT_LINES)
                .getValuesAs(JsonObject.class).stream()
                .map(this::extractResultLine)
                .collect(toList());

        final EventStream eventStream = this.eventSource.getStreamById(hearingId);
        final HearingAggregate aggregate = this.aggregateService.get(eventStream, HearingAggregate.class);
        final Stream<Object> events = aggregate.shareResults(hearingId, sharedTime, resultLines);

        eventStream.append(events.map(this.enveloper.withMetadataFrom(command)));
    }

    @Handles("hearing.command.update-plea")
    public void updatePlea(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("Processing hearing.command.update-plea command");
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));
        final HearingUpdatePleaCommand hearingUpdatePleaCommand =
                this.jsonObjectToObjectConverter.convert(payload, HearingUpdatePleaCommand.class);
        applyHearingPleaAggregate(caseId, aggregate -> aggregate.updatePlea(hearingUpdatePleaCommand), command);
    }

    @Handles("hearing.plea-add")
    public void pleaAdd(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final HearingPlea hearingPlea =
                this.jsonObjectToObjectConverter.convert(payload, HearingPlea.class);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.addPlea(hearingPlea), command);
    }

    @Handles("hearing.plea-change")
    public void pleaChange(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final HearingPlea hearingPlea =
                this.jsonObjectToObjectConverter.convert(payload, HearingPlea.class);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.changePlea(hearingPlea), command);
    }

    @Handles("hearing.command.update-verdict")
    public void updateVerdict(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("Processing hearing.command.update-verdict command");
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand =
                this.jsonObjectToObjectConverter.convert(payload, HearingUpdateVerdictCommand.class);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.updateVerdict(hearingUpdateVerdictCommand), command);
    }

    private void applyToHearingAggregate(final UUID streamId, final Function<HearingAggregate, Stream<Object>> function,
                                         final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final HearingAggregate aggregate = this.aggregateService.get(eventStream, HearingAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

    private void applyHearingPleaAggregate(final UUID streamId, final Function<HearingsPleaAggregate, Stream<Object>> function,
                                           final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final HearingsPleaAggregate hearingsPleaAggregate = this.aggregateService.get(eventStream, HearingsPleaAggregate.class);
        final Stream<Object> events = function.apply(hearingsPleaAggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

    private ResultLine extractResultLine(final JsonObject resultLine) {
        return new ResultLine(fromString(resultLine.getString(FIELD_GENERIC_ID)),
                resultLine.containsKey(FIELD_LAST_SHARED_RESULT_ID) ? fromString(resultLine.getString(FIELD_LAST_SHARED_RESULT_ID)) : null,
                fromString(resultLine.getString(FIELD_CASE_ID)),
                fromString(resultLine.getString(FIELD_PERSON_ID)),
                fromString(resultLine.getString(FIELD_OFFENCE_ID)),
                resultLine.getString(FIELD_LEVEL),
                resultLine.getString(RESULT_LABEL),
                resultLine.getJsonArray(FIELD_RESULT_PROMPTS).getValuesAs(JsonObject.class).stream()
                        .map(prompt -> new ResultPrompt(prompt.getString(FIELD_RESULT_LABEL), prompt.getString(FIELD_RESULT_VALUE)))
                        .collect(toList()),
                resultLine.getString(FIELD_COURT),
                resultLine.getString(FIELD_COURT_ROOM),
                fromString(resultLine.getString(FIELD_CLERK_OF_THE_COURT_ID)),
                resultLine.getString(FIELD_CLERK_OF_THE_COURT_FIRST_NAME),
                resultLine.getString(FIELD_CLERK_OF_THE_COURT_LAST_NAME));
    }
}
