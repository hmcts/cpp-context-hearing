package uk.gov.moj.cpp.hearing.it;

import static java.time.LocalDate.now;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.addDefenceCounsel;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.RECORDED_LABEL_END_HEARING;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsADefenceCounsel;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddDefenceCounselCommandTemplates.addDefenceCounselCommandTemplate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
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
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingsCheckIn;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingsCheckInPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsMagistrateUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantCase;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import io.restassured.path.json.JsonPath;
import java.time.ZoneId;
import uk.gov.justice.core.courts.BreachType;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.Defendant;
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
class HearingCheckInIT extends AbstractIT {

    private static final String DEFENDANT_FIRST_NAME = "FIRST_NAME";
    private static final String DEFENDANT_LAST_NAME = "LAST_NAME";
    private static final String APPLICATION_TYPE = "APPLICATION_TYPE";
    private static final String HEARING_TYPE_DESCRIPTION = "Trial";
    private static final String INACTIVE_CASE_URN = "INACTIVECASEURN1";
    private static final String APPLICATION_REFERENCE = "APPREF0001";

    @Test
    void shouldRetrieveHearingWithProsecutionCaseForCheckIn() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final LocalDate hearingDate = now();

        initiateHearing(getRequestSpec(), createHearingWithCase(hearingId, courtCentreId, roomId, null));

