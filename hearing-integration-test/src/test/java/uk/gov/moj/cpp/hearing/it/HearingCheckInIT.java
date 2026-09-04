package uk.gov.moj.cpp.hearing.it;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.ApplicationStatus.LISTED;
import static uk.gov.justice.core.courts.CourtCentre.courtCentre;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Gender.MALE;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.justice.core.courts.HearingType.hearingType;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.core.courts.LinkType.LINKED;
import static uk.gov.justice.core.courts.Person.person;
import static uk.gov.justice.core.courts.PersonDefendant.personDefendant;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier;
import static uk.gov.justice.core.courts.SummonsTemplateType.BREACH;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingsCheckInPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsMagistrateUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.BreachType;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.Jurisdiction;
import uk.gov.justice.core.courts.OffenceActiveOrder;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * CAD-1609: an application hearing whose case has no active offences carries no
 * ha_case/prosecutionCases row at all (see
 * InitiateHearingCommandHandler.enrichWithActiveApplicationOffences) — the check-in list must
 * still surface the hearing, with the defence/prosecution parties coming from the application.
 */
public class HearingCheckInIT extends AbstractIT {

    private static final String DEFENDANT_FIRST_NAME = "FIRST_NAME";
    private static final String DEFENDANT_LAST_NAME = "LAST_NAME";
    private static final String APPLICATION_TYPE = "APPLICATION_TYPE";

    @Test
    public void shouldRetrieveHearingWithProsecutionCaseForCheckIn() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final LocalDate hearingDate = now();

        initiateHearing(getRequestSpec(), createHearingWithCase(hearingId, courtCentreId, roomId, hearingDate, null));

        getHearingsCheckInPollForMatch(courtCentreId, hearingDate.toString(), isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(greaterThanOrEqualTo(1)))
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getFirstName(), is(DEFENDANT_FIRST_NAME))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getLastName(), is(DEFENDANT_LAST_NAME))
                ))
        );
    }

    @Test
    public void shouldRetrieveApplicationHearingWithNoProsecutionCaseForCheckIn() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final LocalDate hearingDate = now();

        // no prosecution case attached to the hearing at all — only a court application, as
        // happens when the application's case has no active offences (an "inactive case").
        initiateHearing(getRequestSpec(), createHearingWithApplicationOnly(hearingId, courtCentreId, roomId, hearingDate));

        getHearingsCheckInPollForMatch(courtCentreId, hearingDate.toString(), isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(greaterThanOrEqualTo(1)))
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getProsecutionCaseSummaries, is(empty()))
                        .with(HearingSummaries::getCourtApplicationSummaries, hasSize(1))
                        .with(hs -> hs.getCourtApplicationSummaries().get(0).getSubject().getFirstName(), is(DEFENDANT_FIRST_NAME))
                        .with(hs -> hs.getCourtApplicationSummaries().get(0).getSubject().getLastName(), is(DEFENDANT_LAST_NAME))
                ))
        );
    }

    private InitiateHearingCommand createHearingWithCase(final UUID hearingId, final UUID courtCentreId, final UUID roomId,
                                                          final LocalDate hearingDate, final List<CourtApplication> courtApplications) {
        final UUID prosecutionCaseId = randomUUID();
        return initiateHearingCommand()
                .setHearing(hearing()
                        .withId(hearingId)
                        .withCourtCentre(courtCentre()
                                .withId(courtCentreId)
                                .withName("Lavender hill")
                                .withRoomId(roomId)
                                .build())
                        .withHearingDays(singletonList(hearingDay()
                                .withListedDurationMinutes(10)
                                .withListingSequence(0)
                                .withSittingDay(ZonedDateTime.now())
                                .withCourtRoomId(roomId)
                                .withCourtCentreId(courtCentreId)
                                .build()))
                        .withProsecutionCases(singletonList(prosecutionCase()
                                .withId(prosecutionCaseId)
                                .withInitiationCode(InitiationCode.J)
                                .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withProsecutionAuthorityCode("code")
                                        .withCaseURN("caseURN")
                                        .build())
                                .withDefendants(singletonList(defendant()
                                        .withId(randomUUID())
                                        .withCourtProceedingsInitiated(ZonedDateTime.now())
                                        .withMasterDefendantId(randomUUID())
                                        .withProsecutionCaseId(prosecutionCaseId)
                                        .withPersonDefendant(personDefendant()
                                                .withPersonDetails(person()
                                                        .withFirstName(DEFENDANT_FIRST_NAME)
                                                        .withLastName(DEFENDANT_LAST_NAME)
                                                        .withGender(MALE)
                                                        .build())
                                                .build())
                                        .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                                                .withId(randomUUID())
                                                .withOffenceDefinitionId(randomUUID())
                                                .withOffenceCode("code")
                                                .withStartDate(now())
                                                .withOffenceTitle("OFFENCE_TITLE")
                                                .withWording("OFFENCE_WORDING")
                                                .build()))
                                        .build()))
                                .build()))
                        .withCourtApplications(courtApplications)
                        .withJurisdictionType(MAGISTRATES)
                        .withType(hearingType()
                                .withId(randomUUID())
                                .withDescription("Trial")
                                .build())
                        .build());
    }

    private InitiateHearingCommand createHearingWithApplicationOnly(final UUID hearingId, final UUID courtCentreId, final UUID roomId,
                                                                     final LocalDate hearingDate) {
        final InitiateHearingCommand command = createHearingWithCase(hearingId, courtCentreId, roomId, hearingDate, asList(createCourtApplication()));
        command.getHearing().setProsecutionCases(null);
        return command;
    }

    private CourtApplication createCourtApplication() {
        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicationReceivedDate(now())
                .withApplicationStatus(LISTED)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withId(randomUUID())
                        .withPersonDetails(person()
                                .withFirstName(DEFENDANT_FIRST_NAME)
                                .withLastName(DEFENDANT_LAST_NAME)
                                .withGender(MALE)
                                .build())
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
                        .withCaseStatus("INACTIVE")
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
                        .withCode("TypeCode")
                        .withLinkType(LINKED)
                        .withType(APPLICATION_TYPE)
                        .withLegislation("APPLICATION_LEGISLATION")
                        .withJurisdiction(Jurisdiction.MAGISTRATES)
                        .withSummonsTemplateType(BREACH)
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
