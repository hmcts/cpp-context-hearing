package uk.gov.moj.cpp.hearing.it;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.CourtCentre.courtCentre;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Gender.MALE;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.justice.core.courts.HearingType.hearingType;
import static uk.gov.justice.core.courts.JudicialRole.judicialRole;
import static uk.gov.justice.core.courts.JudicialRoleType.judicialRoleType;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.core.courts.Offence.offence;
import static uk.gov.justice.core.courts.Person.person;
import static uk.gov.justice.core.courts.PersonDefendant.personDefendant;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.it.Queries.jsonPayloadMatchesBean;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUserByGivenGroup;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.Jurisdiction;
import uk.gov.justice.core.courts.LinkType;
import uk.gov.justice.core.courts.SummonsTemplateType;
import uk.gov.justice.core.courts.BreachType;
import uk.gov.justice.core.courts.OffenceActiveOrder;
import uk.gov.justice.hearing.courts.Defendants;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.hearing.courts.ProsecutionCaseSummaries;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.test.utils.core.http.RequestParams;
import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

public class HearingCheckInIT extends AbstractIT {

    private static final String INACTIVE_CASE_URN = "INACTIVE_URN";
    private static final String INACTIVE_DEFENDANT_FIRST_NAME = "INACTIVE_FIRST";
    private static final String INACTIVE_DEFENDANT_LAST_NAME = "INACTIVE_LAST";

