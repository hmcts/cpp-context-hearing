package uk.gov.moj.cpp.hearing.event;

import static java.lang.String.format;
import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import org.json.JSONObject;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.listing.Hearing;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;
import uk.gov.moj.cpp.hearing.event.command.InitiateHearingCommand;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import javax.inject.Inject;
import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventProcessor.class);

    private static final String PUBLIC_HEARING_INITIATED = "public.hearing.initiated";
    private static final String PUBLIC_HEARING_RESULTED = "public.hearing.resulted";
    private static final String PUBLIC_HEARING_RESULT_AMENDED = "public.hearing.result-amended";
    private static final String PUBLIC_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";
    private static final String PUBLIC_HEARING_EVENT_LOGGED = "public.hearing.event-logged";
    private static final String PUBLIC_HEARING_TIMESTAMP_CORRECTED = "public.hearing.event-timestamp-corrected";
    private static final String PUBLIC_HEARING_PLEA_UPDATED = "public.hearing.plea-updated";
    private static final String PUBLIC_HEARING_VERDICT_UPDATED = "public.hearing.verdict-updated";
    private static final String PUBLIC_HEARING_UPDATE_PLEA_IGNORED = "public.hearing.update-plea-ignored";
    private static final String PUBLIC_HEARING_UPDATE_VERDICT_IGNORED = "public.hearing.update-verdict-ignored";


    private static final String FIELD_ID = "id";
    private static final String FIELD_HEARING_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LAST_HEARING_EVENT_ID = "lastHearingEventId";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_PRIORITY = "priority";
    private static final String FIELD_CASE_URN = "caseUrn";
    private static final String FIELD_CASE = "case";
    private static final String FIELD_HEARING_EVENT_DEFINITION = "hearingEventDefinition";
    private static final String FIELD_HEARING_EVENT = "hearingEvent";
    private static final String FIELD_HEARING = "hearing";
    private static final String FIELD_COURT_ROOM_NAME = "courtRoomName";
    private static final String FIELD_COURT_ROOM_ID = "courtRoomId";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_COURT_CENTRE = "courtCentre";

    private static final String HEARING_QUERY = "hearing.get.hearing";
    private static final String CASE_QUERY = "progression.query.caseprogressiondetail";
    private static final String HEARING_INITIATE_HEARING = "hearing.initiate-hearing";
    private static final String HEARING_RECORD_MAGS_COURT_HEARING = "hearing.record-mags-court-hearing";
    private static final String HEARING_PLEA_ADD = "hearing.plea-add";
    private static final String HEARING_PLEA_CHANGE = "hearing.plea-change";

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_CASE_IDS = "caseIds";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final int DEFAULT_HEARING_DURATION_MINUTES = 15;

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Inject
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;

    @Handles("hearing.results-shared")
    public void publishHearingResultsSharedPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.debug("'public.hearing.resulted' event received {}", payload);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULTED).apply(payload));
    }

    @Handles("hearing.result-amended")
    public void publishHearingResultAmendedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULT_AMENDED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.draft-result-saved")
    public void publicDraftResultSavedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_DRAFT_RESULT_SAVED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        JsonString hearingId = event.payloadAsJsonObject().getJsonObject(FIELD_HEARING).getJsonString(FIELD_ID);

        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_INITIATED).apply(createObjectBuilder()
                .add(FIELD_HEARING_ID, hearingId)
                .build()));
    }

    @Handles("hearing.hearing.confirmed-recorded")
    public void processHearingConfirmedRecorded(final JsonEnvelope event) {
        LOGGER.trace("Received hearing.hearing.confirmed-recorded event, processing");
        final JsonObject payload = event.payloadAsJsonObject();
        final Hearing hearing = this.jsonObjectToObjectConverter.convert(payload.getJsonObject(FIELD_HEARING), Hearing.class);
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));

        final InitiateHearingCommand initiateHearingCommand = getInitiateHearingCommand(caseId, hearing);

        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_INITIATE_HEARING)
                .apply(this.objectToJsonValueConverter.convert(initiateHearingCommand)));
    }

    @Handles("hearing.sending-sheet-recorded")
    public void processSendingSheetRecordedRecordMags(final JsonEnvelope event) {
        LOGGER.trace("Received hearing.sending-sheet-recorded event, processing");
        final JsonObject payload = event.payloadAsJsonObject();
        final SendingSheetCompletedRecorded sendingSheetCompletedRecorded = this.jsonObjectToObjectConverter.convert(payload, SendingSheetCompletedRecorded.class);

        final RecordMagsCourtHearingCommand command = new RecordMagsCourtHearingCommand(sendingSheetCompletedRecorded.getHearing());

        final JsonValue newPayload = this.objectToJsonValueConverter.convert(command);
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_RECORD_MAGS_COURT_HEARING)
                .apply(newPayload));
    }

    @Handles("hearing.case.plea-added")
    public void processCasePleaAdded(final JsonEnvelope event) {
        LOGGER.trace("Received plea-added event, processing");
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_PLEA_ADD)
                .apply(event.payloadAsJsonObject()));

    }

    @Handles("hearing.case.plea-changed")
    public void processCasePleaChanged(final JsonEnvelope event) {

        LOGGER.trace("Received plea-changed event, processing");
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_PLEA_CHANGE)
                .apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.hearing-plea-updated")
    public void publishHearingPleaUpdatedPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-plea-updated' event received {}", payload);
        final String caseId = payload.getString(FIELD_CASE_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_PLEA_UPDATED).apply(Json.createObjectBuilder().add(FIELD_CASE_ID, caseId).build()));
    }
    @Handles("hearing.hearing-update-plea-ignored")
    public void publishHearingUpdatePleaIgnoredPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-update-plea-ignored' event received {}", payload);
        final String caseId = payload.getString(FIELD_CASE_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_UPDATE_PLEA_IGNORED).apply(Json.createObjectBuilder().add(FIELD_CASE_ID, caseId).build()));
    }

    @Handles("hearing.hearing-verdict-updated")
    public void publishHearingVerdictUpdatedPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-verdict-updated' event received {}", payload);
        final String hearingId = payload.getString(FIELD_HEARING_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_VERDICT_UPDATED).apply(Json.createObjectBuilder().add(FIELD_HEARING_ID, hearingId).build()));
    }



    @Handles("hearing.hearing-update-verdict-ignored")
    public void publishHearingUpdateVerdictIgnoredPublicEvent(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        LOGGER.trace("'hearing.hearing-update-verdict-ignored' event received {}", payload);
        final String hearingId = payload.getString(FIELD_HEARING_ID);
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_UPDATE_VERDICT_IGNORED).apply(Json.createObjectBuilder().add(FIELD_HEARING_ID, hearingId).build()));
    }

    @Handles("hearing.hearing-event-logged")
    public void publishHearingEventLoggedPublicEvent(final JsonEnvelope event) {
        final JsonObject jsonObject = event.payloadAsJsonObject();
        final String hearingEventDefinitionId = jsonObject.getString(FIELD_HEARING_DEFINITION_ID);
        final String hearingEventId = jsonObject.getString(FIELD_HEARING_EVENT_ID);
        final String lastHearingEventId = jsonObject.getString(FIELD_LAST_HEARING_EVENT_ID, null);
        final String recordedLabel = jsonObject.getString(FIELD_RECORDED_LABEL);
        final String eventTime = jsonObject.getString(FIELD_EVENT_TIME);
        final String lastModifiedTime = jsonObject.getString(FIELD_LAST_MODIFIED_TIME);
        final boolean priority = !jsonObject.getBoolean(FIELD_ALTERABLE);
        final Optional<HearingDetails> optionalHearingDetails = getHearingDetails(event);

        if (optionalHearingDetails.isPresent()) {
            final HearingDetails hearingDetails = optionalHearingDetails.get();
            final JsonObjectBuilder hearingEventJsonBuilder = createObjectBuilder()
                    .add(FIELD_HEARING_EVENT_ID, hearingEventId)
                    .add(FIELD_RECORDED_LABEL, recordedLabel)
                    .add(FIELD_EVENT_TIME, eventTime)
                    .add(FIELD_LAST_MODIFIED_TIME, lastModifiedTime);
            if (null != lastHearingEventId) {
                hearingEventJsonBuilder.add(FIELD_LAST_HEARING_EVENT_ID, lastHearingEventId);
            }

            final JsonObjectBuilder courtHouseJsonBuilder = createObjectBuilder()
                    .add(FIELD_COURT_CENTRE_ID, hearingDetails.getCourtCenterId().toString())
                    .add(FIELD_COURT_CENTRE_NAME, hearingDetails.getCourtCenterName())
                    .add(FIELD_COURT_ROOM_NAME, hearingDetails.getCourtRoomName())
                    .add(FIELD_COURT_ROOM_ID, hearingDetails.getCourtRoomId().toString());

            final JsonObjectBuilder hearingJsonBuilder = createObjectBuilder();
            hearingJsonBuilder
                    .add(FIELD_HEARING_TYPE, hearingDetails.getHearingType())
                    .add(FIELD_COURT_CENTRE, courtHouseJsonBuilder);

            final JsonObjectBuilder hearingEventPayloadBuilder = createObjectBuilder()
                    .add(FIELD_CASE, createObjectBuilder().add(FIELD_CASE_URN, hearingDetails.getCaseUrn()))
                    .add(FIELD_HEARING_EVENT_DEFINITION, createObjectBuilder()
                            .add(FIELD_HEARING_DEFINITION_ID, hearingEventDefinitionId)
                            .add(FIELD_PRIORITY, priority))
                    .add(FIELD_HEARING_EVENT, hearingEventJsonBuilder)
                    .add(FIELD_HEARING, hearingJsonBuilder);

            if (null != lastHearingEventId) {
                final JsonObject payload = hearingEventPayloadBuilder.build();
                LOGGER.debug("public.hearing-event-timestamp-corrected event published {}", payload);
                this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_TIMESTAMP_CORRECTED).apply(payload));
            } else {
                final JsonObject payload = hearingEventPayloadBuilder.build();
                LOGGER.debug("public.hearing-event-logged event published {}", payload);
                this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_EVENT_LOGGED).apply(payload));
            }
        } else {
            LOGGER.error("Missing hearing details/case details for hearingId {} and hearingEventId {}. Hearing event won't be published!",
                    event.payloadAsJsonObject().getString(FIELD_HEARING_ID), hearingEventId);
        }
    }

    @Handles("hearing.mags-court-hearing-recorded")
    public void magsCourtProcessed(final JsonEnvelope event) {
        LOGGER.trace("Received hearing.mags-court-hearing-recorded event, processing");
        final JsonObject payload = event.payloadAsJsonObject();
        final MagsCourtHearingRecorded preceding = this.jsonObjectToObjectConverter.convert(payload, MagsCourtHearingRecorded.class);
        final UUID caseId = preceding.getOriginatingHearing().getCaseId();

        final InitiateHearingCommand initiateHearingCommand =
                getInitiateHearingCommand(caseId, preceding.getOriginatingHearing(), preceding.getHearingId(), preceding.getConvictionDate());

        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_INITIATE_HEARING)
                .apply(this.objectToJsonValueConverter.convert(initiateHearingCommand)));

    }

    private Optional<HearingDetails> getHearingDetails(final JsonEnvelope event) {
        Optional<HearingDetails> hearingDetails = Optional.empty();
        final String hearingId = event.payloadAsJsonObject().getString(FIELD_HEARING_ID);
        final JsonEnvelope hearingQuery = this.enveloper.withMetadataFrom(event, HEARING_QUERY).apply(
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId)
                        .build()
        );

        final JsonObject hearingResponsePayload = this.requester.request(hearingQuery).payloadAsJsonObject();

        if (!hearingResponsePayload.isEmpty()) {
            final String caseId = hearingResponsePayload.getJsonArray(FIELD_CASE_IDS).getString(0);

            final JsonEnvelope caseQuery = this.enveloper.withMetadataFrom(event, CASE_QUERY).apply(
                    createObjectBuilder().add(FIELD_CASE_ID, caseId).build()
            );

            try {
                final JsonObject caseResponsePayload = this.requester.request(caseQuery).payloadAsJsonObject();

                if (caseResponsePayload != null && !caseResponsePayload.isEmpty()) {
                    final String caseUrn = caseResponsePayload.getString(FIELD_CASE_URN);
                    final UUID courtCenterId = fromString(hearingResponsePayload.getString(FIELD_COURT_CENTRE_ID));
                    final String courtCenterName = hearingResponsePayload.getString(FIELD_COURT_CENTRE_NAME);
                    final String roomName = hearingResponsePayload.getString(FIELD_ROOM_NAME);
                    final UUID roomId = fromString(hearingResponsePayload.getString(FIELD_ROOM_ID));
                    final String hearingType = hearingResponsePayload.getString(FIELD_HEARING_TYPE);

                    final HearingDetails details = new HearingDetails(caseUrn, courtCenterId, courtCenterName, roomName, roomId, hearingType);
                    hearingDetails = Optional.of(details);
                } else {
                    LOGGER.error("Could not find case details for case id {}, hearing id {} from action {}:", caseId, hearingId, CASE_QUERY);
                }
            } catch (final RuntimeException e) {
                LOGGER.error(format("Error while retrieving case details/hearing details for case id %s, hearing id %s:", caseId, hearingId), e);
            }
        } else {
            LOGGER.error("Could not find hearing details for hearing id {} from action {}:", hearingId, HEARING_QUERY);
        }
        return hearingDetails;
    }

    class HearingDetails {
        private final String caseUrn;
        private final UUID courtCenterId;
        private final String courtCenterName;
        private final String courtRoomName;
        private final UUID courtRoomId;
        private final String hearingType;

        public HearingDetails(
                final String caseUrn,
                final UUID courtCenterId,
                final String courtCenterName,
                final String courtRoomName,
                final UUID courtRoomId,
                final String hearingType) {
            this.caseUrn = caseUrn;
            this.courtCenterId = courtCenterId;
            this.courtCenterName = courtCenterName;
            this.courtRoomName = courtRoomName;
            this.hearingType = hearingType;
            this.courtRoomId = courtRoomId;
        }

        public String getCaseUrn() {
            return this.caseUrn;
        }

        public UUID getCourtCenterId() {
            return this.courtCenterId;
        }

        public String getCourtCenterName() {
            return this.courtCenterName;
        }

        public String getCourtRoomName() {
            return this.courtRoomName;
        }

        public UUID getCourtRoomId() {
            return this.courtRoomId;
        }

        public String getHearingType() {
            return this.hearingType;
        }
    }

    private InitiateHearingCommand getInitiateHearingCommand(final UUID caseId,
                                                             final Hearing hearing) {
        final InitiateHearingCommand command = new InitiateHearingCommand();
        command.setHearingId(hearing.getId());
        command.setCaseId(caseId);
        command.setCourtCentreId(hearing.getCourtCentreId() == null ? null : fromString(hearing.getCourtCentreId()));
        command.setRoomId(hearing.getCourtRoomId() == null ? null : fromString(hearing.getCourtRoomId()));
        command.setCourtCentreName(hearing.getCourtCentreName());
        command.setRoomName(hearing.getCourtRoomName());
        command.setDuration(hearing.getEstimateMinutes());
        command.setHearingType(hearing.getType());
        command.setStartDateTime(hearing.getStartDateTime());
        if (hearing.getJudge() != null) {
            command.setJudgeId(hearing.getJudge().getId());
            command.setJudgeFirstName(hearing.getJudge().getFirstName());
            command.setJudgeLastName(hearing.getJudge().getLastName());
            command.setJudgeTitle(hearing.getJudge().getTitle());
        }
        return command;
    }

    private InitiateHearingCommand getInitiateHearingCommand(final UUID caseId,
                                                             final uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing originatingHearing,
                                                             UUID newHearingId, LocalDate convictionDate) {
        //this is actually a create hearing command - the hearing may be historical
        final InitiateHearingCommand command = new InitiateHearingCommand();
        command.setHearingId(newHearingId);
        command.setCaseId(caseId);
        command.setCourtCentreId(originatingHearing.getCourtCentreId() == null ? null : fromString(originatingHearing.getCourtCentreId()));
        command.setCourtCentreName(originatingHearing.getCourtCentreName());
        command.setDuration(DEFAULT_HEARING_DURATION_MINUTES);
        command.setHearingType(originatingHearing.getType());
        command.setStartDateTime(convictionDate.atStartOfDay(ZoneOffset.UTC));
        return command;
    }

}