        getHearingsCheckInPollForMatch(courtCentreId, hearingDate.toString(), isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasSize(greaterThanOrEqualTo(1)))
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(hs -> hs.getType().getDescription(), is(HEARING_TYPE_DESCRIPTION))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getFirstName(), is(DEFENDANT_FIRST_NAME))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getLastName(), is(DEFENDANT_LAST_NAME))
                ))
        );
    }

    @Test
    void shouldRetrieveApplicationHearingWithNoProsecutionCaseForCheckIn() {
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

    @Test
    void shouldIncludeInactiveApplicationCaseHeldUnderAnotherHearing() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID inactiveCaseId = randomUUID();
        final UUID offenceId = randomUUID();
        // the case only exists in ha_case under an earlier hearing, in a different court centre
        initiateHearing(getRequestSpec(), createHearingWithCase(randomUUID(), randomUUID(), randomUUID(), null,
                inactiveCaseId, DEFENDANT_FIRST_NAME, ZonedDateTime.now(), offenceId));

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final LocalDate hearingDate = now();
        initiateHearing(getRequestSpec(), createHearingWithApplicationOnly(hearingId, courtCentreId, randomUUID(), inactiveCaseId, offenceId));

        getHearingsCheckInPollForMatch(courtCentreId, hearingDate.toString(), isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(1))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getId(), is(inactiveCaseId))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getFirstName(), is(DEFENDANT_FIRST_NAME))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getLastName(), is(DEFENDANT_LAST_NAME))
                        .with(HearingSummaries::getCourtApplicationSummaries, hasSize(1))
                ))
        );
    }

    @Test
    void shouldUseLatestHearingSnapshotOfInactiveApplicationCase() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID inactiveCaseId = randomUUID();
        final UUID offenceId = randomUUID();
        final ZonedDateTime now = ZonedDateTime.now();
        initiateHearing(getRequestSpec(), createHearingWithCase(randomUUID(), randomUUID(), randomUUID(), null,
                inactiveCaseId, "OLDER_SNAPSHOT", now.minusDays(30), offenceId));
        initiateHearing(getRequestSpec(), createHearingWithCase(randomUUID(), randomUUID(), randomUUID(), null,
                inactiveCaseId, "LATEST_SNAPSHOT", now.plusDays(30), offenceId));
        initiateHearing(getRequestSpec(), createHearingWithCase(randomUUID(), randomUUID(), randomUUID(), null,
                inactiveCaseId, "MIDDLE_SNAPSHOT", now.minusDays(5), offenceId));

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        initiateHearing(getRequestSpec(), createHearingWithApplicationOnly(hearingId, courtCentreId, randomUUID(), inactiveCaseId, offenceId));

        getHearingsCheckInPollForMatch(courtCentreId, now().toString(), isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(1))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getFirstName(), is("LATEST_SNAPSHOT"))
                ))
        );
    }

    @Test
    void shouldKeepOnlyDefendantsWhoseOffenceIsListedOnTheApplicationCase() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID inactiveCaseId = randomUUID();
        final UUID matchingOffenceId = randomUUID();
        initiateHearing(getRequestSpec(), createHearingWithCase(randomUUID(), randomUUID(), randomUUID(), null,
                inactiveCaseId, ZonedDateTime.now(),
                defendantWithOffence("MATCHING_DEFENDANT", matchingOffenceId, inactiveCaseId),
                defendantWithOffence("OTHER_DEFENDANT", randomUUID(), inactiveCaseId)));

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        initiateHearing(getRequestSpec(), createHearingWithApplicationOnly(hearingId, courtCentreId, randomUUID(), inactiveCaseId, matchingOffenceId));

        getHearingsCheckInPollForMatch(courtCentreId, now().toString(), isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(1))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getId(), is(inactiveCaseId))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants(), hasSize(1))
                        .with(hs -> hs.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getFirstName(), is("MATCHING_DEFENDANT"))
                ))
        );

        final HearingSummaries summary = findSummary(courtCentreId, hearingId);
        assertThat(summary.getProsecutionCaseSummaries(), hasSize(1));
        assertThat(summary.getProsecutionCaseSummaries().get(0).getId(), is(inactiveCaseId));
        assertThat(summary.getProsecutionCaseSummaries().get(0).getDefendants(), hasSize(1));
        assertThat(summary.getProsecutionCaseSummaries().get(0).getDefendants().get(0).getFirstName(), is("MATCHING_DEFENDANT"));
    }

    @Test
    void shouldDropInactiveApplicationCaseWhenNoDefendantOffenceMatches() {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final UUID inactiveCaseId = randomUUID();
        initiateHearing(getRequestSpec(), createHearingWithCase(randomUUID(), randomUUID(), randomUUID(), null,
                inactiveCaseId, DEFENDANT_FIRST_NAME, ZonedDateTime.now(), randomUUID()));

        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        // the application case lists an offence that no defendant of the case has
        initiateHearing(getRequestSpec(), createHearingWithApplicationOnly(hearingId, courtCentreId, randomUUID(), inactiveCaseId, randomUUID()));

        getHearingsCheckInPollForMatch(courtCentreId, now().toString(), isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getProsecutionCaseSummaries, is(empty()))
                        .with(HearingSummaries::getCourtApplicationSummaries, hasSize(1))
                ))
        );

        final HearingSummaries summary = findSummary(courtCentreId, hearingId);
        assertThat(summary.getProsecutionCaseSummaries(), is(empty()));
        assertThat(summary.getCourtApplicationSummaries(), hasSize(1));
    }

    @Test
    void shouldCheckInDefenceCounselToApplicationHearingOnInactiveCase() {
        final UUID userId = randomUUID();
        givenAUserHasLoggedInAsADefenceCounsel(userId);
        stubUsersAndGroupsUserRoles(userId);

        final UUID hearingId = randomUUID();
        final UUID inactiveCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        initiateHearing(getRequestSpec(), createHearingWithApplicationOnlyForDefendant(hearingId, randomUUID(), randomUUID(),
                inactiveCaseId, defendantId, INACTIVE_CASE_URN));

        final DefenceCounsel defenceCounsel = defenceCounselFor(defendantId);
        try (EventListener defenceCounselAdded = listenFor("public.hearing.defence-counsel-added")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))) {

            addDefenceCounsel(getRequestSpec(), hearingId, addDefenceCounselCommandTemplate(hearingId, defenceCounsel));

            defenceCounselAdded.waitFor();
        }

        pollForHearing(hearingId.toString(),
                withJsonPath("$.hearing.defenceCounsels.[0].id", is(defenceCounsel.getId().toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(defendantId.toString())));
    }

    @Test
    void shouldIgnoreDefenceCounselCheckInAfterHearingEndedForApplicationHearingOnInactiveCase() {
        final UUID userId = randomUUID();
        givenAUserHasLoggedInAsADefenceCounsel(userId);
        stubUsersAndGroupsUserRoles(userId);

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = initiateHearing(getRequestSpec(),
                createHearingWithApplicationOnlyForDefendant(hearingId, randomUUID(), randomUUID(), randomUUID(), defendantId, INACTIVE_CASE_URN));

        final HearingEventDefinition endHearing = findEventDefinitionWithActionLabel(RECORDED_LABEL_END_HEARING);
        logEvent(randomUUID(), getRequestSpec(), asDefault(), initiateHearingCommand, endHearing.getId(), false, randomUUID(),
                PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")), RECORDED_LABEL_END_HEARING, null);

        final JsonPath changeIgnored;
        try (EventListener defenceCounselChangeIgnored = listenFor("public.hearing.defence-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))) {

            addDefenceCounsel(getRequestSpec(), hearingId, addDefenceCounselCommandTemplate(hearingId, defenceCounselFor(defendantId)));

            changeIgnored = defenceCounselChangeIgnored.waitFor();
        }

        // an application-only hearing is referenced by its application reference, as in the hearing event log
        assertThat(changeIgnored.getString("caseURN"), is(APPLICATION_REFERENCE));
    }

    private static DefenceCounsel defenceCounselFor(final UUID defendantId) {
        return new DefenceCounsel(singletonList(now()), singletonList(defendantId), STRING.next(), randomUUID(),
                STRING.next(), STRING.next(), STRING.next(), STRING.next(), randomUUID());
    }

    /**
     * An application hearing on an inactive case, as Progression sends it: no prosecution cases, and the
     * case's defendant linked through the application subject's master defendant.
     */
    private InitiateHearingCommand createHearingWithApplicationOnlyForDefendant(final UUID hearingId, final UUID courtCentreId, final UUID roomId,
                                                                               final UUID inactiveCaseId, final UUID defendantId, final String caseUrn) {
        final CourtApplication application = createCourtApplication(inactiveCaseId, randomUUID());
        final CourtApplication applicationForDefendant = CourtApplication.courtApplication()
                .withValuesFrom(application)
                .withApplicationReference(APPLICATION_REFERENCE)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withValuesFrom(application.getSubject())
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(randomUUID())
                                .withDefendantCase(singletonList(DefendantCase.defendantCase()
                                        .withCaseId(inactiveCaseId)
                                        .withDefendantId(defendantId)
                                        .withCaseReference(caseUrn)
                                        .build()))
                                // the schema requires a person or legal-entity defendant on a master defendant
                                .withPersonDefendant(personDefendant()
                                        .withPersonDetails(person()
                                                .withFirstName(DEFENDANT_FIRST_NAME)
                                                .withLastName(DEFENDANT_LAST_NAME)
                                                .withGender(MALE)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withValuesFrom(application.getCourtApplicationCases().get(0))
                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                .withProsecutionAuthorityId(randomUUID())
                                .withProsecutionAuthorityCode(STRING.next())
                                .withCaseURN(caseUrn)
                                .build())
                        .build()))
                .build();
        final InitiateHearingCommand command = createHearingWithCase(hearingId, courtCentreId, roomId, singletonList(applicationForDefendant));
        command.getHearing().setProsecutionCases(null);
        return command;
    }

    private HearingSummaries findSummary(final UUID courtCentreId, final UUID hearingId) {
        return getHearingsCheckIn(courtCentreId, now().toString()).getHearingSummaries().stream()
                .filter(hs -> hearingId.equals(hs.getId()))
                .findFirst()
                .orElseThrow();
    }

    private InitiateHearingCommand createHearingWithCase(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final List<CourtApplication> courtApplications) {
        return createHearingWithCase(hearingId, courtCentreId, roomId, courtApplications, randomUUID(), DEFENDANT_FIRST_NAME, ZonedDateTime.now());
    }

    private InitiateHearingCommand createHearingWithCase(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final List<CourtApplication> courtApplications,
                                                         final UUID prosecutionCaseId, final String defendantFirstName, final ZonedDateTime sittingDay) {
        return createHearingWithCase(hearingId, courtCentreId, roomId, courtApplications, prosecutionCaseId, defendantFirstName, sittingDay, randomUUID());
    }

    private InitiateHearingCommand createHearingWithCase(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final List<CourtApplication> courtApplications,
                                                         final UUID prosecutionCaseId, final String defendantFirstName, final ZonedDateTime sittingDay,
                                                         final UUID offenceId) {
        return createHearingWithCase(hearingId, courtCentreId, roomId, courtApplications, prosecutionCaseId, sittingDay,
                defendantWithOffence(defendantFirstName, offenceId, prosecutionCaseId));
    }

    private InitiateHearingCommand createHearingWithCase(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final List<CourtApplication> courtApplications,
                                                         final UUID prosecutionCaseId, final ZonedDateTime sittingDay,
                                                         final Defendant... defendants) {
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
                                .withSittingDay(sittingDay)
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
                                .withDefendants(asList(defendants))
                                .build()))
                        .withCourtApplications(courtApplications)
                        .withJurisdictionType(MAGISTRATES)
                        .withType(hearingType()
                                .withId(randomUUID())
                                .withDescription(HEARING_TYPE_DESCRIPTION)
                                .build())
                        .build());
    }

    private Defendant defendantWithOffence(final String firstName, final UUID offenceId, final UUID prosecutionCaseId) {
        return defendant()
                .withId(randomUUID())
                .withCourtProceedingsInitiated(ZonedDateTime.now())
                .withMasterDefendantId(randomUUID())
                .withProsecutionCaseId(prosecutionCaseId)
                .withPersonDefendant(personDefendant()
                        .withPersonDetails(person()
                                .withFirstName(firstName)
                                .withLastName(DEFENDANT_LAST_NAME)
                                .withGender(MALE)
                                .build())
                        .build())
                .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId)
                        .withOffenceDefinitionId(randomUUID())
                        .withOffenceCode("code")
                        .withStartDate(now())
                        .withOffenceTitle("OFFENCE_TITLE")
                        .withWording("OFFENCE_WORDING")
                        .build()))
                .build();
    }

    private InitiateHearingCommand createHearingWithApplicationOnly(final UUID hearingId, final UUID courtCentreId, final UUID roomId,
                                                                     final LocalDate hearingDate) {
        return createHearingWithApplicationOnly(hearingId, courtCentreId, roomId, randomUUID(), randomUUID());
    }

    private InitiateHearingCommand createHearingWithApplicationOnly(final UUID hearingId, final UUID courtCentreId, final UUID roomId,
                                                                     final UUID inactiveCaseId, final UUID offenceId) {
        final InitiateHearingCommand command = createHearingWithCase(hearingId, courtCentreId, roomId, singletonList(createCourtApplication(inactiveCaseId, offenceId)));
        command.getHearing().setProsecutionCases(null);
        return command;
    }

    private CourtApplication createCourtApplication(final UUID inactiveCaseId, final UUID offenceId) {
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
                        .withProsecutionCaseId(inactiveCaseId)
                        .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(offenceId)
                                .withOffenceDefinitionId(randomUUID())
                                .withOffenceCode("code")
                                .withStartDate(now())
                                .withOffenceTitle("OFFENCE_TITLE")
                                .withWording("OFFENCE_WORDING")
                                .build()))
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
