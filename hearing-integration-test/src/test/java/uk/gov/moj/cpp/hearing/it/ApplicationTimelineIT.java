package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.command.TrialType.builder;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.setTrialType;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.utils.RestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.Json;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplicationTimelineIT extends AbstractIT {

    private Hearing hearingOne;
    private Hearing hearingTwo;
    private UUID applicationId;
    private TrialType addTrialType;

    @BeforeEach
    public void setUpHearingWithApplication() {
        final InitiateHearingCommandHelper hearingOneHelper =
                h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        hearingOne = hearingOneHelper.getHearing();

        addTrialType = builder()
                .withHearingId(hearingOne.getId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        applicationId = hearingOne.getCourtApplications().get(0).getId();

        setTrialType(getRequestSpec(), hearingOne.getId(), addTrialType);
    }

    @Test
    public void shouldDeleteApplicationHearing(){
        System.out.println(applicationId);
        sendMessage(getPublicTopicInstance().createProducer(),
                "public.progression.events.court-application-deleted",
                Json.createObjectBuilder().add("hearingId",hearingOne.getId().toString()).add("applicationId",applicationId.toString() ).build(),
                metadataOf(randomUUID(),"public.progression.events.court-application-deleted")
                        .withUserId(randomUUID().toString())
                        .build()
        );

        RestUtils.poll(requestParams(getURL("hearing.get.hearing", hearingOne.getId()),
                        "application/vnd.hearing.get.hearing+json").withHeader(USER_ID, getLoggedInAdminUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withoutJsonPath("$.hearing.id")
                        ))
                );

        pollForApplicationTimeline(new Matcher[]{
                anyOf(
                        withoutJsonPath("$.hearingSummaries[0].hearingId"))
        });
    }

    @Test
    public void shouldDisplayApplicationsOnTimeline() {

        stubCourtRoom(hearingOne);

        final Map<String, String> hearingSummaryMap = populateHearingSummaryKeyMap(hearingOne);

        pollForApplicationTimeline(new Matcher[]{
                withJsonPath("$.hearingSummaries[0].hearingId", is(hearingOne.getId().toString())),
                withJsonPath("$.hearingSummaries[0].hearingType", is(hearingSummaryMap.get("hearingType"))),
                withJsonPath("$.hearingSummaries[0].courtHouse", is(hearingSummaryMap.get("courtHouse"))),
                withJsonPath("$.hearingSummaries[0].courtRoom", is(hearingSummaryMap.get("courtRoom"))),
                withJsonPath("$.hearingSummaries[0].hearingTime", is(hearingSummaryMap.get("hearingTime"))),
                withJsonPath("$.hearingSummaries[0].estimatedDuration", is(Integer.parseInt(hearingSummaryMap.get("listedDurationMinutes")))),
                withJsonPath("$.hearingSummaries[0].hearingDate", is(hearingSummaryMap.get("hearingDate"))),
                withJsonPath("$.hearingSummaries[0].applicants[0]", is(hearingSummaryMap.get("applicant"))),
                withJsonPath("$.hearingSummaries[0].youthDefendantIds", is(empty()))
        });

        setUpSecondHearingWithApplication();

        pollForApplicationTimeline(new Matcher[]{
                anyOf(
                        withJsonPath("$.hearingSummaries[0].hearingId", containsString(hearingOne.getId().toString())),
                        withJsonPath("$.hearingSummaries[1].hearingId", containsString(hearingOne.getId().toString()))),
                anyOf(
                        withJsonPath("$.hearingSummaries[0].hearingId", containsString(hearingTwo.getId().toString())),
                        withJsonPath("$.hearingSummaries[1].hearingId", containsString(hearingTwo.getId().toString())))
        });

    }

    private Map<String, String> populateHearingSummaryKeyMap(Hearing hearing) {
        Map<String, String> hearingSummaryMap = new HashMap<>();

        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        final Organisation organisation = hearing.getCourtApplications().get(0).getApplicant().getOrganisation();

        hearingSummaryMap.put("hearingDate", hearingDay.getSittingDay().toLocalDate().format(ofPattern("dd MMM yyyy")));
        hearingSummaryMap.put("hearingTime", hearingDay.getSittingDay().toLocalTime().format(ofPattern("HH:mm")));
        hearingSummaryMap.put("hearingType", hearing.getType().getDescription());
        hearingSummaryMap.put("courtHouse", hearing.getCourtCentre().getName());
        hearingSummaryMap.put("courtRoom", hearing.getCourtCentre().getRoomName());
        hearingSummaryMap.put("listedDurationMinutes", hearingDay.getListedDurationMinutes().toString());
        hearingSummaryMap.put("applicant", organisation.getName());
        hearingSummaryMap.put("applicantId", hearing.getCourtApplications().get(0).getApplicant().getId().toString());
        hearingSummaryMap.put("respondentId", hearing.getCourtApplications().get(0).getRespondents().get(0).getId().toString());
        hearingSummaryMap.put("subjectId", hearing.getCourtApplications().get(0).getSubject().getId().toString());
        return hearingSummaryMap;
    }

    private void pollForApplicationTimeline(final Matcher[] matchers) {
        final String timelineQueryAPIEndPoint = format(ENDPOINT_PROPERTIES.getProperty("hearing.application.timeline"), applicationId);
        final String timelineURL = getBaseUri() + "/" + timelineQueryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.application.timeline+json";

        poll(requestParams(timelineURL, mediaType).withHeader(USER_ID, getLoggedInUser()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(matchers)));
    }

    private void setUpSecondHearingWithApplication() {
        final InitiateHearingCommandHelper hearingTwoHelper =
                h(initiateHearing(getRequestSpec(), standardInitiateHearingWithApplicationTemplate(hearingOne.getCourtApplications())));
        hearingTwo = hearingTwoHelper.getHearing();
        setTrialType(getRequestSpec(), hearingTwo.getId(), addTrialType);
    }

}
