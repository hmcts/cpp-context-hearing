package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;

public class ListingCaseToAddHearingConverter {

    private ListingCaseToAddHearingConverter() {

    }

    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_HEARING_ID = "id";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_START_DATE_TIME = "startDateTime";


    public static JsonObject transformListingCase(final JsonEnvelope event) {
        final JsonObject hearingJsonObject = event.payloadAsJsonObject().getJsonObject("hearing");
        final String caseId = event.payloadAsJsonObject().getString(FIELD_CASE_ID);
        final JsonObjectBuilder jsonBuilder = createObjectBuilder();
        jsonBuilder.add(FIELD_CASE_ID, caseId);
        jsonBuilder.add("hearings", getHearingFromPayload(hearingJsonObject));
        return jsonBuilder.build();
    }

    private static JsonArray getHearingFromPayload(final JsonObject hearingJsonObject) {
        final JsonArrayBuilder hearings = createArrayBuilder();
        final JsonObjectBuilder hearing = createObjectBuilder();
        hearing.add(FIELD_HEARING_ID, hearingJsonObject.getString(FIELD_HEARING_ID));
        hearing.add(FIELD_START_DATE_TIME, fromJsonString(hearingJsonObject.getJsonString(FIELD_START_DATE_TIME)).toString());
        final String courtCentreId = hearingJsonObject.getString(FIELD_COURT_CENTRE_ID, null);
        final String courtCentreName = hearingJsonObject.getString(FIELD_COURT_CENTRE_NAME, null);
        final String courtRoomId = hearingJsonObject.getString("courtRoomId", null);
        final String courtRoomName = hearingJsonObject.getString("courtRoomName", null);
        if (courtCentreId != null) {
            hearing.add(FIELD_COURT_CENTRE_ID, courtCentreId);
        }
        if (courtCentreName != null) {
            hearing.add(FIELD_COURT_CENTRE_NAME, courtCentreName);
        }
        if (courtRoomId != null) {
            hearing.add("roomId", courtRoomId);
        }
        if (courtRoomName != null) {
            hearing.add("roomName", courtRoomName);
        }
        hearing.add("hearingType", hearingJsonObject.getString("type"));
        hearing.add("duration", hearingJsonObject.getInt("estimateMinutes"));
        hearings.add(hearing.build());
        return hearings.build();
    }

}
