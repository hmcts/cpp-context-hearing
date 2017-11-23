package uk.gov.moj.cpp.hearing.event;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.io.IOException;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.google.common.io.Resources;
import org.junit.Test;

/**
 * Created by jchondig on 16/11/2017.
 */
public class ListingCaseToAddHearingConverterTest {

    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_HEARING_ID = "id";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";

    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_ESTIMATE_MINUTES = "estimateMinutes";
    private static final String FIELD_COURT_ROOM_ID = "courtRoomId";
    private static final String FIELD_COURT_ROOM_NAME = "courtRoomName";
    private static final String OBJECT_HEARING = "hearing";
    private static final String CASE_ID = UUID.randomUUID().toString();
    private static final String HEARING_ID = UUID.randomUUID().toString();
    private static final String TYPE = "TRIAL";
    private static final String START_DATE_TIME = "2017-11-11T10:00Z";
    private static final int DURATION = 15;



    @Test
    public void transformListingCaseTest(){
        //Given
        final JsonEnvelope jsonEnvelope = getJsonEnvelope();

        //When
        final JsonObject jsonObject = ListingCaseToAddHearingConverter.transformListingCase(jsonEnvelope);

        //Then
        final JsonArray hearings = jsonObject.getJsonArray("hearings");
        assertThat(jsonObject.getString(FIELD_CASE_ID),is(CASE_ID));
        assertThat(hearings.getJsonObject(0).getString(FIELD_HEARING_ID),is(HEARING_ID));
        assertThat(hearings.getJsonObject(0).getString(FIELD_START_DATE_TIME), is(START_DATE_TIME));
        assertThat(hearings.getJsonObject(0).getInt("duration"), is(DURATION));
    }

    @Test
    public void transformListingCaseTestAllFields() throws Exception {
        //Given
        final String caseId = UUID.randomUUID().toString();
        final String hearingId = UUID.randomUUID().toString();
        final String userId = UUID.randomUUID().toString();
        final JsonObject publicHearingAddedPayload = getSendCaseForListingPayload("public.hearing-added.json",
                caseId, hearingId);
        final Metadata metadata = metadataWithRandomUUID("public.hearing-added.json").build();
        final JsonEnvelope jsonEnvelope = new DefaultJsonEnvelope(metadata, publicHearingAddedPayload);
        //When
        final JsonObject jsonObject = ListingCaseToAddHearingConverter.transformListingCase(jsonEnvelope);
        //Then
        assertThat(jsonObject.getString(FIELD_CASE_ID), is(caseId));
        final JsonArray hearings = jsonObject.getJsonArray("hearings");
        assertThat(hearings.getJsonObject(0).getString(FIELD_HEARING_ID), is(hearingId));
        assertThat(hearings.getJsonObject(0).getString(FIELD_COURT_CENTRE_ID), is(publicHearingAddedPayload.getJsonObject(OBJECT_HEARING).getString(FIELD_COURT_CENTRE_ID)));
        assertThat(hearings.getJsonObject(0).getString(FIELD_COURT_CENTRE_NAME), is(publicHearingAddedPayload.getJsonObject(OBJECT_HEARING).getString(FIELD_COURT_CENTRE_NAME)));
        assertThat(fromJsonString(hearings.getJsonObject(0).getJsonString(FIELD_START_DATE_TIME)), is(fromJsonString(publicHearingAddedPayload.getJsonObject(OBJECT_HEARING).getJsonString(FIELD_START_DATE_TIME))));
        assertThat(hearings.getJsonObject(0).getInt("duration"), is(publicHearingAddedPayload.getJsonObject(OBJECT_HEARING).getInt("estimateMinutes")));
        assertThat(hearings.getJsonObject(0).getString("roomId"), is(publicHearingAddedPayload.getJsonObject(OBJECT_HEARING).getString("courtRoomId")));
        assertThat(hearings.getJsonObject(0).getString("roomName"), is(publicHearingAddedPayload.getJsonObject(OBJECT_HEARING).getString("courtRoomName")));
    }
    private JsonEnvelope getJsonEnvelope() {
        final JsonObjectBuilder hearing = createObjectBuilder();
        hearing.add(FIELD_HEARING_ID, HEARING_ID);
        hearing.add(FIELD_TYPE, TYPE);
        hearing.add(FIELD_START_DATE_TIME, START_DATE_TIME);
        hearing.add(FIELD_ESTIMATE_MINUTES, DURATION);

        final JsonObject jsonObject = createObjectBuilder().add(FIELD_CASE_ID, CASE_ID)
                .add(OBJECT_HEARING, hearing).build();

        final Metadata metadata = metadataWithDefaults().build();
        return new DefaultJsonEnvelope(metadata, jsonObject);

    }


    private JsonObject getSendCaseForListingPayload(final String resource, final String caseId, final String hearingId) throws IOException {
        String sendCaseForListingEventPayloadString = getStringFromResource(resource);
        sendCaseForListingEventPayloadString = sendCaseForListingEventPayloadString.replace("RANDOM_CASE_ID", caseId);
        sendCaseForListingEventPayloadString = sendCaseForListingEventPayloadString.replace("RANDOM_HEARING_ID", hearingId);
        return new StringToJsonObjectConverter().convert(sendCaseForListingEventPayloadString);
    }

    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path),
                defaultCharset());
    }
}
