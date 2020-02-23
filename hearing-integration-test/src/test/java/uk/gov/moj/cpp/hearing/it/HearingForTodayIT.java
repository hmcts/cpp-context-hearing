package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
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
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsMagistrateUser;

public class HearingForTodayIT extends AbstractIT {

    private static final String OFFENCE_TITLE = "OFFENCE TITLE";
    private static final String OFFENCE_WORDING = "OFFENCE WORDING";
    private static final String DEFENDANT_FIRST_NAME = "FIRST_NAME";
    private static final String DEFENDANT_LAST_NAME = "LAST_NAME";
    private static final LocalDate DEFENDANT_DOB = LocalDate.now().minusYears(50);


    @Test
    public void shouldRetrieveHearingForTodayForLoggedOnUser() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = createHearingForToday(hearingId, courtCentreId, roomId, userId);

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        getHearingForTodayPollForMatch(userId, 30, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(1))
                .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getHearingDays, hasSize(2))
                        .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                .with(hearingDay -> hearingDay.getSittingDay().toLocalDate(), is(now()))))
                        .with(HearingSummaries::getHearingDays, second(isBean(HearingDay.class)
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


    private InitiateHearingCommand createHearingForToday(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final UUID userId) {
        final UUID prosecutionCaseId = randomUUID();
        return initiateHearingCommand()
                .setHearing(hearing()
                        .withId(hearingId)
                        .withCourtCentre(courtCentre()
                                .withId(courtCentreId)
                                .withRoomId(roomId)
                                .build())
                        .withHearingDays(Arrays.asList(
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
                                                .build()))
                                        .build()))
                                .build()))
                        .withJurisdictionType(MAGISTRATES)
                        .withType(hearingType()
                                .withId(randomUUID())
                                .withDescription("Trial")
                                .build())
                        .build());
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

}