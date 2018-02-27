package uk.gov.moj.cpp.hearing.it;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

public class OffencesIT extends AbstractIT {


    @Before
    public void setup() {

    }

    @Test
    public void hearingGetOffenceViewTest() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();
        final String pleaId = randomUUID().toString();
        final String pleaValue = "NOT GUILTY";
        final String originalPleaDateString = "2017-02-01";
        final String offenceId = randomUUID().toString();
        final String defendantId = randomUUID().toString();
        final String personId = "d3a0d0f9-78b0-47c6-a362-5febf0485d0f";
        final String verdictId = randomUUID().toString();
        final String verdictDate = LocalDate.now().toString();
        final String verdictValue = "GUILTY";

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        String pleaJson = getStringFromResource("hearing.update-plea.json")
                .replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_PLEA_ID", pleaId)
                .replace("PLEA_VALUE", pleaValue)
                .replace("PLEA_DATE", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID", offenceId)
                .replace("RANDOM_DEFENDANT_ID", defendantId);


        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(pleaJson).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));


        final String verditsJson = getStringFromResource("hearing.update-verdict.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_VERDICT_ID", verdictId)
                .replace("VERDICT_VALUE", verdictValue)
                .replace("VERDICT_DATE", verdictDate)
                .replace("RANDOM_OFFENCE_ID", offenceId)
                .replace("RANDOM_DEFENDANT_ID", defendantId)
                .replace("RANDOM_PERSON_ID", personId);

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-verdict+json")
                .body(verditsJson).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.offences"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.offences+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.offences", IsCollectionWithSize.hasSize(1)),
                                withJsonPath("$.offences[0].caseId", equalTo(caseId)),
                                withJsonPath("$.offences[0].defendantId", equalTo(defendantId)),
                                withJsonPath("$.offences[0].offenceId", equalTo(offenceId)),
                                withJsonPath("$.offences[0].personId", equalTo(personId)),
                                withJsonPath("$.offences[0].plea.pleaId", equalTo(pleaId)),
                                withJsonPath("$.offences[0].plea.value", equalTo("NOT GUILTY")),
                                withJsonPath("$.offences[0].plea.pleaDate", equalTo(originalPleaDateString)),
                                withJsonPath("$.offences[0].verdict.verdictId", equalTo(verdictId)),
                                withJsonPath("$.offences[0].verdict.value", equalTo("GUILTY")),
                                withJsonPath("$.offences[0].verdict.verdictDate", equalTo(verdictDate))

                        )));

    }

}
