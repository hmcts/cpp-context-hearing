package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.hearing.courts.AddRespondentCounsel;
import uk.gov.justice.hearing.courts.RemoveRespondentCounsel;
import uk.gov.justice.hearing.courts.UpdateRespondentCounsel;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddRespondentCounselCommandTemplates.addRespondentCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddRespondentCounselCommandTemplates.addRespondentCounselCommandTemplateWithoutMiddleName;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateRespondentCounselCommandTemplates.updateRespondentCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

@SuppressWarnings("unchecked")
public class RespondentCounselIT extends AbstractIT {

    public static RespondentCounsel createFirstRespondentCounsel(final InitiateHearingCommandHelper hearingOne) {
        final AddRespondentCounsel firstRespondentCounselCommand = UseCases.addRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addRespondentCounselCommandTemplate(hearingOne.getHearingId())
        );
        RespondentCounsel firstRespondentCounsel = firstRespondentCounselCommand.getRespondentCounsel();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounsel.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounsel.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounsel.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounsel.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounsel.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounsel.getRespondents().get(0).toString()))
                        )));
        return firstRespondentCounsel;
    }

    @Test
    public void addRespondentCounsel_shouldAdd() throws Exception {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        RespondentCounsel firstRespondentCounsel = createFirstRespondentCounsel(hearingOne);

        //Adding same respondent counsel should be ignored
        final String currentLastNameValueForFprFirstPC = firstRespondentCounsel.getLastName();
        firstRespondentCounsel.setLastName("DummyLastName");

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels.[0].id", is(firstRespondentCounsel.getId().toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounsel.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounsel.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(currentLastNameValueForFprFirstPC)),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounsel.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounsel.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounsel.getRespondents().get(0).toString()))
                        )));

    }

    @Test
    public void removeRespondentCounsel_shouldRemove() throws Exception {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final AddRespondentCounsel firstRespondentCounselCommand = UseCases.addRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addRespondentCounselCommandTemplateWithoutMiddleName(hearingOne.getHearingId())
        );
        RespondentCounsel firstRespondentCounsel = firstRespondentCounselCommand.getRespondentCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounsel.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounsel.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounsel.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounsel.getTitle())),
                                withoutJsonPath("$.hearing.respondentCounsels.[0].middleName"),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounsel.getRespondents().get(0).toString()))
                        )));

        //remove first DC
        UseCases.removeRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveRespondentCounsel(hearingOne.getHearingId(), firstRespondentCounsel.getId())
        );
        final AddRespondentCounsel secondRespondentCounselCommand = UseCases.addRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addRespondentCounselCommandTemplate(hearingOne.getHearingId())
        );
        RespondentCounsel secondRespondentCounsel = secondRespondentCounselCommand.getRespondentCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels", hasSize(1)),
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(secondRespondentCounsel.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(secondRespondentCounsel.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(secondRespondentCounsel.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(secondRespondentCounsel.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(secondRespondentCounsel.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(secondRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(secondRespondentCounsel.getRespondents().get(0).toString()))
                        )));


    }

    @Test
    public void updateRespondentCounsel_shouldUpdate() throws Exception {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        RespondentCounsel firstRespondentCounsel = createFirstRespondentCounsel(hearingOne);


        //Updating respondent counsel
        firstRespondentCounsel.setFirstName("DummyFirstName");
        firstRespondentCounsel.setLastName("DummyLastName");
        firstRespondentCounsel.setStatus("DummyStatus");
        firstRespondentCounsel.setTitle("UpdateTitle");
        firstRespondentCounsel.setMiddleName("DummyMiddleName");
        firstRespondentCounsel.setAttendanceDays(Arrays.asList(LocalDate.now().plusDays(1)));

        final UpdateRespondentCounsel firstRespondentCounselReAddCommand = UseCases.updateRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateRespondentCounselCommandTemplate(hearingOne.getHearingId(), firstRespondentCounsel)
        );

        RespondentCounsel firstRespondentCounselUpdated = firstRespondentCounselReAddCommand.getRespondentCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels.[0].id", is(firstRespondentCounsel.getId().toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounselUpdated.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounsel.getRespondents().get(0).toString()))
                        )));

    }

    @Test
    public void testUpdateRespondentCounselWhenRespondentCounselIsRemovedThenRespondentCounselShouldNotBeUpdated() throws Exception {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        RespondentCounsel firstRespondentCounsel = createFirstRespondentCounsel(hearingOne);

        RespondentCounsel secondRespondentCounsel = createSecondRespondentCounsel(hearingOne, firstRespondentCounsel);


        UseCases.removeRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveRespondentCounsel(hearingOne.getHearingId(), firstRespondentCounsel.getId())
        );

        firstRespondentCounsel.setLastName("DummyLastName");
        final UpdateRespondentCounsel firstRespondentCounselUpdatedCommand = UseCases.updateRespondentCounselAfterRemovingCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateRespondentCounselCommandTemplate(hearingOne.getHearingId(), firstRespondentCounsel)
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels", hasSize(1)),
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(secondRespondentCounsel.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(secondRespondentCounsel.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(secondRespondentCounsel.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(secondRespondentCounsel.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(secondRespondentCounsel.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(secondRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(secondRespondentCounsel.getRespondents().get(0).toString())))));


    }

    private RespondentCounsel createSecondRespondentCounsel(final InitiateHearingCommandHelper hearingOne, final RespondentCounsel firstRespondentCounsel) {
        final AddRespondentCounsel secondRespondentCounselCommand = UseCases.addRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addRespondentCounselCommandTemplate(hearingOne.getHearingId())
        );
        RespondentCounsel secondRespondentCounsel = secondRespondentCounselCommand.getRespondentCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounsel.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounsel.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounsel.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounsel.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounsel.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounsel.getRespondents().get(0).toString())),

                                withJsonPath("$.hearing.respondentCounsels.[1].status", is(secondRespondentCounsel.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[1].firstName", is(secondRespondentCounsel.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[1].lastName", is(secondRespondentCounsel.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[1].title", is(secondRespondentCounsel.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[1].middleName", is(secondRespondentCounsel.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[1].attendanceDays.[0]", is(secondRespondentCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[1].respondents.[0]", is(secondRespondentCounsel.getRespondents().get(0).toString()))
                        )));
        return secondRespondentCounsel;
    }

    @Test
    public void testUpdateRespondentCounselWithPreviouslySetValues() throws Exception {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        RespondentCounsel firstRespondentCounsel = createFirstRespondentCounsel(hearingOne);

        String tempFirstRespondentCounsel = firstRespondentCounsel.getFirstName();

        //Updating respondent counsel first time
        firstRespondentCounsel.setFirstName("DummyFirstName");
        firstRespondentCounsel.setLastName("DummyLastName");
        firstRespondentCounsel.setStatus("DummyStatus");
        firstRespondentCounsel.setTitle("UpdateTitle");
        firstRespondentCounsel.setMiddleName("DummyMiddleName");
        firstRespondentCounsel.setAttendanceDays(Arrays.asList(LocalDate.now().plusDays(1)));

        final UpdateRespondentCounsel firstRespondentCounselReAddCommand = UseCases.updateRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateRespondentCounselCommandTemplate(hearingOne.getHearingId(), firstRespondentCounsel)
        );

        RespondentCounsel firstRespondentCounselUpdated = firstRespondentCounselReAddCommand.getRespondentCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels.[0].id", is(firstRespondentCounselUpdated.getId().toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounselUpdated.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounselUpdated.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounselUpdated.getRespondents().get(0).toString()))
                        )));
        //UpdateFirstRespondentCounsel second time with Original values
        firstRespondentCounselUpdated.setFirstName(tempFirstRespondentCounsel);

        final UpdateRespondentCounsel thirdTimeUpdateCommand = UseCases.updateRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateRespondentCounselCommandTemplate(hearingOne.getHearingId(), firstRespondentCounselUpdated));


        RespondentCounsel thirdUpdateWithFirst = thirdTimeUpdateCommand.getRespondentCounsel();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.respondentCounsels.[0].id", is(firstRespondentCounselUpdated.getId().toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(thirdUpdateWithFirst.getFirstName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounselUpdated.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounselUpdated.getRespondents().get(0).toString()))
                        )));
    }
}