    @Test
    public void shouldReturnInactiveApplicationCaseNotPresentInHaCaseForHearing() {
        final UUID userId = randomUUID();
        setupAsAuthorisedUserByGivenGroup(userId, "Court Clerks");
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final UUID inactiveCaseId = randomUUID();
        final UUID inactiveDefendantId = randomUUID();

        // hearing 1 owns the (now inactive) case in ha_case
        initiateHearing(getRequestSpec(), hearingWithCase(randomUUID(), courtCentreId, roomId, userId, inactiveCaseId,
                inactiveDefendantId, INACTIVE_CASE_URN, INACTIVE_DEFENDANT_FIRST_NAME, INACTIVE_DEFENDANT_LAST_NAME, null));

        // hearing 2 has its own case, and an application pointing at the inactive case only via court_application_json
        final UUID hearingId = randomUUID();
        final UUID ownCaseId = randomUUID();
        initiateHearing(getRequestSpec(), hearingWithCase(hearingId, courtCentreId, roomId, userId, ownCaseId,
                randomUUID(), "OWN_URN", "OWN_FIRST", "OWN_LAST",
                singletonList(applicationWithCaseStatus(inactiveCaseId, "inactive"))));

        pollForMatch(courtCentreId, 30, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(2))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasItem(isBean(ProsecutionCaseSummaries.class)
                                .with(ProsecutionCaseSummaries::getId, is(ownCaseId))))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasItem(isBean(ProsecutionCaseSummaries.class)
                                .with(ProsecutionCaseSummaries::getId, is(inactiveCaseId))
                                .with(pc -> pc.getProsecutionCaseIdentifier().getCaseURN(), is(INACTIVE_CASE_URN))
                                .with(ProsecutionCaseSummaries::getDefendants, hasItem(isBean(Defendants.class)
                                        .with(Defendants::getId, is(inactiveDefendantId))
                                        .with(Defendants::getFirstName, is(INACTIVE_DEFENDANT_FIRST_NAME))
                                        .with(Defendants::getLastName, is(INACTIVE_DEFENDANT_LAST_NAME)))))))));
    }

    @Test
    public void shouldNotReturnActiveApplicationCaseNotPresentInHaCaseForHearing() {
        final UUID userId = randomUUID();
        setupAsAuthorisedUserByGivenGroup(userId, "Court Clerks");
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final UUID otherCaseId = randomUUID();

        initiateHearing(getRequestSpec(), hearingWithCase(randomUUID(), courtCentreId, roomId, userId, otherCaseId,
                randomUUID(), "OTHER_URN", "F", "L", null));

        final UUID hearingId = randomUUID();
        final UUID ownCaseId = randomUUID();
        initiateHearing(getRequestSpec(), hearingWithCase(hearingId, courtCentreId, roomId, userId, ownCaseId,
                randomUUID(), "OWN_URN", "OWN_FIRST", "OWN_LAST",
                singletonList(applicationWithCaseStatus(otherCaseId, "ACTIVE"))));

        pollForMatch(courtCentreId, 30, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(1))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasItem(isBean(ProsecutionCaseSummaries.class)
                                .with(ProsecutionCaseSummaries::getId, is(ownCaseId)))))));
    }

    private static void pollForMatch(final UUID courtCentreId, final long timeout, final BeanMatcher<GetHearings> resultMatcher) {
        final RequestParams requestParams = requestParams(getURL("hearing.get.hearings-check-in", now().toString(), courtCentreId.toString()),
                "application/vnd.hearing.get.hearings-check-in+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser())
                .build();
        final Matcher<ResponseData> expectedConditions = allOf(status().is(OK), jsonPayloadMatchesBean(GetHearings.class, resultMatcher));
        poll(requestParams).timeout(timeout, TimeUnit.SECONDS).until(status().is(OK), expectedConditions);
    }

    private InitiateHearingCommand hearingWithCase(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final UUID userId,
                                                   final UUID caseId, final UUID defendantId, final String caseUrn,
                                                   final String firstName, final String lastName,
                                                   final List<CourtApplication> courtApplications) {
        return initiateHearingCommand()
                .setHearing(hearing()
                        .withId(hearingId)
                        .withCourtCentre(courtCentre().withId(courtCentreId).withName("Lavender hill").withRoomId(roomId).build())
                        .withHearingDays(singletonList(hearingDay()
                                .withListedDurationMinutes(10)
                                .withListingSequence(0)
                                .withSittingDay(ZonedDateTime.now())
                                .build()))
                        .withJudiciary(singletonList(judicialRole()
                                .withJudicialId(randomUUID())
                                .withJudicialRoleType(judicialRoleType().withJudiciaryType("Type").build())
                                .withUserId(userId)
                                .build()))
                        .withProsecutionCases(singletonList(prosecutionCase()
                                .withId(caseId)
                                .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withProsecutionAuthorityCode("code")
                                        .withCaseURN(caseUrn)
                                        .build())
                                .withInitiationCode(InitiationCode.J)
                                .withDefendants(singletonList(defendant()
                                        .withId(defendantId)
                                        .withCourtProceedingsInitiated(ZonedDateTime.now())
                                        .withMasterDefendantId(randomUUID())
                                        .withProsecutionCaseId(caseId)
                                        .withPersonDefendant(personDefendant()
                                                .withPersonDetails(person()
                                                        .withFirstName(firstName)
                                                        .withLastName(lastName)
                                                        .withDateOfBirth(now().minusYears(40))
                                                        .withGender(MALE)
                                                        .build())
                                                .build())
                                        .withOffences(singletonList(offence()
                                                .withId(randomUUID())
                                                .withOffenceDefinitionId(randomUUID())
                                                .withOffenceCode("code")
                                                .withStartDate(now().plusDays(10))
                                                .withOffenceTitle("OFFENCE TITLE")
                                                .withWording("OFFENCE WORDING")
                                                .build()))
                                        .build()))
                                .build()))
                        .withCourtApplications(courtApplications)
                        .withJurisdictionType(MAGISTRATES)
                        .withType(hearingType().withId(randomUUID()).withDescription("Trial").build())
                        .build());
    }

    private CourtApplication applicationWithCaseStatus(final UUID prosecutionCaseId, final String caseStatus) {
        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicationReceivedDate(now())
                .withApplicationStatus(ApplicationStatus.LISTED)
                .withSubject(CourtApplicationParty.courtApplicationParty().withId(randomUUID()).withSummonsRequired(false).withNotificationRequired(false).build())
                .withApplicant(CourtApplicationParty.courtApplicationParty().withId(randomUUID()).withSummonsRequired(false).withNotificationRequired(false).build())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus(caseStatus)
                        .withProsecutionCaseId(prosecutionCaseId)
                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                .withProsecutionAuthorityId(randomUUID())
                                .withProsecutionAuthorityCode("code")
                                .withCaseURN("APPLICATION_URN")
                                .build())
                        .build()))
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(randomUUID())
                        .withCategoryCode("Application Category")
                        .withCode("TypeCode")
                        .withLinkType(LinkType.LINKED)
                        .withType("APPLICATION_TYPE")
                        .withLegislation("APPLICATION_LEGISLATION")
                        .withJurisdiction(Jurisdiction.MAGISTRATES)
                        .withSummonsTemplateType(SummonsTemplateType.BREACH)
                        .withBreachType(BreachType.NOT_APPLICABLE)
                        .withAppealFlag(false)
                        .withApplicantAppellantFlag(false)
                        .withPleaApplicableFlag(false)
                        .withCommrOfOathFlag(false)
                        .withCourtOfAppealFlag(false)
                        .withCourtExtractAvlFlag(false)
                        .withProsecutorThirdPartyFlag(false)
                        .withSpiOutApplicableFlag(false)
                        .withOffenceActiveOrder(OffenceActiveOrder.NOT_APPLICABLE)
                        .build())
                .build();
    }
}
