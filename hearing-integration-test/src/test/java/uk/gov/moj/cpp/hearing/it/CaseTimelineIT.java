package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
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
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithGroupProceedings;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithIsBoxHearing;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VACATED_TRIAL_TYPE_ID;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.test.TestUtilities;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;

import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Matcher;
import org.junit.Test;

@NotThreadSafe
public class CaseTimelineIT extends AbstractIT {

    private Hearing hearing;
    private HearingDay hearingDay;

    @Test
    public void shouldDisplayCaseTimeline() throws Exception {
        setUpHearing(now(ZoneId.of("UTC")).plusDays(1L), null);
        stubCourtRoom(hearing);
        stubProsecutionCases(hearing);
        stubCourtRoom(hearing);
        final String hearingDate = hearingDay.getSittingDay().toLocalDate().format(ofPattern("dd MMM yyyy"));
        verifyTimeline(hearingDate, false);

        final UUID caseId = hearing.getProsecutionCases().get(0).getId();

        // change to vacated
        final UUID hearingId = hearing.getId();
        setTrialType(getRequestSpec(), hearingId, builder()
                .withHearingId(hearingId)
                .withVacatedTrialReasonId(VACATED_TRIAL_TYPE_ID)
                .build());
        pollForHearingSummaryTimeline(withJsonPath("$.hearingSummaries[0].outcome", is("Vacated")), caseId);

        // change to effective
        setTrialType(getRequestSpec(), hearingId, builder()
                .withHearingId(hearingId)
                .withIsEffectiveTrial(true)
                .build());
        pollForHearingSummaryTimeline(withJsonPath("$.hearingSummaries[0].outcome", is("Effective")), caseId);

        // change to vacated initiated from listing world
        UseCases.updateHearingVacatedTrialDetail(new HearingVacatedTrialDetailsUpdateCommand(hearingId, VACATED_TRIAL_TYPE_ID, true, true));
        pollForHearingSummaryTimeline(withJsonPath("$.hearingSummaries[0].outcome", is("Vacated")), caseId);

    }

    @Test
    public void shouldDisplayCaseTimelineWithIsBoxHearing() throws Exception {
        setUpHearing(now(ZoneId.of("UTC")).plusDays(1L), true);
        stubCourtRoom(hearing);
        stubProsecutionCases(hearing);
        final String hearingDate = hearingDay.getSittingDay().toLocalDate().format(ofPattern("dd MMM yyyy"));
        verifyTimeline(hearingDate, true);
    }

    @Test
    public void shouldDisplayCaseTimelineWithOutIsBoxHearing() throws Exception {
        setUpHearing(now(ZoneId.of("UTC")).plusDays(1L), false);
        stubCourtRoom(hearing);
        stubProsecutionCases(hearing);
        final String hearingDate = hearingDay.getSittingDay().toLocalDate().format(ofPattern("dd MMM yyyy"));
        verifyTimeline(hearingDate, false);
    }

