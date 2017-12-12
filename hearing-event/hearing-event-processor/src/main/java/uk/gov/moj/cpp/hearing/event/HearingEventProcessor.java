package uk.gov.moj.cpp.hearing.event;

import static java.lang.String.format;
import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.listing.Hearing;
import uk.gov.moj.cpp.hearing.event.command.InitiateHearingCommand;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventProcessor.class);

    private static final String PUBLIC_HEARING_HEARING_INITIATED = "public.hearing.hearing-initiated";
    private static final String PUBLIC_HEARING_RESULTED = "public.hearing.resulted";
    private static final String PUBLIC_HEARING_RESULT_AMENDED = "public.hearing.result-amended";
    private static final String PUBLIC_HEARING_HEARING_ADJOURNED = "public.hearing.adjourn-date-updated";
    private static final String PUBLIC_HEARING_EVENT_LOGGED = "public.hearing.event-logged";
    private static final String PUBLIC_HEARING_TIMESTAMP_CORRECTED = "public.hearing.event-timestamp-corrected";

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
    private static final String FIELD_COURT_CENTER_NAME = "courtCentreName";
    private static final String FIELD_ROOM_NUMBER = "roomNumber";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_COURT_CENTRE = "courtCentre";
    private static final String FIELD_COURT_CENTER_ID = "courtCentreId";

    private static final String HEARING_QUERY = "hearing.get.hearing";
    private static final String CASE_QUERY = "structure.query.case";
    private static final String HEARING_INITIATE_HEARING = "hearing.initiate-hearing";
    private static final String HEARING_PLEA_ADD = "hearing.plea-add";
    private static final String HEARING_PLEA_CHANGE = "hearing.plea-change";

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_CASE_ID = "caseId";
    public static final String FIELD_URN = "urn";
    public static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    public static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    public static final String FIELD_ROOM_NAME = "roomName";

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

    @Handles("hearing.hearing-initiated")
    public void publishHearingInitiatedPublicEvent(final JsonEnvelope event) {
        final String hearingId = event.payloadAsJsonObject().getString(FIELD_HEARING_ID);
        final JsonObject payload = createObjectBuilder().add(FIELD_HEARING_ID, hearingId).build();
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_HEARING_INITIATED).apply(payload));
    }

    @Handles("hearing.results-shared")
    public void publishHearingResultsSharedPublicEvent(final JsonEnvelope event) {
        LOGGER.debug(format("'public.hearing.resulted' event received %s", event.payloadAsJsonObject()));
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULTED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.adjourn-date-updated")
    public void publishHearingDateAdjournedPublicEvent(final JsonEnvelope event) {
        final String startDate = event.payloadAsJsonObject().getString("startDate");
        final JsonObject payload = createObjectBuilder().add("startDate", startDate).build();
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_HEARING_ADJOURNED).apply(payload));
    }

    @Handles("hearing.result-amended")
    public void publishHearingResultAmendedPublicEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULT_AMENDED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.hearing.confirmed-recorded")
    public void createInitiateHearingCommandFromHearingConfirmedRecorded(final JsonEnvelope event) {
        LOGGER.trace("Received hearing.hearing.confirmed-recorded event, processing");
        final JsonObject payload = event.payloadAsJsonObject();
        final Hearing hearing = this.jsonObjectToObjectConverter.convert(payload.getJsonObject(FIELD_HEARING), Hearing.class);
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));

        final InitiateHearingCommand initiateHearingCommand =
                        getInitiateHearingCommand(caseId, hearing);


        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_INITIATE_HEARING)
                .apply(this.objectToJsonValueConverter.convert(initiateHearingCommand)));;
    }
    @Handles("hearing.case.plea-added")
    public void createHearingPleaAddFromPleaAdded(final JsonEnvelope event) {
        LOGGER.trace("Received plea-added event, processing");
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_PLEA_ADD)
                .apply(event.payloadAsJsonObject()));;

    }
    @Handles("hearing.case.plea-changed")
    public void createHearingPleaChangeFromPleaChanged(final JsonEnvelope event) {
        LOGGER.trace("Received plea-changed event, processing");
        this.sender.send(this.enveloper.withMetadataFrom(event, HEARING_PLEA_CHANGE)
                .apply(event.payloadAsJsonObject()));;

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
        final Optional<HearingDetails> optionalCaseDetails = getHearingDetails(event);

        if (optionalCaseDetails.isPresent()) {
            final HearingDetails hearingDetails = optionalCaseDetails.get();
            final JsonObjectBuilder hearingEventJsonBuilder = createObjectBuilder()
                    .add(FIELD_HEARING_EVENT_ID, hearingEventId)
                    .add(FIELD_RECORDED_LABEL, recordedLabel)
                    .add(FIELD_EVENT_TIME, eventTime)
                    .add(FIELD_LAST_MODIFIED_TIME, lastModifiedTime);
            if (null != lastHearingEventId) {
                hearingEventJsonBuilder.add(FIELD_LAST_HEARING_EVENT_ID, lastHearingEventId);
            }

            final JsonObjectBuilder courtHouseJsonBuilder = createObjectBuilder()
                    .add(FIELD_COURT_CENTER_ID, hearingDetails.getCourtCenterId().toString())
                    .add(FIELD_COURT_CENTER_NAME, hearingDetails.getCourtCenterName())
                    .add(FIELD_ROOM_NUMBER, hearingDetails.getRoomNumber());

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
            LOGGER.error("case urn is null for hearingId {} and hearingEventId {}", event.payloadAsJsonObject().getString(FIELD_HEARING_ID), hearingEventId);
        }
    }

    private Optional<HearingDetails> getHearingDetails(final JsonEnvelope event) {
        Optional<HearingDetails> hearingDetails = Optional.empty();

        final String hearingId = event.payloadAsJsonObject().getString(FIELD_HEARING_ID);
        final JsonEnvelope hearingQuery = enveloper.withMetadataFrom(event, HEARING_QUERY).apply(
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId)
                        .build()
        );

        final JsonObject hearingResponsePayload = this.requester.request(hearingQuery).payloadAsJsonObject();

        if (!hearingResponsePayload.isEmpty()) {
            final String caseId = hearingResponsePayload.getJsonArray("caseIds").getString(0);

            //get caseUrn from case
            final JsonEnvelope caseQuery = this.enveloper.withMetadataFrom(event, CASE_QUERY).apply(
                    createObjectBuilder()
                            .add(FIELD_CASE_ID, caseId)
                            .build()
            );

            try {
                final JsonObject caseResponsePayload = this.requester.request(caseQuery).payloadAsJsonObject();

                if (!caseResponsePayload.isEmpty()) {
                    final String caseUrn = caseResponsePayload.getString(FIELD_URN);
                    final UUID courtCenterId = fromString(hearingResponsePayload.getString(FIELD_COURT_CENTRE_ID));
                    final String courtCenterName = hearingResponsePayload.getString(FIELD_COURT_CENTRE_NAME);
                    final String roomName = hearingResponsePayload.getString(FIELD_ROOM_NAME);
                    final String hearingType = hearingResponsePayload.getString(FIELD_HEARING_TYPE);

                    final HearingDetails details = new HearingDetails(caseUrn, courtCenterId, courtCenterName, roomName, hearingType);
                    hearingDetails = Optional.of(details);
                }
            } catch (final RuntimeException e) {
                LOGGER.error("Could not find case details for case id {}, hearing id {}:", caseId, hearingId, e);
            }
        }
        return hearingDetails;
    }

    class HearingDetails {
        private final String caseUrn;
        private final UUID courtCenterId;
        private final String courtCenterName;
        private final String roomNumber;
        private final String hearingType;

        public HearingDetails(
                final String caseUrn,
                final UUID courtCenterId,
                final String courtCenterName,
                final String roomNumber,
                final String hearingType) {
            this.caseUrn = caseUrn;
            this.courtCenterId = courtCenterId;
            this.courtCenterName = courtCenterName;
            this.roomNumber = roomNumber;
            this.hearingType = hearingType;
        }

        public String getCaseUrn() {
            return caseUrn;
        }

        public UUID getCourtCenterId() {
            return courtCenterId;
        }

        public String getCourtCenterName() {
            return courtCenterName;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public String getHearingType() {
            return hearingType;
        }
    }

    private InitiateHearingCommand getInitiateHearingCommand(final UUID caseId,
                    final Hearing hearing) {
        final InitiateHearingCommand command = new InitiateHearingCommand();
        command.setHearingId(hearing.getId());
        command.setCaseId(caseId);
        command.setCourtCentreId(hearing.getCourtCentreId() == null ? null
                        : UUID.fromString(hearing.getCourtCentreId()));
        command.setRoomId(hearing.getCourtRoomId() == null ? null
                        : UUID.fromString(hearing.getCourtRoomId()));
        command.setCourtCentreName(hearing.getCourtCentreName());
        command.setRoomName(hearing.getCourtRoomName());
        command.setDuration(hearing.getEstimateMinutes());
        command.setHearingType(hearing.getType());
        command.setStartDateTime(hearing.getStartDateTime());
        return command;
    }
}
