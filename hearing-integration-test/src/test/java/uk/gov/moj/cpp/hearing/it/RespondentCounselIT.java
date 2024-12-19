package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.addRespondentCounsel;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.removeRespondentCounsel;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateRespondentCounsel;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddRespondentCounselCommandTemplates.addRespondentCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateRespondentCounselCommandTemplates.updateRespondentCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.hearing.courts.AddRespondentCounsel;
import uk.gov.justice.hearing.courts.RemoveRespondentCounsel;
import uk.gov.justice.hearing.courts.UpdateRespondentCounsel;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

@NotThreadSafe
public class RespondentCounselIT extends AbstractIT {

    @SuppressWarnings("squid:S2699")
    @Test
    void testRespondentCounsel_shouldAddUpdateAndRemove() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        RespondentCounsel firstRespondentCounsel = createFirstRespondentCounsel(hearingOne);

        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.respondentCounsels.[0].id", is(firstRespondentCounsel.getId().toString())),
                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounsel.getStatus())),
                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounsel.getFirstName())),
                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounsel.getLastName())),
                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounsel.getTitle())),
                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounsel.getMiddleName())),
                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounsel.getRespondents().get(0).toString()))
        );

        //Updating respondent counsel
        firstRespondentCounsel.setFirstName("DummyFirstName");
        firstRespondentCounsel.setLastName("DummyLastName");
        firstRespondentCounsel.setStatus("DummyStatus");
        firstRespondentCounsel.setTitle("UpdateTitle");
        firstRespondentCounsel.setMiddleName("DummyMiddleName");
        firstRespondentCounsel.setAttendanceDays(List.of(LocalDate.now().plusDays(1)));

        final UpdateRespondentCounsel firstRespondentCounselReAddCommand = updateRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateRespondentCounselCommandTemplate(hearingOne.getHearingId(), firstRespondentCounsel)
        );

        RespondentCounsel firstRespondentCounselUpdated = firstRespondentCounselReAddCommand.getRespondentCounsel();
        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.respondentCounsels.[0].id", is(firstRespondentCounsel.getId().toString())),
                withJsonPath("$.hearing.respondentCounsels.[0].status", is(firstRespondentCounselUpdated.getStatus())),
                withJsonPath("$.hearing.respondentCounsels.[0].firstName", is(firstRespondentCounselUpdated.getFirstName())),
                withJsonPath("$.hearing.respondentCounsels.[0].lastName", is(firstRespondentCounselUpdated.getLastName())),
                withJsonPath("$.hearing.respondentCounsels.[0].title", is(firstRespondentCounselUpdated.getTitle())),
                withJsonPath("$.hearing.respondentCounsels.[0].middleName", is(firstRespondentCounselUpdated.getMiddleName())),
                withJsonPath("$.hearing.respondentCounsels.[0].attendanceDays.[0]", is(firstRespondentCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.respondentCounsels.[0].respondents.[0]", is(firstRespondentCounsel.getRespondents().get(0).toString()))
        );

        removeRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveRespondentCounsel(hearingOne.getHearingId(), firstRespondentCounsel.getId()));

        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.respondentCounsels", is(empty()))
        );

    }

    public static RespondentCounsel createFirstRespondentCounsel(final InitiateHearingCommandHelper hearingOne) {
        final AddRespondentCounsel firstRespondentCounselCommand = addRespondentCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addRespondentCounselCommandTemplate(hearingOne.getHearingId())
        );
        RespondentCounsel firstRespondentCounsel = firstRespondentCounselCommand.getRespondentCounsel();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser()).build())
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
}