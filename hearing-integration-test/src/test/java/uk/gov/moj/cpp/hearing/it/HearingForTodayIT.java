package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.core.courts.ApplicationJurisdictionType;
import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.LinkType;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingForTodayPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsMagistrateUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

public class HearingForTodayIT extends AbstractIT {

    private static final String OFFENCE_TITLE = "OFFENCE TITLE";
    private static final String OFFENCE_WORDING = "OFFENCE WORDING";
    private static final String DEFENDANT_FIRST_NAME = "FIRST_NAME";
    private static final String DEFENDANT_LAST_NAME = "LAST_NAME";
    private static final String APPLICATION_LEGISLATION = "APPLICATION_LEGISLATION";
    private static final String APPLICATION_TYPE = "APPLICATION_TYPE";
    private static final LocalDate DEFENDANT_DOB = LocalDate.now().minusYears(50);


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
    public void shouldRetrieveApplicationHearingForTodayForLoggedOnUser() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = createHearingForToday(hearingId, courtCentreId, roomId, userId, createCourtApplication());

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

    private List<CourtApplication> createCourtApplication() {
        CourtApplication courtApplication = CourtApplication.courtApplication()
                                             .withId(randomUUID())
                                             .withApplicationReceivedDate(LocalDate.now())
                                             .withApplicationStatus(ApplicationStatus.LISTED)
                                             .withApplicant(CourtApplicationParty.courtApplicationParty().withId(randomUUID()).build())
                                             .withType(CourtApplicationType.courtApplicationType()
                                                     .withId(randomUUID())
                                                     .withApplicationCategory("Application Category")
                                                     .withLinkType(LinkType.LINKED)
                                                     .withApplicationType(APPLICATION_TYPE)
                                                     .withApplicationLegislation(APPLICATION_LEGISLATION)
                                                     .withApplicationJurisdictionType(ApplicationJurisdictionType.MAGISTRATES)
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
        return hearingSummaries.getCourtApplicationSummaries().get(0).getType().getApplicationLegislation();
    }

    private String getApplicationType(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getCourtApplicationSummaries().get(0).getType().getApplicationType();
    }
}