package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForDefendantTypeOrganisation;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorizedUserToQueryCaseByDefendant;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.junit.Test;

public class CaseByDefendantQueryIT extends AbstractIT{

    @Test
    public void shouldReturnCasesByPersonDefendantWhileCaseIdsArePassedAsQueryParameter() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        Queries.getHearingPollForMatch(hearingOne.getHearing().getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        final ProsecutionCase prosecutionCase = hearingOne.getHearing().getProsecutionCases().get(0);
        final Person person = prosecutionCase.getDefendants().get(0).getPersonDefendant().getPersonDetails();
        final LocalDate hearingDate = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        setupAsAuthorizedUserToQueryCaseByDefendant(getLoggedInUser());

        poll(requestParams(getURL("hearing.get.cases-by-person-defendant-with-caseId", person.getFirstName(), person.getLastName(), person.getDateOfBirth(), hearingDate, prosecutionCase.getId()),
            "application/vnd.hearing.get.cases-by-person-defendant+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecutionCases", IsCollectionWithSize.hasSize(1)),
                                withJsonPath("$.prosecutionCases[0].caseId", is(prosecutionCase.getId().toString()))
                        ))
                );
    }

    @Test
    public void shouldReturnCasesByPersonDefendantWhileCaseIdsAreNotPassedAsQueryParameter() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        Queries.getHearingPollForMatch(hearingOne.getHearing().getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        final ProsecutionCase prosecutionCase = hearingOne.getHearing().getProsecutionCases().get(0);
        final Person person = prosecutionCase.getDefendants().get(0).getPersonDefendant().getPersonDetails();
        final LocalDate hearingDate = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        setupAsAuthorizedUserToQueryCaseByDefendant(getLoggedInUser());

        poll(requestParams(getURL("hearing.get.cases-by-person-defendant-without-caseId", person.getFirstName(), person.getLastName(), person.getDateOfBirth(), hearingDate),
                "application/vnd.hearing.get.cases-by-person-defendant+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecutionCases", IsCollectionWithSize.hasSize(1)),
                                withJsonPath("$.prosecutionCases[0].caseId", is(prosecutionCase.getId().toString()))
                        ))
                );
    }

    @Test
    public void shouldReturnCasesByOrganisationDefendant() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateForDefendantTypeOrganisation()));

        Queries.getHearingPollForMatch(hearingOne.getHearing().getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearing().getId())))
        );

        final ProsecutionCase prosecutionCase = hearingOne.getHearing().getProsecutionCases().get(0);
        final Organisation organisation = prosecutionCase.getDefendants().get(0).getLegalEntityDefendant().getOrganisation();
        final LocalDate hearingDate = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        setupAsAuthorizedUserToQueryCaseByDefendant(getLoggedInUser());

        poll(requestParams(getURL("hearing.get.cases-by-organisation-defendant", organisation.getName(), hearingDate, prosecutionCase.getId()),
                "application/vnd.hearing.get.cases-by-organisation-defendant+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecutionCases", IsCollectionWithSize.hasSize(1)),
                                withJsonPath("$.prosecutionCases[0].caseId", is(prosecutionCase.getId().toString()))
                        ))
                );
    }
}
