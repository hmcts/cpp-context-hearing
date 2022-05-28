package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubStagingEnforcementOutstandingFines;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.test.utils.core.http.RequestParams;
import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
@NotThreadSafe
public class DefendantOutstandingFinesIT extends AbstractIT {

    @Before
    public void setUp() {
        super.setUpPerTest();
    }

    @Test
    public void should_NOT_get_outstanding_fines_when_defendant_id_is_unknown() throws Exception {
        UUID unknownDefendantId = randomUUID();
        RequestParams build = requestParams(getURL("hearing.defendant.outstanding-fines", unknownDefendantId),
                "application/vnd.hearing.defendant.outstanding-fines+json")
                                      .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()
                                      ).build();

        ResponseData responseData = makeRequest(build);
        Assert.assertThat(responseData.getStatus(), is(OK));
        Assert.assertTrue(new JSONObject(responseData.getPayload()).isNull("outstandingFines)"));
    }

    @Test
    public void should_get_outstanding_files() throws Exception {
        stubStagingEnforcementOutstandingFines();
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        final Defendant addNewDefendant = standardInitiateHearingTemplate().getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        addNewDefendant.setAssociatedPersons(asList(associatedPerson(defaultArguments()).build()));
        addNewDefendant.setProsecutionCaseId(hearingOne.getFirstDefendantForFirstCase().getProsecutionCaseId());
        UseCases.addDefendant(addNewDefendant);

        poll(requestParams(getURL("hearing.defendant.outstanding-fines", addNewDefendant.getId()),
                "application/vnd.hearing.defendant.outstanding-fines+json")
                     .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.outstandingFines.[0].defendantName", is("Abbie ARMSTRONG")),
                                withJsonPath("$.outstandingFines.[0].dateOfBirth", is("1980-11-06"))
                        )));
    }

    private static ResponseData makeRequest(RequestParams requestParams) {
        Response response = new RestClient().query(requestParams.getUrl(), requestParams.getMediaType(), requestParams.getHeaders());
        String responseData = (String) response.readEntity(String.class);
        return new ResponseData(Response.Status.fromStatusCode(response.getStatus()), responseData, response.getHeaders());
    }

}