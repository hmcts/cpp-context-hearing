package uk.gov.moj.cpp.hearing.it;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingForTomorrowPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class FutureHearingIT extends AbstractIT {

    private static final String OFFENCE_TITLE = "OFFENCE TITLE";
    private static final String OFFENCE_WORDING = "OFFENCE WORDING";
    private static final String DEFENDANT_FIRST_NAME = "FIRST_NAME";
    private static final String DEFENDANT_LAST_NAME = "LAST_NAME";
    private static final String COURT_CENTRE_NAME = "Lavender hill";
    private static final String CASE_URN = "caseURN";
    private static final String TRIAL = "Trial";
    private static final ZonedDateTime NOW = new UtcClock().now();

    @Test
    public void shouldGetFutureHearingsByCaseIds() {
        final UUID userId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = createHearingForTomorrow(userId, hearingId, prosecutionCaseId,
                courtCentreId, roomId, NOW.plusDays(2), NOW.plusDays(3), NOW.plusDays(1));

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        getHearingForTomorrowPollForMatch(userId, prosecutionCaseId, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(greaterThanOrEqualTo(1)))
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getHearingDays, hasSize(3))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(NOW.toLocalDate().plusDays(1)))))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(NOW.toLocalDate().plusDays(2)))))
                        .with(HearingSummaries::getHearingDays, hasItem(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(NOW.toLocalDate().plusDays(3)))))
                        .with(this::getCourtCentreId, is(courtCentreId))
                        .with(this::getCourtCentreName, is(COURT_CENTRE_NAME))
                        .with(this::getCourtCentreRoomId, is(roomId))
                        .with(this::getHearingLanguage, is("ENGLISH"))
                        .with(this::getProsecutionCaseIdentifierCaseUrn, is(CASE_URN))
                        .with(this::getTypeDescription, is(TRIAL))
                        .with(this::getFirstName, is(DEFENDANT_FIRST_NAME))
                        .with(this::getLastName, is(DEFENDANT_LAST_NAME))
                        .with(this::getOffenceTitle, is(OFFENCE_TITLE))
                        .with(this::getOffenceWording, is(OFFENCE_WORDING))))
        );
    }

    @Test
    public void shouldGetNullFutureHearingsByCaseIdsPasDatedRecords() {
        final UUID userId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = createHearingForTomorrow(userId, hearingId, prosecutionCaseId,
                courtCentreId, roomId, NOW.minusDays(2), NOW.minusDays(3), NOW.minusDays(1));

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        getHearingForTomorrowPollForMatch(userId, prosecutionCaseId, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, nullValue())
        );
    }

    @Test
    public void shouldGetNullGetHearingsByCaseIdsNoDBRecord() {
        final UUID userId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();

        getHearingForTomorrowPollForMatch(userId, prosecutionCaseId, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, nullValue())
        );
    }

    private InitiateHearingCommand createHearingForTomorrow(final UUID userId, final UUID hearingId, final UUID prosecutionCaseId,
                                                            final UUID courtCentreId, final UUID roomId, final ZonedDateTime zonedDateTime1,
                                                            final ZonedDateTime zonedDateTime2, final ZonedDateTime zonedDateTime3) {
        return initiateHearingCommand()
                .setHearing(hearing()
                        .withId(hearingId)
                        .withCourtCentre(courtCentre()
                                .withId(courtCentreId)
                                .withName(COURT_CENTRE_NAME)
                                .withRoomId(roomId)
                                .build())
                        .withHearingDays(asList(
                                hearingDay()
                                        .withListedDurationMinutes(10)
                                        .withListingSequence(0)
                                        .withSittingDay(zonedDateTime1)
                                        .build(),
                                hearingDay()
                                        .withListedDurationMinutes(10)
                                        .withListingSequence(1)
                                        .withSittingDay(zonedDateTime2)
                                        .build(),
                                hearingDay()
                                        .withListedDurationMinutes(10)
                                        .withListingSequence(2)
                                        .withSittingDay(zonedDateTime3)
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
                                        .withCaseURN(CASE_URN)
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
                                                        .withDateOfBirth(LocalDate.now().minusYears(50))
                                                        .withGender(MALE)
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
                        .withCourtApplications(null)
                        .withJurisdictionType(MAGISTRATES)
                        .withType(hearingType()
                                .withId(randomUUID())
                                .withDescription(TRIAL)
                                .build())
                        .build());
    }

    private UUID getCourtCentreId(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getCourtCentre().getId();
    }

    private String getCourtCentreName(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getCourtCentre().getName();
    }

    private UUID getCourtCentreRoomId(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getCourtCentre().getRoomId();
    }

    private String getHearingLanguage(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getHearingLanguage();
    }

    private String getProsecutionCaseIdentifierCaseUrn(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getProsecutionCaseSummaries().get(0).getProsecutionCaseIdentifier().getCaseURN();
    }

    private String getTypeDescription(final HearingSummaries hearingSummaries) {
        return hearingSummaries.getType().getDescription();
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
}
