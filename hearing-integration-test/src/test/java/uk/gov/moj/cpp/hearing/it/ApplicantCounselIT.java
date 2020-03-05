package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddApplicantCounselCommandTemplates.addApplicantCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateApplicantCounselCommandTemplates.updateApplicantCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.hearing.courts.AddApplicantCounsel;
import uk.gov.justice.hearing.courts.RemoveApplicantCounsel;
import uk.gov.justice.hearing.courts.UpdateApplicantCounsel;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class ApplicantCounselIT extends AbstractIT {

    public static ApplicantCounsel createFirstApplicantCounsel(final InitiateHearingCommandHelper hearingOne) {
        final AddApplicantCounsel firstApplicantCounselCommand = UseCases.addApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addApplicantCounselCommandTemplate(hearingOne.getHearingId())
        );
        ApplicantCounsel firstApplicantCounsel = firstApplicantCounselCommand.getApplicantCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.applicantCounsels.[0].status", is(firstApplicantCounsel.getStatus())),
                                withJsonPath("$.hearing.applicantCounsels.[0].firstName", is(firstApplicantCounsel.getFirstName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].lastName", is(firstApplicantCounsel.getLastName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].title", is(firstApplicantCounsel.getTitle())),
                                withJsonPath("$.hearing.applicantCounsels.[0].middleName", is(firstApplicantCounsel.getMiddleName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].attendanceDays.[0]", is(firstApplicantCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].applicants.[0]", is(firstApplicantCounsel.getApplicants().get(0).toString()))
                        )));
        return firstApplicantCounsel;
    }

    @Test
    public void addApplicantCounsel_shouldAdd() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        ApplicantCounsel firstApplicantCounsel = createFirstApplicantCounsel(hearingOne);

        ApplicantCounsel secondApplicantCounsel = createSecondApplicantCounsel(hearingOne, firstApplicantCounsel);


        //Adding same applicant counsel should be ignored
        final String currentLastNameValueForFprFirstPC = firstApplicantCounsel.getLastName();
        firstApplicantCounsel.setLastName("DummyLastName");
        final AddApplicantCounsel firstApplicantCounselReAddCommand = UseCases.addApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addApplicantCounselCommandTemplate(hearingOne.getHearingId(), firstApplicantCounsel)
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.applicantCounsels.[0].id", is(firstApplicantCounsel.getId().toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].status", is(firstApplicantCounsel.getStatus())),
                                withJsonPath("$.hearing.applicantCounsels.[0].firstName", is(firstApplicantCounsel.getFirstName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].lastName", is(currentLastNameValueForFprFirstPC)),
                                withJsonPath("$.hearing.applicantCounsels.[0].title", is(firstApplicantCounsel.getTitle())),
                                withJsonPath("$.hearing.applicantCounsels.[0].middleName", is(firstApplicantCounsel.getMiddleName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].attendanceDays.[0]", is(firstApplicantCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].applicants.[0]", is(firstApplicantCounsel.getApplicants().get(0).toString()))
                        )));

    }

    private ApplicantCounsel createSecondApplicantCounsel(final InitiateHearingCommandHelper hearingOne, final ApplicantCounsel firstApplicantCounsel) {
        final AddApplicantCounsel secondApplicantCounselCommand = UseCases.addApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addApplicantCounselCommandTemplate(hearingOne.getHearingId())
        );
        ApplicantCounsel secondApplicantCounsel = secondApplicantCounselCommand.getApplicantCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.applicantCounsels.[0].status", is(firstApplicantCounsel.getStatus())),
                                withJsonPath("$.hearing.applicantCounsels.[0].firstName", is(firstApplicantCounsel.getFirstName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].lastName", is(firstApplicantCounsel.getLastName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].title", is(firstApplicantCounsel.getTitle())),
                                withJsonPath("$.hearing.applicantCounsels.[0].middleName", is(firstApplicantCounsel.getMiddleName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].attendanceDays.[0]", is(firstApplicantCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].applicants.[0]", is(firstApplicantCounsel.getApplicants().get(0).toString())),

                                withJsonPath("$.hearing.applicantCounsels.[1].status", is(secondApplicantCounsel.getStatus())),
                                withJsonPath("$.hearing.applicantCounsels.[1].firstName", is(secondApplicantCounsel.getFirstName())),
                                withJsonPath("$.hearing.applicantCounsels.[1].lastName", is(secondApplicantCounsel.getLastName())),
                                withJsonPath("$.hearing.applicantCounsels.[1].title", is(secondApplicantCounsel.getTitle())),
                                withJsonPath("$.hearing.applicantCounsels.[1].middleName", is(secondApplicantCounsel.getMiddleName())),
                                withJsonPath("$.hearing.applicantCounsels.[1].attendanceDays.[0]", is(secondApplicantCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.applicantCounsels.[1].applicants.[0]", is(secondApplicantCounsel.getApplicants().get(0).toString()))
                        )));
        return secondApplicantCounsel;
    }

    @Test
    public void removeApplicantCounsel_shouldRemove() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        ApplicantCounsel firstApplicantCounsel = createFirstApplicantCounsel(hearingOne);
        //remove first PC
        UseCases.removeApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveApplicantCounsel(hearingOne.getHearingId(), firstApplicantCounsel.getId())
        );
        createSecondApplicantCounsel(hearingOne);


    }

    @Test
    public void updateApplicantCounsel_shouldUpdate() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        ApplicantCounsel firstApplicantCounsel = createFirstApplicantCounsel(hearingOne);


        //Updating Applicant counsel
        firstApplicantCounsel.setFirstName("DummyFirstName");
        firstApplicantCounsel.setLastName("DummyLastName");
        firstApplicantCounsel.setStatus("DummyStatus");
        firstApplicantCounsel.setTitle("UpdateTitle");
        firstApplicantCounsel.setMiddleName("DummyMiddleName");
        firstApplicantCounsel.setAttendanceDays(Arrays.asList(LocalDate.now().plusDays(1)));

        final UpdateApplicantCounsel firstApplicantCounselUpdateCommand = UseCases.updateApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateApplicantCounselCommandTemplate(hearingOne.getHearingId(), firstApplicantCounsel)
        );

        ApplicantCounsel firstApplicantCounselUpdated = firstApplicantCounselUpdateCommand.getApplicantCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.applicantCounsels.[0].id", is(firstApplicantCounsel.getId().toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].status", is(firstApplicantCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.applicantCounsels.[0].firstName", is(firstApplicantCounselUpdated.getFirstName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].lastName", is(firstApplicantCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].title", is(firstApplicantCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.applicantCounsels.[0].middleName", is(firstApplicantCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].attendanceDays.[0]", is(firstApplicantCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].applicants.[0]", is(firstApplicantCounsel.getApplicants().get(0).toString()))
                        )));

    }

    @Test
    public void testUpdateApplicantCounselWhenApplicantCounselIsRemovedThenApplicantCounselShouldNotBeUpdated() throws Exception {
        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        ApplicantCounsel firstApplicantCounsel = createFirstApplicantCounsel(hearingOne);

        ApplicantCounsel secondApplicantCounsel = createSecondApplicantCounsel(hearingOne, firstApplicantCounsel);


        UseCases.removeApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveApplicantCounsel(hearingOne.getHearingId(), firstApplicantCounsel.getId())
        );

        firstApplicantCounsel.setLastName("DummyLastName");
        final UpdateApplicantCounsel firstApplicantCounselUpdatedCommand = UseCases.updateApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateApplicantCounselCommandTemplate(hearingOne.getHearingId(), firstApplicantCounsel)
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.applicantCounsels", hasSize(1)),
                                withJsonPath("$.hearing.applicantCounsels.[0].status", is(secondApplicantCounsel.getStatus())),
                                withJsonPath("$.hearing.applicantCounsels.[0].firstName", is(secondApplicantCounsel.getFirstName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].lastName", is(secondApplicantCounsel.getLastName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].title", is(secondApplicantCounsel.getTitle())),
                                withJsonPath("$.hearing.applicantCounsels.[0].middleName", is(secondApplicantCounsel.getMiddleName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].attendanceDays.[0]", is(secondApplicantCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].applicants.[0]", is(secondApplicantCounsel.getApplicants().get(0).toString())))));

    }

    private void createSecondApplicantCounsel(final InitiateHearingCommandHelper hearingOne) {
        final AddApplicantCounsel secondApplicantCounselCommand = UseCases.addApplicantCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addApplicantCounselCommandTemplate(hearingOne.getHearingId())
        );
        ApplicantCounsel secondApplicantCounsel = secondApplicantCounselCommand.getApplicantCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.applicantCounsels", hasSize(1)),
                                withJsonPath("$.hearing.applicantCounsels.[0].status", is(secondApplicantCounsel.getStatus())),
                                withJsonPath("$.hearing.applicantCounsels.[0].firstName", is(secondApplicantCounsel.getFirstName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].lastName", is(secondApplicantCounsel.getLastName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].title", is(secondApplicantCounsel.getTitle())),
                                withJsonPath("$.hearing.applicantCounsels.[0].middleName", is(secondApplicantCounsel.getMiddleName())),
                                withJsonPath("$.hearing.applicantCounsels.[0].attendanceDays.[0]", is(secondApplicantCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.applicantCounsels.[0].applicants.[0]", is(secondApplicantCounsel.getApplicants().get(0).toString()))
                        )));
    }
}
