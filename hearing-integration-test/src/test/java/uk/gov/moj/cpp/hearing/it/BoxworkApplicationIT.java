package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.command.TrialType.builder;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.setTrialType;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithIsBoxHearing;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubApplicationsByParentId;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.steps.CourtListRestrictionSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S2699")
public class BoxworkApplicationIT extends AbstractIT {

    @Test
    public void shouldDisplayApplicationOnTimelineForBoxworkHearing() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithIsBoxHearing(true);
        final Hearing hearing = initiateHearingCommand.getHearing();
        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1L));

        final InitiateHearingCommandHelper hearingHelper = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        final UUID applicationId = hearingHelper.getHearing().getCourtApplications().get(0).getId();

        final TrialType trialType = builder()
                .withHearingId(hearing.getId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        setTrialType(getRequestSpec(), hearing.getId(), trialType);

        stubCourtRoom(hearing);
        stubApplicationsByParentId(applicationId);

        final String hearingDate = hearingDay.getSittingDay().toLocalDate().format(ofPattern("dd MMM yyyy"));

        final String timelineURL = getBaseUri() + "/" + format(ENDPOINT_PROPERTIES.getProperty("hearing.application.timeline"), applicationId);

        poll(requestParams(timelineURL, "application/vnd.hearing.application.timeline+json")
                .withHeader(USER_ID, getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingSummaries[0].hearingId", is(hearing.getId().toString())),
                                withJsonPath("$.hearingSummaries[0].hearingDate", is(hearingDate)),
                                withJsonPath("$.hearingSummaries[0].isBoxHearing", is(true))
                        )));
    }

    @Test
    public void shouldRestrictCourtListWhenBoxworkApplicationSubjectIsYouth() {
        final HearingFactory factory = new HearingFactory();
        final CourtApplicationParty youthSubject = buildYouthApplicationParty(factory);
        final CourtApplication courtApplication = factory.courtApplication(youthSubject).build();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(courtApplication));
        initiateHearingCommand.getHearing().setIsBoxHearing(true);
        h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final Hearing hearing = initiateHearingCommand.getHearing();
        final CourtListRestrictionSteps steps = new CourtListRestrictionSteps();

        steps.hideApplicationApplicantFromXhibit(hearing, true);

        steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
    }

    @Test
    public void shouldRestrictCourtListWhenBoxworkApplicationRespondentIsYouth() {
        final HearingFactory factory = new HearingFactory();
        final CourtApplicationParty youthRespondent = buildYouthApplicationParty(factory);
        final CourtApplication courtApplication = factory.courtApplication()
                .withRespondents(singletonList(youthRespondent))
                .build();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(courtApplication));
        initiateHearingCommand.getHearing().setIsBoxHearing(true);
        h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final Hearing hearing = initiateHearingCommand.getHearing();
        final CourtListRestrictionSteps steps = new CourtListRestrictionSteps();

        steps.hideApplicationRespondentFromXhibit(hearing, true);

        steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.courtApplicationRespondentIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
    }

    private CourtApplicationParty buildYouthApplicationParty(final HearingFactory factory) {
        final MasterDefendant youthMasterDefendant = MasterDefendant.masterDefendant()
                .withMasterDefendantId(randomUUID())
                .withPersonDefendant(PersonDefendant.personDefendant()
                        .withPersonDetails(factory.person().build())
                        .build())
                .withIsYouth(true)
                .build();

        return CourtApplicationParty.courtApplicationParty()
                .withMasterDefendant(youthMasterDefendant)
                .withId(randomUUID())
                .withSummonsRequired(false)
                .withNotificationRequired(false)
                .withPersonDetails(factory.person().build())
                .build();
    }
}
