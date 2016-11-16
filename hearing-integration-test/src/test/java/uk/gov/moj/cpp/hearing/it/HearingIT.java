package uk.gov.moj.cpp.hearing.it;

import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;

import uk.gov.moj.cpp.hearing.helper.StubUtil;

public class HearingIT extends AbstractIT {
    private final UUID userId = randomUUID();
    private final Header cppuidHeader = new Header("CJSCPPUID", userId.toString());

    @Before
    public void setUp() {
        StubUtil.setupUsersGroupDataActionClassificationStub();
    }

    @Test
    public void hearingTest() throws IOException, InterruptedException {
        final String caseId = UUID.randomUUID().toString();
        final String hearingId = UUID.randomUUID().toString();

        final String listHearing = Resources.toString(
                        Resources.getResource("hearing.command.list-hearing.json"),
                        Charset.defaultCharset());

        final String commandAPIEndPoint = prop.getProperty("hearing-command-api-hearings");

        final String listHearingBody = listHearing.replace("RANDOM_HEARING_ID", hearingId)
                        .replace("RANDOM_CASE_ID", caseId);

        final Response writeResponse = given().spec(reqSpec).and()
                        .contentType("application/vnd.hearing.command.list-hearing+json")
                        .body(listHearingBody).header(cppuidHeader).when().post(commandAPIEndPoint)
                        .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        TimeUnit.SECONDS.sleep(10);


        final String queryAPIEndPoint = MessageFormat
                        .format(prop.getProperty("hearing-query-api-hearings"), caseId);

        final Response readResponse = given().spec(reqSpec).and()
                        .accept("application/vnd.hearing.query.hearings+json")
                        .header(cppuidHeader).when().get(queryAPIEndPoint).then().extract()
                        .response();

        assertThat(readResponse.getStatusCode(), is(200));

        final ResponseBody respBody = readResponse.getBody();
        assertThat((ArrayList<HashMap>) respBody.path("hearings"), hasSize(1));
        assertThat(readResponse.jsonPath().getList("hearings.caseId").contains(caseId),
                        equalTo(true));
    }


}
