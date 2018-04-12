package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.Test;

import com.jayway.restassured.response.Response;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;

@SuppressWarnings("unchecked")
public class NewOffencePleaUpdateIT extends AbstractIT {
    
    private enum PleaValueType {GUILTY, NOT_GUILTY};
    
    @Test
    public void updatePleaToNotGuilty() throws Throwable {
        updatePlea(PleaValueType.NOT_GUILTY);
    }
    
    @Test
    public void updatePleaToGuilty() throws Throwable {
         updatePlea(PleaValueType.GUILTY);
    }

    private static void updatePlea(final PleaValueType pleaValue) throws Throwable {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID caseId = initiateHearingCommand.getCases().get(0).getCaseId();
        final UUID defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        final LocalDate pleaDate = PAST_LOCAL_DATE.next(); 
        
        EventListener eventListener = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        final String hearingDetailsQueryURL = getURL("hearing.get.hearing.v2", hearingId);

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                    status().is(OK),
                    payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                            withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                            withJsonPath("$.cases[0].defendants[0].defendantId", is(defendantId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offenceId.toString())),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea"),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate"),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.value")
        )));

        final String updatePleaCommandURL = getURL("hearing.update-plea", hearingId);
        final String updatePleaPayload = getJsonBody(caseId, defendantId, offenceId, pleaValue.name(), pleaDate);

        final Response response = given().spec(requestSpec).and().contentType("application/vnd.hearing.update-plea+json")
                .body(updatePleaPayload).header(CPP_UID_HEADER).when().post(updatePleaCommandURL).then().extract().response();

        assertThat(response.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .timeout(10, TimeUnit.SECONDS)
                .until(
                    status().is(OK),
                    payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                            withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                            withJsonPath("$.cases[0].defendants[0].defendantId", is(defendantId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offenceId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaDate)),
                            withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", is(pleaValue.name())
        ))));
    }

    private static String getJsonBody(final UUID caseId, final UUID defendentId, final UUID offenceId, final String value,
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
