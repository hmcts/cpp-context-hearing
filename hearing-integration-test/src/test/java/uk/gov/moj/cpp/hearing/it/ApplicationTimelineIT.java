package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.command.TrialType.builder;
import static uk.gov.moj.cpp.hearing.it.UseCases.setTrialType;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Person;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class ApplicationTimelineIT extends AbstractIT {

    private Hearing hearingOne;
    private Hearing hearingTwo;
    private UUID applicationId;
    private TrialType addTrialType;

    @Before
    public void setUpHearingWithApplication() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOneHelper =
                h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        hearingOne = hearingOneHelper.getHearing();

        addTrialType = builder()
                .withHearingId(hearingOne.getId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        applicationId = hearingOne.getCourtApplications().get(0).getId();

        setTrialType(getRequestSpec(), hearingOne.getId(), addTrialType);
    }

    private void setUpSecondHearingWithApplication() {
        final CommandHelpers.InitiateHearingCommandHelper hearingTwoHelper =
                h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingWithApplicationTemplate(hearingOne.getCourtApplications())));
        hearingTwo = hearingTwoHelper.getHearing();
        setTrialType(getRequestSpec(), hearingTwo.getId(), addTrialType);
    }

    @Test
    public void shouldDisplayApplicationTimeline() {
        final String timelineQueryAPIEndPoint = format(ENDPOINT_PROPERTIES.getProperty("hearing.application.timeline"), applicationId);
        final String timelineURL = getBaseUri() + "/" + timelineQueryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.application.timeline+json";
        final Map<String, String> hearingSummaryMap = populateHearingSummaryKeyMap(hearingOne);

        poll(requestParams(timelineURL, mediaType).withHeader(USER_ID, getLoggedInUser()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingSummaries[0].hearingId", is(hearingOne.getId().toString())),
                                withJsonPath("$.hearingSummaries[0].hearingType", is(hearingSummaryMap.get("hearingType"))),
                                withJsonPath("$.hearingSummaries[0].courtHouse", is(hearingSummaryMap.get("courtHouse"))),
                                withJsonPath("$.hearingSummaries[0].courtRoom", is(hearingSummaryMap.get("courtRoom"))),
                                withJsonPath("$.hearingSummaries[0].hearingTime", is(hearingSummaryMap.get("hearingTime"))),
                                withJsonPath("$.hearingSummaries[0].estimatedDuration", is(Integer.parseInt(hearingSummaryMap.get("listedDurationMinutes")))),
                                withJsonPath("$.hearingSummaries[0].hearingDate", is(hearingSummaryMap.get("hearingDate"))),
                                withJsonPath("$.hearingSummaries[0].applicants[0]", is(hearingSummaryMap.get("applicant")))
                        )));

    }

    @Test
    public void shouldDisplayApplicationMultiHearingTimeline() throws InterruptedException {

        setUpSecondHearingWithApplication();

        final String timelineQueryAPIEndPoint = format(ENDPOINT_PROPERTIES.getProperty("hearing.application.timeline"), applicationId);
        final String timelineURL = getBaseUri() + "/" + timelineQueryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.application.timeline+json";

        poll(requestParams(timelineURL, mediaType).withHeader(USER_ID, getLoggedInUser()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                anyOf(
                                        withJsonPath("$.hearingSummaries[0].hearingId", containsString(hearingOne.getId().toString())),
                                        withJsonPath("$.hearingSummaries[1].hearingId", containsString(hearingOne.getId().toString()))),
                                anyOf(
                                        withJsonPath("$.hearingSummaries[0].hearingId", containsString(hearingTwo.getId().toString())),
                                        withJsonPath("$.hearingSummaries[1].hearingId", containsString(hearingTwo.getId().toString())))
                        )));
    }

    private Map<String, String> populateHearingSummaryKeyMap(Hearing hearing) {
        Map<String, String> hearingSummaryMap = new HashMap<>();

        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        final Person personDetails = hearing.getCourtApplications().get(0).getApplicant().getPersonDetails();

        hearingSummaryMap.put("hearingDate", hearingDay.getSittingDay().toLocalDate().format(ofPattern("dd MMM yyyy")));
        hearingSummaryMap.put("hearingTime", hearingDay.getSittingDay().toLocalTime().format(ofPattern("HH:mm")));
        hearingSummaryMap.put("hearingType", hearing.getType().getDescription());
        hearingSummaryMap.put("courtHouse", hearing.getCourtCentre().getName());
        hearingSummaryMap.put("courtRoom", hearing.getCourtCentre().getRoomName());
        hearingSummaryMap.put("listedDurationMinutes", hearingDay.getListedDurationMinutes().toString());
        hearingSummaryMap.put("applicant", String.format("%s %s", personDetails.getFirstName(), personDetails.getLastName()));

        return hearingSummaryMap;
    }

}