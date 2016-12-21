package uk.gov.moj.cpp.hearing.it;

import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import uk.gov.moj.cpp.hearing.helper.StubUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.io.Resources;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class HearingIT extends AbstractIT {

    private final UUID userId = UUID.fromString("8959b8b5-92bd-4ada-96f4-7ac9d482671a");
    private final Header cppuidHeader = new Header("CJSCPPUID", userId.toString());

    @BeforeClass
    public static void setUp() {
        StubUtil.setupUsersGroupDataActionClassificationStub();
    }

    @Test
    public void hearingTest() throws IOException, InterruptedException {
        final String caseId = UUID.randomUUID().toString();
        final String hearingId = UUID.randomUUID().toString();

        final String initiateHearing = Resources.toString(
                Resources.getResource("hearing.initiate-hearing.json"),
                Charset.defaultCharset());

        final String commandAPIEndPoint = MessageFormat
                .format(prop.getProperty("hearing.initiate-hearing"), hearingId);

        final String initiateHearingBody = initiateHearing.replace("RANDOM_CASE_ID", caseId);

        Response  writeResponse = given().spec(reqSpec).and()
                .contentType("application/vnd.hearing.start+json")
                .body("{\n" +
                        "  \"localTime\": \"2016-06-01T10:00:00Z\"\n" +
                        "}").header(cppuidHeader).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        writeResponse = given().spec(reqSpec).and()
                .contentType("application/vnd.hearing.end+json")
                .body("{\n" +
                        "  \"localTime\": \"2016-06-01T11:00:00Z\"\n" +
                        "}").header(cppuidHeader).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));


        writeResponse = given().spec(reqSpec).and()
                .contentType("application/vnd.hearing.initiate-hearing+json")
                .body(initiateHearingBody).header(cppuidHeader).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));


        writeResponse = given().spec(reqSpec).and()
                .contentType("application/vnd.hearing.add-case+json")
                .body("{\n" +
                        "  \"caseId\": \"2a2d7e9e-0c60-11e6-a148-3e1d05defe78\"\n" +
                        "}").header(cppuidHeader).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        writeResponse = given().spec(reqSpec).and()
                .contentType("application/vnd.hearing.book-room+json")
                .body("{\n" +
                        "  \"roomName\": \"Room1\"\n" +
                        "}").header(cppuidHeader).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        writeResponse = given().spec(reqSpec).and()
                .contentType("application/vnd.hearing.allocate-court+json")
                .body("{\n" +
                        "  \"courtCentreName\": \"Bournemouth\"\n" +
                        "}").header(cppuidHeader).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));



        TimeUnit.SECONDS.sleep(15);


        final String queryAPIEndPoint = MessageFormat
                .format(prop.getProperty("hearing.get.hearing"), hearingId);

        final Response readResponse = given().spec(reqSpec).and()
                .accept("application/vnd.hearing.get.hearing+json")
                .header(cppuidHeader).when().get(queryAPIEndPoint).then().extract()
                .response();

        assertThat(readResponse.getStatusCode(), is(200));


        /**
         * Test Read store
         */
        assertThat("Case should associated with hearing",readResponse.jsonPath().getList("caseIds").contains(caseId), equalTo(true));
        assertThat("Case should associated with hearing",readResponse.jsonPath().getList("caseIds").contains("2a2d7e9e-0c60-11e6-a148-3e1d05defe78"), equalTo(true));
        assertThat("Hearing ID should match",readResponse.jsonPath().get("hearingId").equals(hearingId), equalTo(true));
        assertThat("HearingType should match",readResponse.jsonPath().get("hearingType").equals("TRIAL"), equalTo(true));
        assertThat("Court Centre name should match",readResponse.jsonPath().get("courtCentreName").equals("Bournemouth"), equalTo(true));
        assertThat("Room name should match",readResponse.jsonPath().get("roomName").equals("Room1"), equalTo(true));
        assertThat("Hearing start Date should match",readResponse.jsonPath().get("startDate").equals("2016-06-01"), equalTo(true));
        assertThat("Hearing Start time should match",readResponse.jsonPath().get("startTime").equals("10:00"), equalTo(true));
        assertThat("Hearing Started time should match",readResponse.jsonPath().get("startedAt").equals("2016-06-01T10:00:00Z"), equalTo(true));
        assertThat("Hearing ended time should match",readResponse.jsonPath().get("endedAt").equals("2016-06-01T11:00:00Z"), equalTo(true));


        final String getHearingsByDate = MessageFormat
                .format(prop.getProperty("hearing.get.hearings-by-startDate"), (String)readResponse.jsonPath().get("startDate"));

        final Response readResponses = given().spec(reqSpec).and()
                .accept("application/vnd.hearing.get.hearings-by-startdate+json")
                .header(cppuidHeader).when().get(getHearingsByDate).then().extract()
                .response();

        assertThat(readResponses.getStatusCode(), is(200));
        assertThat("hearings list size should be greater or equal one",readResponses.jsonPath().getList("hearings").size() >= 1, equalTo(true));

    }

}
