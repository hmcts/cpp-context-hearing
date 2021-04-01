package uk.gov.moj.cpp.hearing.it;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingForTodayPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsMagistrateUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.BreachType;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.Jurisdiction;
import uk.gov.justice.core.courts.LinkType;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.OffenceActiveOrder;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.core.courts.SummonsTemplateType;
import uk.gov.justice.hearing.courts.CourtApplicationSummaries;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class HearingForTodayIT extends AbstractIT {

    private static final String OFFENCE_TITLE = "OFFENCE TITLE";
    private static final String OFFENCE_WORDING = "OFFENCE WORDING";
    private static final String DEFENDANT_FIRST_NAME = "FIRST_NAME";
    private static final String DEFENDANT_LAST_NAME = "LAST_NAME";
    private static final String APPLICATION_LEGISLATION = "APPLICATION_LEGISLATION";
    private static final String APPLICATION_TYPE = "APPLICATION_TYPE";
    private static final LocalDate DEFENDANT_DOB = LocalDate.now().minusYears(50);

    private static final UUID COURTORDER_PROSECUTION_CASE_ID = randomUUID();
    private static final UUID COURTORDER_PROSECUTION_AUTHORITY_ID = randomUUID();
    private static final String COURTORDER_PROSECUTION_AUTHORITY_CODE = STRING.next();
    private static final String COURTORDER_CASE_URN = STRING.next();
    private static final UUID COURTORDER_MASTER_DEFENDANT_ID = randomUUID();
    private static final String COURTORDER_MASTER_DEFENDANT_FIRSTNAME = STRING.next();
    private static final String COURTORDER_MASTER_DEFENDANT_MIDDLENAME = STRING.next();
    private static final String COURTORDER_MASTER_DEFENDANT_LASTNAME = STRING.next();

    @Test
    public void shouldRetrieveHearingForTodayForLoggedOnUser() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = createHearingForToday(hearingId, courtCentreId, roomId, userId, null);

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        getHearingForTodayPollForMatch(userId, 30, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(greaterThanOrEqualTo(1)))
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getHearingDays, hasSize(2))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(now()))))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(now()))))
                        .with(HearingSummaries::getCourtCentreId, is(courtCentreId))
                        .with(HearingSummaries::getRoomId, is(roomId))
                        .with(this::getDateOfBirth, is(DEFENDANT_DOB))
                        .with(this::getFirstName, is(DEFENDANT_FIRST_NAME))
                        .with(this::getLastName, is(DEFENDANT_LAST_NAME))
                        .with(this::getOffenceTitle, is(OFFENCE_TITLE))
                        .with(this::getOffenceWording, is(OFFENCE_WORDING))))
        );
    }


    @Test
    public void shouldRetrieveApplicationWithCaseHearingForTodayForLoggedOnUser() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = createHearingForToday(hearingId, courtCentreId, roomId, userId, createCourtApplicationWithCase());

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        getHearingForTodayPollForMatch(userId, 30, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(greaterThanOrEqualTo(1)))
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getHearingDays, hasSize(2))
                        .with(HearingSummaries::getCourtApplicationSummaries, hasSize(1))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(now()))))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(now()))))
                        .with(HearingSummaries::getCourtCentreId, is(courtCentreId))
                        .with(HearingSummaries::getRoomId, is(roomId))
                        .with(this::getDateOfBirth, is(DEFENDANT_DOB))
                        .with(this::getFirstName, is(DEFENDANT_FIRST_NAME))
                        .with(this::getLastName, is(DEFENDANT_LAST_NAME))
                        .with(this::getOffenceTitle, is(OFFENCE_TITLE))
                        .with(this::getOffenceWording, is(OFFENCE_WORDING))
                        .with(this::getApplicationType, is(APPLICATION_TYPE))
                        .with(this::extractApplicationLegislation, is(APPLICATION_LEGISLATION))
                ))
        );
    }

    @Test
    public void shouldRetrieveApplicationWithCourtOrderHearingForTodayForLoggedOnUser() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = createHearingForToday(hearingId, courtCentreId, roomId, userId, createCourtApplicationWithCourtOrder());

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        getHearingForTodayPollForMatch(userId, 30, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(greaterThanOrEqualTo(1)))
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getHearingDays, hasSize(2))
                        .with(HearingSummaries::getCourtApplicationSummaries, hasSize(1))
                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getCaseSummaries().get(0).getId(), is(COURTORDER_PROSECUTION_CASE_ID))))

                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getCaseSummaries().get(0)
                                        .getProsecutionCaseIdentifier().getProsecutionAuthorityId(), is(COURTORDER_PROSECUTION_AUTHORITY_ID))))

                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getCaseSummaries().get(0)
                                        .getProsecutionCaseIdentifier().getProsecutionAuthorityCode(), is(COURTORDER_PROSECUTION_AUTHORITY_CODE))))

                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getCaseSummaries().get(0).getProsecutionCaseIdentifier().getCaseURN(), is(COURTORDER_CASE_URN))))

                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getSubject().getFirstName(), is(COURTORDER_MASTER_DEFENDANT_FIRSTNAME))))

                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getSubject().getMiddleName(), is(COURTORDER_MASTER_DEFENDANT_MIDDLENAME))))

                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getSubject().getLastName(), is(COURTORDER_MASTER_DEFENDANT_LASTNAME))))

                        .with(HearingSummaries::getCourtApplicationSummaries, hasItem(isBean(CourtApplicationSummaries.class)
                                .with(courtApplicationSummaries -> courtApplicationSummaries.getSubject().getMasterDefendantId(), is(COURTORDER_MASTER_DEFENDANT_ID))))

                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(now()))))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(now()))))
                        .with(HearingSummaries::getCourtCentreId, is(courtCentreId))
                        .with(HearingSummaries::getRoomId, is(roomId))
                        .with(this::getDateOfBirth, is(DEFENDANT_DOB))
                        .with(this::getFirstName, is(DEFENDANT_FIRST_NAME))
                        .with(this::getLastName, is(DEFENDANT_LAST_NAME))
                        .with(this::getOffenceTitle, is(OFFENCE_TITLE))
                        .with(this::getOffenceWording, is(OFFENCE_WORDING))
                        .with(this::getApplicationType, is(APPLICATION_TYPE))
                        .with(this::extractApplicationLegislation, is(APPLICATION_LEGISLATION))
                ))
        );
    }

    private InitiateHearingCommand createHearingForToday(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final UUID userId, List<CourtApplication> courtApplicationList) {
        final UUID prosecutionCaseId = randomUUID();
        return initiateHearingCommand()
                .setHearing(hearing()
                        .withId(hearingId)
                        .withCourtCentre(courtCentre()
                                .withId(courtCentreId)
                                .withName("Lavender hill")
                                .withRoomId(roomId)
                                .build())
                        .withHearingDays(asList(
                                hearingDay()
                                        .withListedDurationMinutes(10)
                                        .withListingSequence(0)
                                        .withSittingDay(ZonedDateTime.now())
                                        .build(),
                                hearingDay()
                                        .withListedDurationMinutes(10)
                                        .withListingSequence(1)
                                        .withSittingDay(ZonedDateTime.now().plusDays(1))
                                        .build(),
                                hearingDay()
                                        .withListedDurationMinutes(10)
                                        .withListingSequence(2)
                                        .withSittingDay(ZonedDateTime.now().plusSeconds(30))
                                        .build()

                        ))
                        .withJudiciary(singletonList(judicialRole()
                                .withJudicialId(randomUUID())
                                .withJudicialRoleType(judicialRoleType()
                                        .withJudiciaryType("Type")
                                        .build())
                                .withUserId(userId)
                                .build()))
                        .withProsecutionCases(singletonList(prosecutionCase()
                                .withId(prosecutionCaseId)
                                .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withProsecutionAuthorityCode("code")
                                        .withCaseURN("caseURN")
                                        .build())
                                .withInitiationCode(InitiationCode.J)
                                .withDefendants(singletonList(defendant()
                                        .withId(randomUUID())
                                        .withCourtProceedingsInitiated(ZonedDateTime.now())
                                        .withMasterDefendantId(randomUUID())
                                        .withProsecutionCaseId(prosecutionCaseId)
                                        .withPersonDefendant(personDefendant()
                                                .withPersonDetails(person()
                                                        .withFirstName(DEFENDANT_FIRST_NAME)
                                                        .withLastName(DEFENDANT_LAST_NAME)
                                                        .withDateOfBirth(DEFENDANT_DOB).withGender(MALE)
                                                        .build())
                                                .build())
                                        .withOffences(singletonList(offence()
                                                .withId(randomUUID())
                                                .withOffenceDefinitionId(randomUUID())
                                                .withOffenceCode("code")
                                                .withStartDate(now().plusDays(10))
                                                .withOffenceTitle(OFFENCE_TITLE)
                                                .withWording(OFFENCE_WORDING)
                                                .withReportingRestrictions(Arrays.asList(ReportingRestriction.reportingRestriction().withId(randomUUID()).withLabel("Yes")
                                                        .withJudicialResultId(randomUUID()).build()))
                                                .build()))
                                        .build()))
                                .build()))
                        .withCourtApplications(courtApplicationList)
                        .withJurisdictionType(MAGISTRATES)
                        .withType(hearingType()
                                .withId(randomUUID())
                                .withDescription("Trial")
                                .build())
                        .build());
    }

    private List<CourtApplication> createCourtApplicationWithCase() {
        CourtApplication courtApplication = CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicationReceivedDate(now())
                .withApplicationStatus(ApplicationStatus.LISTED)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withId(randomUUID())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .build())
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withId(randomUUID())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .build())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withProsecutionCaseId(randomUUID())
                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                .withProsecutionAuthorityId(randomUUID())
                                .withProsecutionAuthorityCode(STRING.next())
                                .withCaseURN(STRING.next())
                                .build())
                        .build()))
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(randomUUID())
                        .withCategoryCode("Application Category")
                        .withLinkType(LinkType.LINKED)
                        .withType(APPLICATION_TYPE)
                        .withLegislation(APPLICATION_LEGISLATION)
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
        return asList(courtApplication);
    }

    private List<CourtApplication> createCourtApplicationWithCourtOrder() {
        CourtApplication courtApplication = CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicationReceivedDate(now())
                .withApplicationStatus(ApplicationStatus.LISTED)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withId(randomUUID())
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(COURTORDER_MASTER_DEFENDANT_ID)
                                .withPersonDefendant(personDefendant()
                                        .withPersonDetails(person()
                                                .withFirstName(COURTORDER_MASTER_DEFENDANT_FIRSTNAME)
                                                .withMiddleName(COURTORDER_MASTER_DEFENDANT_MIDDLENAME)
                                                .withLastName(COURTORDER_MASTER_DEFENDANT_LASTNAME)
                                                .withGender(MALE)
                                                .build())
                                        .build())
                                .build())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .build())
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withId(randomUUID())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .build())
                .withCourtOrder(CourtOrder.courtOrder()
                        .withId(randomUUID())
                        .withJudicialResultTypeId(randomUUID())
                        .withLabel(STRING.next())
                        .withOrderDate(now())
                        .withStartDate(now())
                        .withOrderingCourt(courtCentre()
                                .withId(randomUUID())
                                .withName(STRING.next())
                                .build())
                        .withOrderingHearingId(randomUUID())
                        .withIsSJPOrder(false)
                        .withCanBeSubjectOfBreachProceedings(false)
                        .withCanBeSubjectOfVariationProceedings(false)
                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                .withProsecutionCaseId(COURTORDER_PROSECUTION_CASE_ID)
                                .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                        .withCaseURN(COURTORDER_CASE_URN)
                                        .withProsecutionAuthorityCode(COURTORDER_PROSECUTION_AUTHORITY_CODE)
                                        .withProsecutionAuthorityId(COURTORDER_PROSECUTION_AUTHORITY_ID)
                                        .build())
                                .withOffence(offence()
                                        .withId(randomUUID())
                                        .withOffenceDefinitionId(randomUUID())
                                        .withOffenceCode(STRING.next())
                                        .withOffenceTitle(STRING.next())
                                        .withWording(STRING.next())
                                        .withStartDate(now())
                                        .build())
                                .build()))
                        .build())
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(randomUUID())
                        .withCategoryCode("Application Category")
                        .withLinkType(LinkType.LINKED)
                        .withType(APPLICATION_TYPE)
                        .withLegislation(APPLICATION_LEGISLATION)
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
        return asList(courtApplication);
    }

    private LocalDate getDateOfBirth(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getDateOfBirth();
    }

    private String getFirstName(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getFirstName();
    }

    private String getLastName(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getLastName();
    }

    private String getOffenceTitle(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getOffences().get(0).getOffenceTitle();
    }

    private String getOffenceWording(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getOffences().get(0).getWording();
    }

    private String extractApplicationLegislation(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getCourtApplicationSummaries().get(0).getType().getLegislation();
    }

    private String getApplicationType(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getCourtApplicationSummaries().get(0).getType().getType();
    }
}