    private void setUpHearing(final ZonedDateTime sittingDay, final Boolean isBoxHearing) {
        final InitiateHearingCommand initiateHearingCommand = isBoxHearing == null ? standardInitiateHearingTemplate() : standardInitiateHearingTemplateWithIsBoxHearing(isBoxHearing);
        hearing = initiateHearingCommand.getHearing();
        hearingDay = hearing.getHearingDays().get(0);
        hearingDay.setSittingDay(sittingDay);

        h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final TrialType addTrialType = builder()
                .withHearingId(hearing.getId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        setTrialType(getRequestSpec(), hearing.getId(), addTrialType);
    }

    private void verifyTimeline(final String hearingDate, final boolean isBoxHearing) {
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final UUID prosecutionCaseId = prosecutionCase.getId();

        final String hearingTime = hearingDay.getSittingDay().toLocalTime().format(ofPattern("HH:mm").withZone(ZoneId.of("UTC")));
        final String hearingType = hearing.getType().getDescription();
        final String courtHouse = hearing.getCourtCentre().getName();
        final String courtRoom = hearing.getCourtCentre().getRoomName();
        final Integer listedDurationMinutes = hearingDay.getListedDurationMinutes();
        final Person personDetails = prosecutionCase.getDefendants().get(0).getPersonDefendant().getPersonDetails();
        final String defendant = String.format("%s %s", personDetails.getFirstName(), personDetails.getLastName());

        final Matcher<ReadContext> timelineMatcher = allOf(
                withJsonPath("$.hearingSummaries[0].hearingId", is(hearing.getId().toString())),
                withJsonPath("$.hearingSummaries[0].hearingDate", is(hearingDate)),
                withJsonPath("$.hearingSummaries[0].hearingType", is(hearingType)),
                withJsonPath("$.hearingSummaries[0].courtHouse", is(courtHouse)),
                withJsonPath("$.hearingSummaries[0].courtRoom", is(courtRoom)),
                withJsonPath("$.hearingSummaries[0].hearingTime", is(hearingTime)),
                withJsonPath("$.hearingSummaries[0].estimatedDuration", is(listedDurationMinutes)),
                withJsonPath("$.hearingSummaries[0].defendants[0].name", is(defendant)),
                withJsonPath("$.hearingSummaries[0].outcome", is("InEffective")),
                withJsonPath("$.hearingSummaries[0].applications[0].applicationId", is(hearing.getCourtApplications().get(0).getId().toString())),
                withJsonPath("$.hearingSummaries[0].applications[0].applicants[0].id", is(hearing.getCourtApplications().get(0).getApplicant().getId().toString())),
                withJsonPath("$.hearingSummaries[0].applications[0].respondents[0].id", is(hearing.getCourtApplications().get(0).getRespondents().get(0).getId().toString())),
                withJsonPath("$.hearingSummaries[0].applications[0].subjects[0].id", is(hearing.getCourtApplications().get(0).getSubject().getId().toString())),
                isBoxHearing ? withJsonPath("$.hearingSummaries[0].isBoxHearing", is(true)) : hasNoJsonPath("$.hearingSummaries[0].isBoxHearing")
        );


        pollForHearingSummaryTimeline(timelineMatcher, prosecutionCaseId);
    }

    private void pollForHearingSummaryTimeline(final Matcher<? super ReadContext> timelineMatcher, final UUID prosecutionCaseId) {
        final String timelineQueryAPIEndPoint = format(ENDPOINT_PROPERTIES.getProperty("hearing.case.timeline"), prosecutionCaseId);
        final String timelineURL = getBaseUri() + "/" + timelineQueryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.case.timeline+json";
        poll(requestParams(timelineURL, mediaType).withHeader(USER_ID, getLoggedInUser()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(timelineMatcher));
    }

    @Test
    public void shouldFetchDefndantCorrespondingToCaseId() throws Exception {
        final UUID groupId = randomUUID();
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = getUuidMapForCivilCaseStructure(3);
        final Iterator<UUID> iterator = caseStructure.keySet().iterator();
        final UUID masterCaseId = iterator.next();

        final InitiateHearingCommand hearingCommand = standardInitiateHearingTemplateWithGroupProceedings(caseStructure, groupId, masterCaseId);
        h(initiateHearing(getRequestSpec(), hearingCommand));
        final Hearing hearing = hearingCommand.getHearing();

        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(1);
        final UUID prosecutionCaseId = prosecutionCase.getId();

        final Person personDetails = prosecutionCase.getDefendants().get(0).getPersonDefendant().getPersonDetails();

        final String defendantName = String.format("%s %s", personDetails.getFirstName(), personDetails.getLastName());

        final Matcher<ReadContext> timelineMatcher = allOf(
                withJsonPath("$.hearingSummaries[0].hearingId", is(hearing.getId().toString())),
                withJsonPath("$.hearingSummaries[0].defendants[0].name", is(defendantName))
        );

        pollForHearingSummaryTimeline(timelineMatcher, prosecutionCaseId);
    }

    private HashMap<UUID, Map<UUID, List<UUID>>> getUuidMapForCivilCaseStructure(int count) {
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        for (int i = 0; i < count; i++) {
            caseStructure.put(randomUUID(), toMap(randomUUID(), TestUtilities.asList(randomUUID())));
        }
        return caseStructure;
    }
}