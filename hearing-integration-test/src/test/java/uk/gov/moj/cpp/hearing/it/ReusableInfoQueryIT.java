package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.getQueryReusableInfoUrl;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubAzure;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

public class ReusableInfoQueryIT extends AbstractIT{

    @BeforeClass
    public static void setupPerClass() {
        UUID userId = randomUUID();
        setupAsAuthorisedUser(userId);
        stubAzure();
    }

    @Test
   public void shouldNotReturnReusableInfoForMajorCreditor(){

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        Queries.getHearingPollForMatch(hearingOne.getHearing().getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        poll(requestParams(getQueryReusableInfoUrl(hearingOne.getHearingId()), "application/vnd.hearing.query.reusable-info+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.reusablePrompts", hasSize(2)),
                                withJsonPath("$.reusablePrompts[0].promptRef", is("defendantDrivingLicenceNumber")),
                                withJsonPath("$.reusablePrompts[1].promptRef", is("defendantDrivingLicenceNumber"))
                        ))
                );
    }

    @Test
    public void shouldReturnReusableInfoForMinorCreditor(){

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearingWithNsp(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID caseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();
        Queries.getHearingPollForMatch(hearingOne.getHearing().getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        poll(requestParams(getQueryReusableInfoUrl(hearingOne.getHearingId()), "application/vnd.hearing.query.reusable-info+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.reusablePrompts", hasSize(3)),
                                withJsonPath("$.reusablePrompts[0].promptRef", is("defendantDrivingLicenceNumber")),
                                withJsonPath("$.reusablePrompts[1].promptRef", is("minorcreditornameandaddress")),
                                withJsonPath("$.reusablePrompts[1].caseId", is(caseId.toString())),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressOrganisationName", is("ProsecutionAuthorityName")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressAddress1", is("line 1")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressAddress2", is("line 2")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressAddress3", is("line 3")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressAddress4", is("line 4")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressAddress5", is("line 5")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressPostCode", is("E14 4EX")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressEmailAddress1", is("contact@cpp.co.uk")),
                                withJsonPath("$.reusablePrompts[1].value.minorcreditornameandaddressCategoryCode", is("Charity")),
                                withJsonPath("$.reusablePrompts[2].promptRef", is("defendantDrivingLicenceNumber"))
                        ))
                );
    }


    @Test
    public void shouldReturnReusableInfoForApplication(){

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate(),true, true, true, true, false));

        Queries.getHearingPollForMatch(hearingOne.getHearing().getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        poll(requestParams(getQueryReusableInfoUrl(hearingOne.getHearingId()), "application/vnd.hearing.query.reusable-info+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.reusablePrompts", hasSize(3)),
                                withJsonPath("$.reusablePrompts[0].promptRef", is("defendantDrivingLicenceNumber")),
                                withJsonPath("$.reusablePrompts[1].promptRef", is("defendantDrivingLicenceNumber")),
                                withJsonPath("$.reusablePrompts[1].value", is("")),
                                withJsonPath("$.reusablePrompts[2].promptRef", is("defendantDrivingLicenceNumber")),
                                withJsonPath("$.reusablePrompts[2].value", is("DVLA12345"))
                        ))
                );
    }
}
