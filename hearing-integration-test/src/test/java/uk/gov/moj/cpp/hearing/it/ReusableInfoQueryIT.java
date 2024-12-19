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
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearingWithNsp;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.getQueryReusableInfoUrl;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.UUID;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ReusableInfoQueryIT extends AbstractIT {

    @BeforeAll
    public static void setupPerClass() {
        UUID userId = randomUUID();
        setupAsAuthorisedUser(userId);
    }

    @Test
    public void shouldNotReturnReusableInfoForMajorCreditor() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        getHearingPollForMatch(hearingOne.getHearing().getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        pollForReusableInfo(hearingOne.getHearingId().toString(), new Matcher[]{
                withJsonPath("$.reusablePrompts", hasSize(2)),
                withJsonPath("$.reusablePrompts[0].promptRef", is("defendantDrivingLicenceNumber")),
                withJsonPath("$.reusablePrompts[1].promptRef", is("defendantDrivingLicenceNumber"))
        });
    }

    @Test
    public void shouldReturnReusableInfoForMinorCreditor() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearingWithNsp(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID caseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();
        getHearingPollForMatch(hearingOne.getHearing().getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        pollForReusableInfo(hearingOne.getHearingId().toString(), new Matcher[]{
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
        });
    }


    @Test
    public void shouldReturnReusableInfoForApplication() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate(), true, true, true, true, false, true));

        getHearingPollForMatch(hearingOne.getHearing().getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        pollForReusableInfo(hearingOne.getHearingId().toString(), new Matcher[]{
                withJsonPath("$.reusablePrompts", hasSize(4)),
                withJsonPath("$.reusablePrompts[0].promptRef", is("defendantDrivingLicenceNumber")),
                withJsonPath("$.reusablePrompts[1].promptRef", is("defendantDrivingLicenceNumber")),
                withJsonPath("$.reusablePrompts[1].value", is("")),
                withJsonPath("$.reusablePrompts[2].applicationId", is(hearingOne.getCourtApplication().getId().toString())),
                withJsonPath("$.reusablePrompts[2].promptRef", is("prosecutortobenotified")),
                withJsonPath("$.reusablePrompts[2].type", is("NAMEADDRESS")),
                withJsonPath("$.reusablePrompts[2].value.prosecutortobenotifiedOrganisationName", is("ABC Org")),
                withJsonPath("$.reusablePrompts[2].value.prosecutortobenotifiedAddress1", is("address1")),
                withJsonPath("$.reusablePrompts[2].value.prosecutortobenotifiedAddress2", is("address2")),
                withJsonPath("$.reusablePrompts[2].value.prosecutortobenotifiedPostCode", is("CB3 0GU")),
                withJsonPath("$.reusablePrompts[2].value.prosecutortobenotifiedEmailAddress1", is("James.Thomas@gmail.com")),
                withJsonPath("$.reusablePrompts[3].promptRef", is("defendantDrivingLicenceNumber")),
                withJsonPath("$.reusablePrompts[3].value", is("DVLA12345"))
        });
    }

    private void pollForReusableInfo(final String hearingId, final Matcher[] matchers) {

        poll(requestParams(getQueryReusableInfoUrl(hearingId), "application/vnd.hearing.query.reusable-info+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(matchers))
                );
    }
}
