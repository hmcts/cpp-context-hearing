package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.System.out;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.Test;

import com.jayway.restassured.response.Response;

import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;

@SuppressWarnings("unchecked")
public class NewOffencePleaUpdateIT extends AbstractIT {

    @Test
    public void updateOffencePleaTest() throws IOException {

        final InitiateHearingCommand initiateHearingCommand = UseCases.initiateHearingMultipleDefendants(requestSpec, 1);
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        out.println("updateOffencePleaTest hearingId: " + hearingId);

        final Defendant defendant = initiateHearingCommand.getHearing().getDefendants().get(0);
        final Offence offence = defendant.getOffences().get(0);
        final UUID caseId = offence.getCaseId();
        final String pleaValue = "GUILTY";
        final LocalDate pleaDate = LocalDate.now();
        
        final String hearingDetailsQueryURL = getURL("hearing.get.hearing.v2", hearingId);

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json")).until(
                status().is(OK),
                payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                        withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                        withJsonPath("$.cases[0].defendants[0].defendantId", is(defendant.getId().toString())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offence.getId().toString())),
                        hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea"),
                        hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate"),
                        hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.value"))));

        final String updatePleaCommandURL = getURL("hearing.update-plea", hearingId);
        final String updatePleaPayload = getJsonBody(caseId, defendant.getId(), offence.getId(), pleaValue, pleaDate);

        final Response response = given().spec(requestSpec).and().contentType("application/vnd.hearing.update-plea+json")
                .body(updatePleaPayload).header(CPP_UID_HEADER).when().post(updatePleaCommandURL).then().extract().response();

        assertThat(response.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        
        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json")).until(
                status().is(OK),
                payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                        withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                        withJsonPath("$.cases[0].defendants[0].defendantId", is(defendant.getId().toString())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offence.getId().toString())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaDate)),
                        withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", is(pleaValue)))));
    }

    private String getJsonBody(final UUID caseId, final UUID defendentId, final UUID offenceId, final String value,
            final LocalDate localDate) throws IOException {
        String json = getStringFromResource("hearing.update-plea.json");
        json = json.replace("RANDOM_CASE_ID", caseId.toString());
        json = json.replace("RANDOM_DEFENDANT_ID", defendentId.toString());
        json = json.replace("RANDOM_OFFENCE_ID", offenceId.toString());
        json = json.replace("RANDOM_PLEA_ID", randomUUID().toString());
        json = json.replace("PLEA_VALUE", value);
        return json = json.replace("PLEA_DATE", ISO_LOCAL_DATE.format(localDate));
    }
}