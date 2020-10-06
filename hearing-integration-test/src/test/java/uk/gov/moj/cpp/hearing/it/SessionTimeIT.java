package uk.gov.moj.cpp.hearing.it;

import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SessionTimeIT extends AbstractIT {

    @Test
    public void shouldSaveSessionTimeForFullUpdate() throws Exception {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final UUID courtAssociateId = randomUUID();
        final UUID courtClerkId = randomUUID();
        final UUID legalAdviserId = randomUUID();
        final UUID jud1 = randomUUID();
        final UUID jud2 = randomUUID();
        final UUID jud3 = randomUUID();
        final String jud1Name = STRING.next();
        final String jud2Name = STRING.next();
        final String jud3Name = STRING.next();
        final boolean chair1 = true;
        final boolean chair2 = false;
        final boolean chair3 = false;
        final UUID courtHouseId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final LocalDate courtSessionDate = LocalDate.of(2020, 12, 12);
        saveSessionTime(courtHouseId, courtRoomId, courtSessionDate, courtAssociateId, courtClerkId, legalAdviserId, jud1, jud2, jud3, jud1Name, jud2Name, jud3Name, chair1, chair2, chair3);
        final String payload = poll(requestParams(getURL("hearing.query.session-time", courtHouseId, courtRoomId)+"?courtSessionDate="+ courtSessionDate.toString(),
                "application/vnd.hearing.query.session-time+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print()
                ).getPayload();
        final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
        final JsonObject sessionTime = stringToJsonObjectConverter.convert(payload);
        assertThat(sessionTime.getString("courtHouseId"), is(courtHouseId.toString()));
        assertThat(sessionTime.getString("courtRoomId"), is(courtRoomId.toString()));
        final JsonObject amCourtSession = sessionTime.getJsonObject("amCourtSession");

        assertThat(amCourtSession.getString("startTime"), is("10:00"));
        assertThat(amCourtSession.getString("endTime"), is("13:00"));

        verifyCourtSession(courtAssociateId, courtClerkId, legalAdviserId, jud1, jud2, jud3, jud1Name, jud2Name, jud3Name, chair1, chair2, chair3, amCourtSession);

        final JsonObject pmCourtSession = sessionTime.getJsonObject("pmCourtSession");

        assertThat(pmCourtSession.getString("startTime"), is("14:00"));
        assertThat(pmCourtSession.getString("endTime"), is("18:00"));

        verifyCourtSession(courtAssociateId, courtClerkId, legalAdviserId, jud1, jud2, jud3, jud1Name, jud2Name, jud3Name, chair1, chair2, chair3, pmCourtSession);
    }

    @Test
    public void shouldThrowExceptionWhenInvalidQueryRequest() throws Exception {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        saveSessionTime(randomUUID(), randomUUID(), LocalDate.now(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), STRING.next(), STRING.next(), STRING.next(), true, false, false);
        final String payload = poll(requestParams(getURL("hearing.query.session-time", randomUUID(), randomUUID()),
                "application/vnd.hearing.query.session-time+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(NOT_FOUND),
                        print()
                ).getPayload();
        assertEquals("", payload);
    }

    @Test
    public void shouldSaveSessionTimeForPartialUpdate() throws Exception {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final UUID jud1 = randomUUID();
        final String jud1Name = STRING.next();
        final boolean chair1 = true;
        final UUID jud2 = randomUUID();
        final String jud2Name = STRING.next();
        final boolean chair2 = false;
        final UUID courtHouseId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final LocalDate courtSessionDate = LocalDate.now();

        final JsonObjectBuilder recordSessionTime1 = Json.createObjectBuilder();
        recordSessionTime1.add("courtHouseId", courtHouseId.toString());
        recordSessionTime1.add("courtRoomId", courtRoomId.toString());
        recordSessionTime1.add("courtSessionDate", courtSessionDate.toString());

        final JsonObjectBuilder courtSession = Json.createObjectBuilder();
        final JsonArrayBuilder judiciaryList = Json.createArrayBuilder();
        judiciaryList.add(judiciary(jud1, jud1Name, chair1));
        judiciaryList.add(judiciary(jud2, jud2Name, chair2));
        courtSession.add("judiciaries", judiciaryList);

        recordSessionTime1.add("amCourtSession", courtSession);
        updateSessionTimeCommand(recordSessionTime1.build());

        final String payloadAfter1 = poll(requestParams(getURL("hearing.query.session-time", courtHouseId, courtRoomId)+"?courtSessionDate="+ courtSessionDate.toString(),
                "application/vnd.hearing.query.session-time+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print()
                ).getPayload();
        final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
        final JsonObject sessionTime = stringToJsonObjectConverter.convert(payloadAfter1);
        assertThat(sessionTime.getString("courtHouseId"), is(courtHouseId.toString()));
        assertThat(sessionTime.getString("courtRoomId"), is(courtRoomId.toString()));
        final JsonObject amCourtSession = sessionTime.getJsonObject("amCourtSession");
        verifyPartialCourtSession(jud1, jud1Name, chair1, amCourtSession);

        //Second Request
        final JsonObjectBuilder recordSessionTime2 = Json.createObjectBuilder();
        recordSessionTime2.add("courtHouseId", courtHouseId.toString());
        recordSessionTime2.add("courtRoomId", courtRoomId.toString());
        recordSessionTime2.add("courtSessionDate", courtSessionDate.toString());


        final JsonObjectBuilder amCourtSession2 = Json.createObjectBuilder();
        final JsonArrayBuilder judiciaryList2 = Json.createArrayBuilder();
        judiciaryList2.add(judiciary(jud2, jud2Name, chair2));
        amCourtSession2.add("judiciaries", judiciaryList2);
        recordSessionTime2.add("amCourtSession", amCourtSession2);

        final UUID jud3pm = randomUUID();
        final String jud3Name = STRING.next();
        final boolean chair3 = false;

        final JsonObjectBuilder pmCourtSession2 = Json.createObjectBuilder();
        final JsonArrayBuilder judiciaryList3 = Json.createArrayBuilder();
        judiciaryList3.add(judiciary(jud3pm, jud3Name, chair3));
        pmCourtSession2.add("judiciaries", judiciaryList3);
        recordSessionTime2.add("pmCourtSession", pmCourtSession2);

        updateSessionTimeCommand(recordSessionTime2.build());

        final String payloadAfter2 = poll(requestParams(getURL("hearing.query.session-time", courtHouseId, courtRoomId)+"?courtSessionDate="+ courtSessionDate.toString(),
                "application/vnd.hearing.query.session-time+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print()
                ).getPayload();

        final JsonObject sessionTimeRetrieved2 = stringToJsonObjectConverter.convert(payloadAfter2);
        assertThat(sessionTimeRetrieved2.getString("courtHouseId"), is(courtHouseId.toString()));
        assertThat(sessionTimeRetrieved2.getString("courtRoomId"), is(courtRoomId.toString()));
        final JsonObject amCourtSessionBackAfter2 = sessionTimeRetrieved2.getJsonObject("amCourtSession");
        final JsonObject pmCourtSessionBackAfter2 = sessionTimeRetrieved2.getJsonObject("pmCourtSession");

        verifyPartialCourtSession(jud2, jud2Name, chair2, amCourtSessionBackAfter2);
        verifyPartialCourtSession(jud3pm, jud3Name, chair3, pmCourtSessionBackAfter2);
    }


    private void saveSessionTime(final UUID courtHouseId,
                                 final UUID courtRoomId,
                                 final LocalDate courtSessionDate,
                                 final UUID courtAssociateId,
                                 final UUID courtClerkId,
                                 final UUID legalAdviserId,
                                 final UUID jud1,
                                 final UUID jud2,
                                 final UUID jud3,
                                 final String jud1Name,
                                 final String jud2Name,
                                 final String jud3Name,
                                 final boolean chair1,
                                 final boolean chair2,
                                 final boolean chair3
    ) {
        final JsonObject command = recordSessionTime(courtHouseId, courtRoomId, courtSessionDate, courtAssociateId, courtClerkId, legalAdviserId, jud1, jud2, jud3, jud1Name, jud2Name, jud3Name, chair1, chair2, chair3);
        updateSessionTimeCommand(command);
    }

    private void updateSessionTimeCommand(final JsonObject command) {
        final String commandUrl = ENDPOINT_PROPERTIES.getProperty("hearing.command.record-session-time");
        final Response response = given().spec(getRequestSpec())
                .and().contentType("application/vnd.hearing.record-session-time+json")
                .and().header(USER_ID, getLoggedInUser())
                .and().body(command.toString())
                .when().post(commandUrl)
                .then().extract().response();
        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));
    }


    private void verifyCourtSession(UUID courtAssociateId, UUID courtClerkId, UUID legalAdviserId, UUID jud1, UUID jud2, UUID jud3, String jud1Name, String jud2Name, String jud3Name, boolean chair1, boolean chair2, boolean chair3, JsonObject amCourtSession) {

        assertThat(amCourtSession.getString("courtAssociateId"), is(courtAssociateId.toString()));
        assertThat(amCourtSession.getString("courtClerkId"), is(courtClerkId.toString()));
        assertThat(amCourtSession.getString("legalAdviserId"), is(legalAdviserId.toString()));

        final JsonArray judiciariesAm = amCourtSession.getJsonArray("judiciaries");

        assertThat(judiciariesAm.getJsonObject(0).getString("judiciaryId"), is(jud1.toString()));
        assertThat(judiciariesAm.getJsonObject(1).getString("judiciaryId"), is(jud2.toString()));
        assertThat(judiciariesAm.getJsonObject(2).getString("judiciaryId"), is(jud3.toString()));

        assertThat(judiciariesAm.getJsonObject(0).getString("judiciaryName"), is(jud1Name.toString()));
        assertThat(judiciariesAm.getJsonObject(1).getString("judiciaryName"), is(jud2Name.toString()));
        assertThat(judiciariesAm.getJsonObject(2).getString("judiciaryName"), is(jud3Name.toString()));

        assertThat(judiciariesAm.getJsonObject(0).getBoolean("benchChairman"), is(chair1));
        assertThat(judiciariesAm.getJsonObject(1).getBoolean("benchChairman"), is(chair2));
        assertThat(judiciariesAm.getJsonObject(2).getBoolean("benchChairman"), is(chair3));
    }

    private void verifyPartialCourtSession(final UUID jud1,
                                           final String jud1Name,
                                           final boolean chair1,
                                           JsonObject amCourtSession) {
        final JsonArray judiciariesAm = amCourtSession.getJsonArray("judiciaries");
        assertThat(judiciariesAm.getJsonObject(0).getString("judiciaryId"), is(jud1.toString()));
        assertThat(judiciariesAm.getJsonObject(0).getString("judiciaryName"), is(jud1Name));
        assertThat(judiciariesAm.getJsonObject(0).getBoolean("benchChairman"), is(chair1));
    }
    private JsonObject recordSessionTime(final UUID courtHouseId, final UUID courtRoomId,
                                         final LocalDate courtSessionDate, final UUID courtAssociateId,
                                         final UUID courtClerkId,
                                         final UUID legalAdviserId,
                                         final UUID jud1,
                                         final UUID jud2,
                                         final UUID jud3,
                                         final String jud1Name,
                                         final String jud2Name,
                                         final String jud3Name,
                                         final boolean chair1,
                                         final boolean chair2,
                                         final boolean chair3
    ) {

        final JsonObjectBuilder recordSessionTime = Json.createObjectBuilder();
        recordSessionTime.add("courtHouseId", courtHouseId.toString());
        recordSessionTime.add("courtRoomId", courtRoomId.toString());
        recordSessionTime.add("courtSessionDate", courtSessionDate.toString());
        recordSessionTime.add("amCourtSession", courtSession("10:00", "13:00", courtAssociateId, courtClerkId, legalAdviserId, jud1, jud2, jud3, jud1Name, jud2Name, jud3Name, chair1, chair2, chair3));
        recordSessionTime.add("pmCourtSession", courtSession("14:00", "18:00", courtAssociateId, courtClerkId, legalAdviserId, jud1, jud2, jud3, jud1Name, jud2Name, jud3Name, chair1, chair2, chair3));

        return recordSessionTime.build();
    }

    private JsonObject courtSession(final String startTime, final String endTime,
                                    final UUID courtAssociateId,
                                    final UUID courtClerkId,
                                    final UUID legalAdviserId,
                                    final UUID jud1,
                                    final UUID jud2,
                                    final UUID jud3,
                                    final String judName1,
                                    final String judName2,
                                    final String judName3,
                                    final boolean chair1,
                                    final boolean chair2,
                                    final boolean chair3
    ) {

        final JsonObjectBuilder courtSession = Json.createObjectBuilder();
        courtSession.add("courtAssociateId", courtAssociateId.toString());
        courtSession.add("courtClerkId", courtClerkId.toString());
        courtSession.add("legalAdviserId", legalAdviserId.toString());
        courtSession.add("startTime", startTime);
        courtSession.add("endTime", endTime);

        final JsonArrayBuilder judiciaryList = Json.createArrayBuilder();
        judiciaryList.add(judiciary(jud1, judName1, chair1));
        judiciaryList.add(judiciary(jud2, judName2, chair2));
        judiciaryList.add(judiciary(jud3, judName3, chair3));
        courtSession.add("judiciaries", judiciaryList);

        return courtSession.build();
    }

    private JsonObject judiciary(final UUID judId, final String jud1Name, final boolean chair) {
        final JsonObjectBuilder judiciary = Json.createObjectBuilder();
        judiciary.add("benchChairman", chair);
        judiciary.add("judiciaryId", judId.toString());
        judiciary.add("judiciaryName", jud1Name);
        return judiciary.build();
    }

}
