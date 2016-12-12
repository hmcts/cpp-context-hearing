package uk.gov.moj.cpp.hearing.command.handler.converter;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.domain.command.AddCaseToHearing;
import uk.gov.moj.cpp.hearing.domain.command.AllocateCourt;
import uk.gov.moj.cpp.hearing.domain.command.BookRoom;
import uk.gov.moj.cpp.hearing.domain.command.EndHearing;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;
import uk.gov.moj.cpp.hearing.domain.command.StartHearing;

import java.time.ZonedDateTime;

import javax.json.JsonObject;

public class JsonToHearingConverter {

    public static final String HEARING_ID = "hearingId";
    private static final String COURT_CENTRE_NAME = "courtCentreName";
    private static final String ROOM_NAME = "roomName";
    private static final String START_DATE_TIME = "startDateTime";
    private static final String LOCAL_TIME = "localTime";
    private static final String CASE_ID = "caseId";
    private static final String HEARING_TYPE = "hearingType";


    public InitiateHearing convertToInitiateHearing(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        ZonedDateTime startDateTime = ZonedDateTimes.fromJsonString(payload.getJsonString(START_DATE_TIME));
        InitiateHearing initiateHearing = new InitiateHearing(JsonObjects.getUUID(payload, HEARING_ID).get(),
                startDateTime, payload.getInt("duration"), payload.getString(HEARING_TYPE, null));
        initiateHearing.setCourtCentreName(payload.getString(COURT_CENTRE_NAME, null));
        initiateHearing.setRoomName(payload.getString(ROOM_NAME, null));
        initiateHearing.setCaseId(JsonObjects.getUUID(payload, CASE_ID).orElse(null));
        return initiateHearing;
    }

    public StartHearing convertToStartHearing(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        ZonedDateTime startTime = ZonedDateTimes.fromJsonString(payload.getJsonString(LOCAL_TIME));
        return new StartHearing(JsonObjects.getUUID(payload, HEARING_ID).get(), startTime);
    }

    public EndHearing convertToEndHearing(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        ZonedDateTime endTime = ZonedDateTimes.fromJsonString(payload.getJsonString(LOCAL_TIME));
        return new EndHearing(JsonObjects.getUUID(payload, HEARING_ID).get(), endTime);
    }

    public AllocateCourt convertToAllocateCourt(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        return new AllocateCourt(JsonObjects.getUUID(payload, HEARING_ID).get(), payload.getString(COURT_CENTRE_NAME));
    }

    public BookRoom convertToBookRoom(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        return new BookRoom(JsonObjects.getUUID(payload, HEARING_ID).get(), payload.getString(ROOM_NAME));
    }

    public AddCaseToHearing convertToAddCase(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        return new AddCaseToHearing(JsonObjects.getUUID(payload, HEARING_ID).get(), JsonObjects.getUUID(payload, CASE_ID).get());
    }
}
