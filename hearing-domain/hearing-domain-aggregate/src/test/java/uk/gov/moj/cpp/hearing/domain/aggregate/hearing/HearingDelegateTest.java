package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.*;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.*;
import uk.gov.moj.cpp.hearing.domain.event.*;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class HearingDelegateTest {
    private static final String GUILTY = "GUILTY";
    public static final String SKIP_EXTEND_HEARING_NOT_CREATED = "Skipping 'hearing.events.hearing-extended' event as hearing has not been created yet";

    private HearingAggregateMomento momento = new HearingAggregateMomento();
    private HearingDelegate hearingDelegate = new HearingDelegate(momento);

    @Test
    public void handleHearingInitiated_FirstTimeHearing() {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        assertThat(momento.getHearing(), is(notNullValue()));
        assertThat(momento.getPleas(),  is(notNullValue()));
        assertThat(momento.getVerdicts(), equalTo(Collections.EMPTY_MAP));
        assertThat(momento.getConvictionDates(), equalTo(Collections.EMPTY_MAP));
    }

    @Test
    public void handleHearingInitiated_AdjournedOrSubsequentHearing() {
        final LocalDate convictionDateForFirstOffence = PAST_LOCAL_DATE.next();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final Offence firstOffenceForFirstDefendantForFirstCase = hearing.getFirstOffenceForFirstDefendantForFirstCase();
        final UUID idOfFirstOffenceForFirstDefendantForFirstCase = firstOffenceForFirstDefendantForFirstCase.getId();
        firstOffenceForFirstDefendantForFirstCase.setPlea(Plea.plea().withPleaValue(GUILTY).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
        firstOffenceForFirstDefendantForFirstCase.setVerdict(Verdict.verdict().withVerdictType(VerdictType.verdictType().withCategoryType("GUILTY").build()).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
        firstOffenceForFirstDefendantForFirstCase.setConvictionDate(convictionDateForFirstOffence);
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        assertThat(momento.getHearing(), is(notNullValue()));
        assertThat(momento.getPleas(), hasKey(idOfFirstOffenceForFirstDefendantForFirstCase));
        assertThat(momento.getVerdicts(), hasKey(idOfFirstOffenceForFirstDefendantForFirstCase));
        assertThat(momento.getConvictionDates(), hasEntry(idOfFirstOffenceForFirstDefendantForFirstCase, convictionDateForFirstOffence));
    }

    @Test
    public void handleHearingInitiated_AdjournedOrSubsequentHearingWithCourtApplicationCases() {
        final LocalDate convictionDateForFirstOffence = PAST_LOCAL_DATE.next();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final Offence offence = hearing.getFirstOffenceForFirstFirstCaseForFirstApplication();
        final UUID offenceId = offence.getId();
        offence.setPlea(Plea.plea().withPleaValue(GUILTY).withOffenceId(offenceId).build());
        offence.setVerdict(Verdict.verdict().withVerdictType(VerdictType.verdictType().withCategoryType("GUILTY").build()).withOffenceId(offenceId).build());
        offence.setConvictionDate(convictionDateForFirstOffence);
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        assertThat(momento.getHearing(), is(notNullValue()));
        assertThat(momento.getPleas(), hasKey(offenceId));
        assertThat(momento.getVerdicts(), hasKey(offenceId));
        assertThat(momento.getConvictionDates(), hasEntry(offenceId, convictionDateForFirstOffence));
    }

    @Test
    public void handleHearingInitiated_AdjournedOrSubsequentHearingWithCourtApplicationCourtOrders() {
        final LocalDate convictionDateForFirstOffence = PAST_LOCAL_DATE.next();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final Offence offence = hearing.getFirstOffenceForFirstFirstCourtOrderForFirstApplication();
        final UUID offenceId = offence.getId();
        offence.setPlea(Plea.plea().withPleaValue(GUILTY).withOffenceId(offenceId).build());
        offence.setVerdict(Verdict.verdict().withVerdictType(VerdictType.verdictType().withCategoryType("GUILTY").build()).withOffenceId(offenceId).build());
        offence.setConvictionDate(convictionDateForFirstOffence);
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        assertThat(momento.getHearing(), is(notNullValue()));
        assertThat(momento.getPleas(), hasKey(offenceId));
        assertThat(momento.getVerdicts(), hasKey(offenceId));
        assertThat(momento.getConvictionDates(), hasEntry(offenceId, convictionDateForFirstOffence));
    }

    @Test
    public void shouldAddMasterDefendantIdToDefendant() {
        final UUID prosecutionCaseId1 = UUID.randomUUID();
        final UUID prosecutionCaseId2 = UUID.randomUUID();
        final UUID defendantId1 = UUID.randomUUID();
        final UUID defendantId2 = UUID.randomUUID();
        final UUID defendantId3 = UUID.randomUUID();
        final UUID defendantId4 = UUID.randomUUID();
        final UUID masterDefendantId = UUID.randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));
        momento.getHearing().setProsecutionCases(
                asList(
                        ProsecutionCase.prosecutionCase()
                                .withId(prosecutionCaseId1)
                                .withDefendants(
                                        asList(
                                                Defendant.defendant()
                                                        .withId(defendantId1)
                                                        .build(),
                                                Defendant.defendant()
                                                        .withId(defendantId2)
                                                        .build()
                                        )
                                )
                                .build(),
                        ProsecutionCase.prosecutionCase()
                                .withId(prosecutionCaseId2)
                                .withDefendants(
                                        asList(
                                                Defendant.defendant()
                                                        .withId(defendantId3)
                                                        .build(),
                                                Defendant.defendant()
                                                        .withId(defendantId4)
                                                        .build()
                                        )
                                )
                                .build()));
        hearingDelegate.handleMasterDefendantIdAdded(prosecutionCaseId1, defendantId1, masterDefendantId);
        assertThat(momento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId(), is(masterDefendantId));
        assertNull(momento.getHearing().getProsecutionCases().get(0).getDefendants().get(1).getMasterDefendantId());
        assertNull(momento.getHearing().getProsecutionCases().get(1).getDefendants().get(1).getMasterDefendantId());
        assertNull(momento.getHearing().getProsecutionCases().get(1).getDefendants().get(1).getMasterDefendantId());
    }

    @Test
    public void shouldHandleHearingExtendedWhenSameCaseHasDifferentDefendant() {
        final UUID hearingId = UUID.randomUUID();
        final UUID caseId = UUID.randomUUID();
        final UUID newDefendantID = UUID.randomUUID();
        final UUID currentDefendantID = UUID.randomUUID();
        final List<ProsecutionCase> extendedCases = caseList(createProsecutionCases(caseId, newDefendantID));
        final List<ProsecutionCase> currentCases = caseList(createProsecutionCases(caseId, currentDefendantID));

        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(currentCases)
                .build());

        final HearingExtended hearingExtended = new HearingExtended(hearingId, null, null, null, null, extendedCases, null);

        hearingDelegate.handleHearingExtended(hearingExtended);

        assertThat(momento.getHearing(), is(notNullValue()));
        final List<ProsecutionCase> prosecutionCases = momento.getHearing().getProsecutionCases();
        assertThat(prosecutionCases, is(notNullValue()));
        assertThat(prosecutionCases.size(), is(1));
        assertThat(prosecutionCases.get(0).getDefendants().size(), is(2));
        assertThat(prosecutionCases.stream()
                .anyMatch(pc -> pc.getDefendants().stream()
                        .anyMatch(d -> d.getId().equals(newDefendantID))), is(true));

        assertThat(prosecutionCases.stream()
                .anyMatch(pc -> pc.getDefendants().stream()
                        .anyMatch(d -> d.getId().equals(currentDefendantID))), is(true));
    }

    @Test
    public void shouldHandleHearingExtendedWhenHavingDifferentCase() {
        final UUID hearingId = UUID.randomUUID();
        final UUID newCaseId = UUID.randomUUID();
        final UUID currentCaseId = UUID.randomUUID();
        final UUID newDefendantID = UUID.randomUUID();
        final UUID currentDefendantID = UUID.randomUUID();
        final List<ProsecutionCase> extendedCases = caseList(createProsecutionCases(newCaseId, newDefendantID));
        final List<ProsecutionCase> currentCases = caseList(createProsecutionCases(currentCaseId, currentDefendantID));

        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(currentCases)
                .build());

        final HearingExtended hearingExtended = new HearingExtended(hearingId, null, null, null, null, extendedCases, null);

        hearingDelegate.handleHearingExtended(hearingExtended);

        assertThat(momento.getHearing(), is(notNullValue()));
        final List<ProsecutionCase> prosecutionCases = momento.getHearing().getProsecutionCases();
        assertThat(prosecutionCases, is(notNullValue()));
        assertThat(prosecutionCases.size(), is(2));
        final Optional<ProsecutionCase> currentCase = prosecutionCases.stream()
                .filter(pc -> pc.getId().equals(currentCaseId))
                .findFirst();

        assertThat(currentCase.isPresent(), is(true));
        assertThat(currentCase.get().getDefendants().size(), is(1));
        assertThat(currentCase.get().getDefendants().get(0).getId(), is(currentDefendantID));

        final Optional<ProsecutionCase> newCase = prosecutionCases.stream()
                .filter(pc -> pc.getId().equals(newCaseId))
                .findFirst();

        assertThat(newCase.isPresent(), is(true));
        assertThat(newCase.get().getDefendants().size(), is(1));
        assertThat(newCase.get().getDefendants().get(0).getId(), is(newDefendantID));
    }

    @Test
    public void shouldHandleHearingExtendedWhenSameDefendantDifferentOffence() {
        final UUID hearingId = UUID.randomUUID();
        final UUID caseId = UUID.randomUUID();
        final UUID defendantID = UUID.randomUUID();
        final UUID newOffenceID = UUID.randomUUID();
        final UUID currentOffenceID = UUID.randomUUID();
        final List<ProsecutionCase> extendedCases = caseList(createProsecutionCases(caseId, defendantID, newOffenceID));
        final List<ProsecutionCase> currentCases = caseList(createProsecutionCases(caseId, defendantID, currentOffenceID));

        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(currentCases)
                .build());

        final HearingExtended hearingExtended = new HearingExtended(hearingId, null, null, null, null, extendedCases, null);

        hearingDelegate.handleHearingExtended(hearingExtended);

        assertThat(momento.getHearing(), is(notNullValue()));
        final List<ProsecutionCase> prosecutionCases = momento.getHearing().getProsecutionCases();
        assertThat(prosecutionCases, is(notNullValue()));
        assertThat(prosecutionCases.size(), is(1));
        assertThat(prosecutionCases.get(0).getDefendants().size(), is(1));
        final Defendant defendant = prosecutionCases.get(0).getDefendants().get(0);
        assertThat(defendant.getOffences().size(), is(2));
        final Optional<Offence> currentOffence = defendant.getOffences().stream()
                .filter(o -> o.getId().equals(currentOffenceID))
                .findFirst();

        assertThat(currentOffence.isPresent(), is(true));

        final Optional<Offence> newOffence = defendant.getOffences().stream()
                .filter(o -> o.getId().equals(newOffenceID))
                .findFirst();

        assertThat(newOffence.isPresent(), is(true));

    }

    @Test
    public void shouldExtendHearingWithHearingBreachApplicationsAddedEvent() {
        final UUID hearingId = UUID.randomUUID();
        final UUID caseId = UUID.randomUUID();
        final UUID defendantID = UUID.randomUUID();
        final UUID newOffenceID = UUID.randomUUID();
        final List<ProsecutionCase> extendedCases = caseList(createProsecutionCases(caseId, defendantID, newOffenceID));
        final CourtApplication courtApplication = createCourtApplication(UUID.randomUUID());
        final UUID secondApplicationId = UUID.randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withCourtApplications(asList(courtApplication))
                .build());
        momento.setBreachApplicationsToBeAdded(asList(courtApplication.getId(), secondApplicationId));
        final List<Object> eventStream = hearingDelegate.extend(hearingId, Collections.singletonList(HearingDay.hearingDay().build()), CourtCentre.courtCentre().build(), JurisdictionType.MAGISTRATES, CourtApplication.courtApplication().withId(secondApplicationId).build(), extendedCases, null).collect(toList());
        assertThat(eventStream.size(), is(2));
        final HearingBreachApplicationsAdded hearingBreachApplicationsAdded = (HearingBreachApplicationsAdded) eventStream.get(0);
        assertThat(hearingBreachApplicationsAdded, notNullValue());

    }

    @Test
    public void shouldSkipExtendHearingIfHearingIsNotCreatedYet() {
        final UUID hearingId = UUID.randomUUID();
        final UUID caseId = UUID.randomUUID();
        final UUID defendantID = UUID.randomUUID();
        final UUID newOffenceID = UUID.randomUUID();
        final List<ProsecutionCase> extendedCases = caseList(createProsecutionCases(caseId, defendantID, newOffenceID));
        momento.setHearing(null);
        final List<Object> eventStream = hearingDelegate.extend(hearingId,Collections.singletonList(HearingDay.hearingDay().build()), CourtCentre.courtCentre().build(), JurisdictionType.MAGISTRATES, CourtApplication.courtApplication().build(), extendedCases,null).collect(toList());
        assertThat(eventStream.size(), is(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) eventStream.get(0);
        assertThat(hearingChangeIgnored.getHearingId(),is(hearingId));
        assertThat(hearingChangeIgnored.getReason(),is(SKIP_EXTEND_HEARING_NOT_CREATED));
    }

    @Test
    public void shouldRemoveProsecutionCaseAndDefendantWhenAllOffencesAreRemoved() {
        final UUID hearingId = UUID.randomUUID();
        final UUID prosecutionCaseId1 = UUID.randomUUID();
        final UUID prosecutionCaseId2 = UUID.randomUUID();
        final UUID prosecutionCaseId3 = UUID.randomUUID();
        final UUID defendantId1 = UUID.randomUUID();
        final UUID defendantId2 = UUID.randomUUID();
        final UUID defendantId3 = UUID.randomUUID();
        final UUID offence1 = UUID.randomUUID();
        final UUID offence2 = UUID.randomUUID();
        final UUID offence3 = UUID.randomUUID();
        final UUID offence4 = UUID.randomUUID();

        final ProsecutionCase prosecutionCase1 = createProsecutionCase(prosecutionCaseId1, defendantId1, offence1, offence2);
        final ProsecutionCase prosecutionCase2 = createProsecutionCase(prosecutionCaseId2, defendantId2, offence3, offence4);
        final ProsecutionCase prosecutionCase3 = createProsecutionCase(prosecutionCaseId3, defendantId3, offence3, offence4);

        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(new ArrayList<>(asList(prosecutionCase1, prosecutionCase2, prosecutionCase3)))
                .build());

        final List<UUID> offencesToBeRemoved = asList(offence1, offence2);
        final List<Object> eventStream = hearingDelegate.unAllocateHearing(hearingId, offencesToBeRemoved).collect(toList());

        assertThat(eventStream.size(), is(1));
        final HearingUnallocated hearingUnallocated = (HearingUnallocated) eventStream.get(0);
        assertThat(hearingUnallocated.getHearingId(), is(hearingId));
        assertThat(hearingUnallocated.getProsecutionCaseIds().size(), is(1));
        assertThat(hearingUnallocated.getDefendantIds().size(), is(1));
        assertThat(hearingUnallocated.getOffenceIds().size(), is(2));
        assertThat(momento.getHearing(), is(notNullValue()));
    }

    @Test
    public void shouldNotRemoveProsecutionCaseAndDefendantWhenSingleOffenceIsRemoved() {
        final UUID hearingId = UUID.randomUUID();
        final UUID prosecutionCaseId1 = UUID.randomUUID();
        final UUID prosecutionCaseId2 = UUID.randomUUID();
        final UUID prosecutionCaseId3 = UUID.randomUUID();
        final UUID defendantId1 = UUID.randomUUID();
        final UUID defendantId2 = UUID.randomUUID();
        final UUID defendantId3 = UUID.randomUUID();
        final UUID offence1 = UUID.randomUUID();
        final UUID offence2 = UUID.randomUUID();
        final UUID offence3 = UUID.randomUUID();
        final UUID offence4 = UUID.randomUUID();

        final ProsecutionCase prosecutionCase1 = createProsecutionCase(prosecutionCaseId1, defendantId1, offence1, offence2);
        final ProsecutionCase prosecutionCase2 = createProsecutionCase(prosecutionCaseId2, defendantId2, offence3, offence4);
        final ProsecutionCase prosecutionCase3 = createProsecutionCase(prosecutionCaseId3, defendantId3, offence3, offence4);

        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(new ArrayList<>(asList(prosecutionCase1, prosecutionCase2, prosecutionCase3)))
                .build());
        final List<UUID> offencesToBeRemoved = asList(offence1);
        final List<Object> eventStream = hearingDelegate.unAllocateHearing(hearingId, offencesToBeRemoved).collect(toList());

        assertThat(eventStream.size(), is(1));
        final HearingUnallocated hearingUnallocated = (HearingUnallocated) eventStream.get(0);
        assertThat(hearingUnallocated.getHearingId(), is(hearingId));
        assertThat(hearingUnallocated.getProsecutionCaseIds().size(), is(0));
        assertThat(hearingUnallocated.getDefendantIds().size(), is(0));
        assertThat(hearingUnallocated.getOffenceIds().size(), is(1));
        assertThat(momento.getHearing(), is(notNullValue()));

    }

    @Test
    public void shouldRaiseEarliestNextHearingDateChangedEventWhenThereAreNoNextHearingDatesForSeedingHearing() {

        final UUID hearingId = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(2);

        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId, seedingHearingId, nextHearingStartDate).collect(toList());

        assertThat(eventStream.size(), is(2));

        final NextHearingStartDateRecorded nextHearingStartDateRecorded = (NextHearingStartDateRecorded) eventStream.get(0);
        assertThat(nextHearingStartDateRecorded.getHearingId(), is(hearingId));
        assertThat(nextHearingStartDateRecorded.getSeedingHearingId(), is(seedingHearingId));
        assertThat(nextHearingStartDateRecorded.getNextHearingStartDate(), is(nextHearingStartDate));

        final EarliestNextHearingDateChanged earliestNextHearingDateChanged = (EarliestNextHearingDateChanged) eventStream.get(1);
        assertThat(earliestNextHearingDateChanged.getHearingId(), is(hearingId));
        assertThat(earliestNextHearingDateChanged.getSeedingHearingId(), is(seedingHearingId));
        assertThat(earliestNextHearingDateChanged.getEarliestNextHearingDate(), is(nextHearingStartDate));

    }

    @Test
    public void shouldRaiseEarliestNextHearingDateChangedEventWhenSameNextHearingDateIsEarlierThanPreviousOne() {

        final UUID hearingId = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(2);

        momento.getNextHearingStartDates().put(hearingId, ZonedDateTime.now().plusDays(3));
        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId, seedingHearingId, nextHearingStartDate).collect(toList());

        assertThat(eventStream.size(), is(2));

        final NextHearingStartDateRecorded nextHearingStartDateRecorded = (NextHearingStartDateRecorded) eventStream.get(0);
        assertThat(nextHearingStartDateRecorded.getHearingId(), is(hearingId));
        assertThat(nextHearingStartDateRecorded.getSeedingHearingId(), is(seedingHearingId));
        assertThat(nextHearingStartDateRecorded.getNextHearingStartDate(), is(nextHearingStartDate));

        final EarliestNextHearingDateChanged earliestNextHearingDateChanged = (EarliestNextHearingDateChanged) eventStream.get(1);
        assertThat(earliestNextHearingDateChanged.getHearingId(), is(hearingId));
        assertThat(earliestNextHearingDateChanged.getSeedingHearingId(), is(seedingHearingId));
        assertThat(earliestNextHearingDateChanged.getEarliestNextHearingDate(), is(nextHearingStartDate));

    }

    @Test
    public void shouldRaiseEarliestNextHearingDateChangedEventWhenSameNextHearingDateIsLaterThanPreviousOne() {

        final UUID hearingId = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(5);

        momento.getNextHearingStartDates().put(hearingId, ZonedDateTime.now().plusDays(3));
        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId, seedingHearingId, nextHearingStartDate).collect(toList());

        assertThat(eventStream.size(), is(2));

        final NextHearingStartDateRecorded nextHearingStartDateRecorded = (NextHearingStartDateRecorded) eventStream.get(0);
        assertThat(nextHearingStartDateRecorded.getHearingId(), is(hearingId));
        assertThat(nextHearingStartDateRecorded.getSeedingHearingId(), is(seedingHearingId));
        assertThat(nextHearingStartDateRecorded.getNextHearingStartDate(), is(nextHearingStartDate));

        final EarliestNextHearingDateChanged earliestNextHearingDateChanged = (EarliestNextHearingDateChanged) eventStream.get(1);
        assertThat(earliestNextHearingDateChanged.getHearingId(), is(hearingId));
        assertThat(earliestNextHearingDateChanged.getSeedingHearingId(), is(seedingHearingId));
        assertThat(earliestNextHearingDateChanged.getEarliestNextHearingDate(), is(nextHearingStartDate));
    }

    @Test
    public void shouldRaiseEarliestNextHearingDateChangedEventWhenNextHearingDateIsEarlierThanAllThePreviousOnes() {

        final UUID hearingId1 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(2);

        momento.getNextHearingStartDates().put(hearingId1, ZonedDateTime.now().plusDays(3));
        momento.getNextHearingStartDates().put(hearingId2, ZonedDateTime.now().plusDays(4));
        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId1, seedingHearingId, nextHearingStartDate).collect(toList());

        assertThat(eventStream.size(), is(2));

        final NextHearingStartDateRecorded nextHearingStartDateRecorded = (NextHearingStartDateRecorded) eventStream.get(0);
        assertThat(nextHearingStartDateRecorded.getHearingId(), is(hearingId1));
        assertThat(nextHearingStartDateRecorded.getSeedingHearingId(), is(seedingHearingId));
        assertThat(nextHearingStartDateRecorded.getNextHearingStartDate(), is(nextHearingStartDate));

        final EarliestNextHearingDateChanged earliestNextHearingDateChanged = (EarliestNextHearingDateChanged) eventStream.get(1);
        assertThat(earliestNextHearingDateChanged.getHearingId(), is(hearingId1));
        assertThat(earliestNextHearingDateChanged.getSeedingHearingId(), is(seedingHearingId));
        assertThat(earliestNextHearingDateChanged.getEarliestNextHearingDate(), is(nextHearingStartDate));

    }

    @Test
    public void shouldNotRaiseEarliestNextHearingDateChangedEventWhenNextHearingDateIsLaterThanOneOfThePreviousOnes() {

        final UUID hearingId1 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(5);

        momento.getNextHearingStartDates().put(hearingId1, ZonedDateTime.now().plusDays(2));
        momento.getNextHearingStartDates().put(hearingId2, ZonedDateTime.now().plusDays(4));
        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId1, seedingHearingId, nextHearingStartDate).collect(toList());

        assertThat(eventStream.size(), is(1));

        final NextHearingStartDateRecorded nextHearingStartDateRecorded = (NextHearingStartDateRecorded) eventStream.get(0);
        assertThat(nextHearingStartDateRecorded.getHearingId(), is(hearingId1));
        assertThat(nextHearingStartDateRecorded.getSeedingHearingId(), is(seedingHearingId));
        assertThat(nextHearingStartDateRecorded.getNextHearingStartDate(), is(nextHearingStartDate));

    }

    @Test
    public void shouldNotRaiseEarliestNextHearingDateChangedEventWhenNextHearingDateIsLaterThanPreviousOnes() {

        final UUID hearingId1 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(5);

        momento.getNextHearingStartDates().put(hearingId2, ZonedDateTime.now().plusDays(3));
        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId1, seedingHearingId, nextHearingStartDate).collect(toList());


        assertThat(eventStream.size(), is(1));

        final NextHearingStartDateRecorded nextHearingStartDateRecorded = (NextHearingStartDateRecorded) eventStream.get(0);
        assertThat(nextHearingStartDateRecorded.getHearingId(), is(hearingId1));
        assertThat(nextHearingStartDateRecorded.getSeedingHearingId(), is(seedingHearingId));
        assertThat(nextHearingStartDateRecorded.getNextHearingStartDate(), is(nextHearingStartDate));


    }
    @Test
    public void shouldUserAddedToJudiciary(){

        final UUID judiciaryId = randomUUID();
        final String emailId = "abc@cde.com";
        final UUID cpUserId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID id = randomUUID();

        final Stream<Object> objectStream = hearingDelegate.userAddedToJudiciary(
                judiciaryId,
                emailId,
                cpUserId,
                hearingId,
                id);
        assertThat(((HearingUserAddedToJudiciary)objectStream.collect(toList()).get(0)).getHearingId(), is(hearingId));
    }

    @Test
    public void shouldNotRaiseEventIfHearingIsAlreadyMarkedAsDuplicate(){

        final UUID hearingId = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(5);

        momento.setDuplicate(true);

        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId, seedingHearingId, nextHearingStartDate).collect(toList());

        assertThat(eventStream.size(), is(0));

    }

    @Test
    public void shouldNotRaiseEventIfHearingIsAlreadyMarkedAsDeleted(){

        final UUID hearingId = UUID.randomUUID();
        final UUID seedingHearingId = UUID.randomUUID();
        final ZonedDateTime nextHearingStartDate = ZonedDateTime.now().plusDays(5);

        momento.setDeleted(true);

        final List<Object> eventStream = hearingDelegate.changeNextHearingStartDate(hearingId, seedingHearingId, nextHearingStartDate).collect(toList());

        assertThat(eventStream.size(), is(0));

    }

    @Test
    public void shouldHandleHearingMarkedAsDuplicate() {
        final UUID hearingId = UUID.randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        final List<Object> eventStream = hearingDelegate.markAsDuplicate(hearingId, "Some reason").collect(toList());

        assertThat(eventStream.size(), is(1));

        final HearingMarkedAsDuplicate hearingMarkedAsDuplicate = (HearingMarkedAsDuplicate) eventStream.get(0);
        assertThat(hearingMarkedAsDuplicate.getHearingId(), is(hearingId));
        assertThat(hearingMarkedAsDuplicate.getCourtCentreId(), is(hearing.getCourtCentre().getId()));
        assertThat(hearingMarkedAsDuplicate.getDefendantIds().size(), is(1));
        assertThat(hearingMarkedAsDuplicate.getProsecutionCaseIds().size(), is(1));
        assertThat(hearingMarkedAsDuplicate.getOffenceIds().size(), is(1));
    }
    @Test
    void updateCourtApplication_shouldReturnApplicationDetailChangedEvent() {
        UUID hearingId = UUID.randomUUID();
        final uk.gov.justice.core.courts.CourtApplication courtApplication = uk.gov.justice.core.courts.CourtApplication.courtApplication()
                .withId(randomUUID())
                .build();
        momento.setHearing(Hearing.hearing().withId(hearingId).build());

        final Stream<Object> result = hearingDelegate.updateCourtApplication(hearingId, courtApplication);

        List<Object> events = result.collect(Collectors.toList());
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ApplicationDetailChanged);
        ApplicationDetailChanged event = (ApplicationDetailChanged) events.get(0);
        assertEquals(hearingId, event.getHearingId());
        assertEquals(courtApplication, event.getCourtApplication());
    }

    @Test
    void updateCourtApplication_shouldReturnHearingIgnoredMessageWhenHearingNotFound() {
        UUID hearingId = UUID.randomUUID();
        momento.setHearing(null);
        Stream<Object> result = hearingDelegate.updateCourtApplication(hearingId, uk.gov.justice.core.courts.CourtApplication.courtApplication()
                .withId(randomUUID())
                .build());

        List<Object> events = result.collect(Collectors.toList());
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof HearingChangeIgnored);
        HearingChangeIgnored event = (HearingChangeIgnored) events.get(0);
        assertEquals(hearingId, event.getHearingId());
        assertEquals("Rejecting 'hearing.update-court-application' event as hearing not found", event.getReason());
    }

    @Test
    void updateCourtApplication_shouldReturnEmptyStreamWhenHearingHasSharedResults() {
        UUID hearingId = UUID.randomUUID();
        CourtApplication courtApplication = uk.gov.justice.core.courts.CourtApplication.courtApplication()
                .withId(randomUUID())
                .build();

        momento.setHearing(Hearing.hearing().withId(hearingId).build());
        momento.getHearing().setHasSharedResults(true);
        Stream<Object> result = hearingDelegate.updateCourtApplication(hearingId, courtApplication);

        List<Object> events = result.collect(Collectors.toList());
        assertTrue(events.isEmpty());
    }

    private List<ProsecutionCase> caseList(ProsecutionCase... cases) {
        return new ArrayList(Arrays.asList(cases));
    }

    private List<CourtApplication> applicationList(CourtApplication... applications) {
        return new ArrayList(Arrays.asList(applications));
    }

    private ProsecutionCase createProsecutionCases(final UUID caseId, final UUID newDefendantID) {
        final List<Defendant> defendants = new ArrayList<>();
        defendants.add(Defendant.defendant()
                .withId(newDefendantID)
                .withOffences(new ArrayList<>())
                .build());

        return ProsecutionCase.prosecutionCase()
                .withId(caseId)
                .withDefendants(defendants)
                .build();
    }


    private CourtApplication createCourtApplication(final UUID id) {
        return  new CourtApplication.Builder().withId(id).build();
    }
    private ProsecutionCase createProsecutionCases(final UUID caseId, final UUID newDefendantID, final UUID offenceId) {
        final List<Offence> offences = new ArrayList<>();
        offences.add(Offence.offence()
                .withId(offenceId)
                .build());

        final List<Defendant> defendants = new ArrayList<>();
        defendants.add(Defendant.defendant()
                .withId(newDefendantID)
                .withOffences(offences)
                .build());

        return ProsecutionCase.prosecutionCase()
                .withId(caseId)
                .withDefendants(defendants)
                .build();
    }

    private ProsecutionCase createProsecutionCase(final UUID caseId, final UUID newDefendantID, final UUID offenceId1, final UUID offenceId2) {
        final List<Offence> offences = new ArrayList<>();
        offences.add(Offence.offence()
                .withId(offenceId1)
                .build());

        offences.add(Offence.offence()
                .withId(offenceId2)
                .build());

        final List<Defendant> defendants = new ArrayList<>();
        defendants.add(Defendant.defendant()
                .withId(newDefendantID)
                .withOffences(offences)
                .build());

        return ProsecutionCase.prosecutionCase()
                .withId(caseId)
                .withDefendants(defendants)
                .build();
    }




}