package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.HearingResultsCleanerUtil.removeResultsFromHearing;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Target2;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAdded;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingBreachApplicationsAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysCancelled;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiateIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicate;
import uk.gov.moj.cpp.hearing.domain.event.HearingUnallocated;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacatedTrialDetailUpdated;
import uk.gov.moj.cpp.hearing.domain.event.MasterDefendantIdAdded;
import uk.gov.moj.cpp.hearing.domain.event.NextHearingStartDateRecorded;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S00107", "squid:S3655", "squid:S1871","PMD:BeanMembersShouldSerialize"})
public class HearingDelegate implements Serializable {

    private static final long serialVersionUID = 6948738797633524093L;

    private final HearingAggregateMomento momento;

    public HearingDelegate(HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleHearingInitiated(HearingInitiated hearingInitiated) {
        final Hearing hearing = hearingInitiated.getHearing();
        this.momento.setHearing(hearing);

        if (isNull(hearing)) {
            return;
        }
        if (nonNull(hearing.getProsecutionCases())) {
            this.momento.getHearing().getProsecutionCases().forEach(
                    prosecutionCase -> prosecutionCase.getDefendants().forEach(
                            defendant -> defendant.getOffences().forEach(this::keepOffence)));
        }
        if (nonNull(hearing.getCourtApplications())) {
            this.momento.getHearing().getCourtApplications().stream()
                    .flatMap(a -> ofNullable(a.getCourtApplicationCases()).orElse(emptyList()).stream())
                    .flatMap(c -> ofNullable(c.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                    .forEach(this::keepOffence);

            this.momento.getHearing().getCourtApplications().stream()
                    .map(CourtApplication::getCourtOrder)
                    .filter(Objects::nonNull)
                    .flatMap(c -> c.getCourtOrderOffences().stream())
                    .map(CourtOrderOffence::getOffence)
                    .forEach(this::keepOffence);
        }
    }

    private void keepOffence(final Offence offence) {
        final UUID offenceId = offence.getId();
        if (nonNull(offence.getConvictionDate())) {
            this.momento.getConvictionDates().put(offenceId, offence.getConvictionDate());
        }

        if (nonNull(offence.getPlea())) {
            this.momento.getPleas().put(offenceId, offence.getPlea());
        }

        if (nonNull(offence.getVerdict())) {
            this.momento.getVerdicts().put(offenceId, offence.getVerdict());
        }
    }

    public void handleHearingExtended(final HearingExtended hearingExtended) {
        updateCourtApplication(hearingExtended);

        updateCourtCentre(hearingExtended);

        updateHearingDays(hearingExtended);

        if (nonNull(this.momento.getHearing()) && nonNull(hearingExtended.getJurisdictionType())) {
            this.momento.getHearing().setJurisdictionType(hearingExtended.getJurisdictionType());
        }

        if (nonNull(this.momento.getHearing()) && isNotEmpty(hearingExtended.getProsecutionCases())) {
            hearingExtended.getProsecutionCases().forEach(
                    extendedPC -> {
                        final ProsecutionCase currentPC = this.momento.getHearing().getProsecutionCases().stream()
                                .filter(prosecutionCase -> prosecutionCase.getId().equals(extendedPC.getId()))
                                .findFirst().orElse(null);

                        if (nonNull(currentPC)) {
                            final ProsecutionCase mergedPC = createProsecutionCase(extendedPC, currentPC);
                            this.momento.getHearing().getProsecutionCases().removeIf(caseToBeRemoved -> caseToBeRemoved.getId().equals(mergedPC.getId()));
                            this.momento.getHearing().getProsecutionCases().add(mergedPC);
                        } else {
                            this.momento.getHearing().getProsecutionCases().add(extendedPC);
                        }
                    }
            );
        }

    }

    private void updateHearingDays(HearingExtended hearingExtended) {
        if (nonNull(this.momento.getHearing()) && nonNull(hearingExtended.getHearingDays())) {
            this.momento.getHearing().setHearingDays(hearingExtended.getHearingDays());
        }
    }

    private void updateCourtCentre(HearingExtended hearingExtended) {
        if (nonNull(this.momento.getHearing()) && nonNull(hearingExtended.getCourtCentre())) {
            this.momento.getHearing().setCourtCentre(hearingExtended.getCourtCentre());
        }
    }

    private void updateCourtApplication(HearingExtended hearingExtended) {
        if (nonNull(this.momento.getHearing()) && nonNull(hearingExtended.getCourtApplication())) {
            final List<CourtApplication> oldCourtApplications = this.momento.getHearing().getCourtApplications();
            final List<CourtApplication> newCourtApplications = oldCourtApplications == null ? new ArrayList<>() :
                    oldCourtApplications.stream()
                            .filter(ca -> !ca.getId().equals(hearingExtended.getCourtApplication().getId()))
                            .collect(Collectors.toList());

            newCourtApplications.add(hearingExtended.getCourtApplication());
            //Adding hearingExtended as we need to reCreate Data again with court details for multiple breach applications
            this.momento.getHearing().setCourtApplications(newCourtApplications);
        }
    }

    public void handleHearingDetailChanged(HearingDetailChanged hearingDetailChanged) {

        if (hearingDetailChanged.getJudiciary() != null && !hearingDetailChanged.getJudiciary().isEmpty()) {
            this.momento.getHearing().setJudiciary(new ArrayList<>(hearingDetailChanged.getJudiciary()));
        }
        if (hearingDetailChanged.getHearingDays() != null && !hearingDetailChanged.getHearingDays().isEmpty()) {
            this.momento.getHearing().setHearingDays(new ArrayList<>(hearingDetailChanged.getHearingDays()));
        }
        this.momento.getHearing().setCourtCentre(hearingDetailChanged.getCourtCentre());
        this.momento.getHearing().setHearingLanguage(hearingDetailChanged.getHearingLanguage());
        this.momento.getHearing().setJurisdictionType(hearingDetailChanged.getJurisdictionType());
        this.momento.getHearing().setReportingRestrictionReason(hearingDetailChanged.getReportingRestrictionReason());
        this.momento.getHearing().setType(hearingDetailChanged.getType());

    }

    public void handleVacatedTrialCleared() {
        if (this.momento.getHearing().getIsVacatedTrial() != null && this.momento.getHearing().getIsVacatedTrial()) {
            this.momento.getHearing().setIsVacatedTrial(false);
            this.momento.getHearing().setCrackedIneffectiveTrial(null);
        }
    }

    public void handleTargetRemoved(final UUID targetId) {
        this.momento.getMultiDayTargets().get(getHearingDay()).remove(targetId);
    }

    public void handleMasterDefendantIdAdded(final UUID prosecutionCaseId, final UUID defendantId, final UUID masterDefendantId) {
        this.momento.getHearing().getProsecutionCases().stream()
                .filter(prosecutionCase -> prosecutionCase.getId().equals(prosecutionCaseId))
                .flatMap(p -> p.getDefendants().stream())
                .filter(d -> d.getId().equals(defendantId))
                .forEach(d -> d.setMasterDefendantId(masterDefendantId));
    }

    public Stream<Object> initiate(final Hearing hearing) {

        final Hearing hearingWithoutResults = removeResultsFromHearing(hearing);

        return Stream.of(new HearingInitiated(hearingWithoutResults));
    }

    public Stream<Object> extend(final UUID hearingId,
                                 final List<HearingDay> hearingDays, final CourtCentre courtCentre, final JurisdictionType jurisdictionType,
                                 final CourtApplication courtApplication, final List<ProsecutionCase> prosecutionCases,
                                 final List<UUID> shadowListedOffences) {
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        if (this.momento.getHearing() == null) {
            return streamBuilder.add(generateHearingIgnoredMessage("Skipping 'hearing.events.hearing-extended' event as hearing has not been created yet",hearingId)).build();
        }
        if(courtApplication != null &&  this.momento.getBreachApplicationsToBeAdded() != null && !this.momento.getBreachApplicationsToBeAdded().isEmpty()) {
            final List<UUID> courtApplicationsInHearing =  this.momento.getHearing().getCourtApplications() != null ?
                    this.momento.getHearing().getCourtApplications().stream().map(a -> a.getId()).collect(toList()) : new ArrayList<>();
            courtApplicationsInHearing.add(courtApplication.getId());
            final boolean allBreachApplicationsAdded = this.momento.getBreachApplicationsToBeAdded().stream().allMatch(a -> courtApplicationsInHearing.contains(a));
            if(allBreachApplicationsAdded) {
                final List<CourtApplication> courtApplicationListAlreadyInHearing =  this.momento.getHearing().getCourtApplications().stream().filter(ap -> this.momento.getBreachApplicationsToBeAdded().contains(ap.getId())).collect(toList());
                courtApplicationListAlreadyInHearing.add(courtApplication);
                streamBuilder.add(new HearingBreachApplicationsAdded(courtApplicationListAlreadyInHearing));
            }
        }
        return streamBuilder.add(new HearingExtended(hearingId, hearingDays, courtCentre, jurisdictionType, courtApplication, prosecutionCases, shadowListedOffences)).build();
    }

    public Stream<Object> updateHearingDetails(final UUID id,
                                               final HearingType type,
                                               final CourtCentre courtCentre,
                                               final JurisdictionType jurisdictionType,
                                               final String reportingRestrictionReason,
                                               final HearingLanguage hearingLanguage,
                                               final List<HearingDay> hearingDays,
                                               final List<JudicialRole> judiciary
    ) {

        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.change-hearing-detail' event as hearing not found", id));
        }

        return Stream.of(new HearingDetailChanged(id, type, courtCentre, jurisdictionType, reportingRestrictionReason, hearingLanguage, hearingDays, judiciary));
    }

    public Stream<Object> updateHearingVacateTrialDetails(final UUID hearingId,
                                                          final Boolean isVacated,
                                                          final UUID vacatedTrialReasonId) {
        return Stream.of(new HearingVacatedTrialDetailUpdated(hearingId, isVacated, vacatedTrialReasonId));
    }

    public Stream<Object> clearVacatedTrial(final UUID hearingId) {
        return Stream.of(new HearingEventVacatedTrialCleared(hearingId));
    }

    public HearingChangeIgnored generateHearingIgnoredMessage(final String reason,
                                                               final UUID hearingId) {
        return new HearingChangeIgnored(hearingId, reason);
    }

    public List<Offence> getAllOffencesMissingCount(final Hearing hearing) {
        return hearing.getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .filter(o -> o.getCount() == null).collect(Collectors.toList());
    }

    public Stream<Object> ignoreHearingInitiate(final List<Offence> offences,
                                                final UUID hearingId) {
        return Stream.of(new HearingInitiateIgnored(hearingId, offences));
    }

    public Stream<Object> addDefendant(final UUID hearingId, final Defendant defendant) {
        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.add-defendant' event as hearing not found", hearingId));
        } else if (checkIfHearingDateAlreadyPassed()) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.add-defendant' event as hearing date has already passed", hearingId));
        }
        return Stream.of(DefendantAdded.caseDefendantAdded().setHearingId(hearingId).setDefendant(defendant));
    }

    private boolean checkIfHearingDateAlreadyPassed() {
        return momento.getHearing().getHearingDays().stream()
                .map(hearingDay -> hearingDay.getSittingDay().toLocalDate())
                .noneMatch(localDate -> (localDate.isAfter(LocalDate.now()) || localDate.isEqual(LocalDate.now())));
    }

    public Stream<Object> updateCourtApplication(final UUID hearingId,
                                                 final CourtApplication courtApplication) {
        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.update-court-application' event as hearing not found", hearingId));
        }
        return Stream.of(new ApplicationDetailChanged(hearingId, courtApplication));
    }

    /**
     * Adds defendants, as long as they don't already exist on the case.
     *
     * @param defendantAdded
     */
    public void handleDefendantAdded(final DefendantAdded defendantAdded) {
        if (momento.getHearing() != null) {
            final List<ProsecutionCase> prosecutionCases = momento.getHearing().getProsecutionCases();

            for (final ProsecutionCase prosecutionCase : prosecutionCases) {
                if (prosecutionCase.getDefendants().stream().map(Defendant::getId).noneMatch(id -> id.equals(defendantAdded.getDefendant().getId()))) {
                    prosecutionCase.getDefendants().add(defendantAdded.getDefendant());
                }
            }
        }
    }

    public void handleApplicationDetailChanged(
            final ApplicationDetailChanged applicationDetailChanged) {
        if (momento.getHearing() != null) {
            final Optional<CourtApplication> previousStoredApplication = momento.getHearing().getCourtApplications().stream()
                    .filter(courtApplication -> courtApplication.getId().equals(applicationDetailChanged.getCourtApplication().getId()))
                    .findFirst();
            previousStoredApplication.ifPresent(courtApplication -> momento.getHearing().getCourtApplications().remove(courtApplication));
            momento.getHearing().getCourtApplications().add(applicationDetailChanged.getCourtApplication());
        }
    }

    public Stream<Object> cancelHearingDays(final UUID hearingId, final List<HearingDay> hearingDays) {
        if (isNull(this.momento.getHearing())) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.command.cancel-hearing-days' event as hearing not found", hearingId));
        }
        return Stream.of(new HearingDaysCancelled(hearingId, hearingDays));
    }

    public Stream<Object> removeTarget(final UUID hearingId, final UUID targetId) {

        final LocalDate hearingDay = getHearingDay();
        final Map<UUID, Target2> targetMap = this.momento.getMultiDayTargets().containsKey(hearingDay) ? this.momento.getMultiDayTargets().get(hearingDay) : new HashMap<>();

        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting action for removing draft target as hearing is null", hearingId));
        } else if (!targetMap.containsKey(targetId)) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting action for removing draft target as target ID not present", hearingId));
        }
        return Stream.of(TargetRemoved.targetRemoved().setHearingId(hearingId).setTargetId(targetId));
    }

    public Stream<Object> addMasterDefendantIdToDefendant(final UUID hearingId, final UUID prosecutionCaseId, final UUID defendantId, final UUID masterDefendantId) {
        return Stream.of(new MasterDefendantIdAdded(hearingId, prosecutionCaseId, defendantId, masterDefendantId));
    }

    public Stream<Object> markAsDuplicate(final UUID hearingId) {
        final List<UUID> prosecutionCaseIds = momento.getHearing().getProsecutionCases().stream().map(ProsecutionCase::getId).collect(Collectors.toList());
        final List<UUID> defendantIds = momento.getHearing().getProsecutionCases().stream().flatMap(c -> c.getDefendants().stream().map(Defendant::getId)).collect(Collectors.toList());
        final List<UUID> offenceIds = momento.getHearing().getProsecutionCases().stream().flatMap(c -> c.getDefendants().stream().flatMap(d -> d.getOffences().stream().map(Offence::getId))).collect(Collectors.toList());
        final UUID courtCentreId = momento.getHearing().getCourtCentre().getId();

        return Stream.of(new HearingMarkedAsDuplicate(prosecutionCaseIds, defendantIds, offenceIds, hearingId, courtCentreId));
    }

    public void handleHearingMarkedAsDuplicate() {
        this.momento.setDuplicate(true);
    }


    public void handleHearingDaysWithoutCourtCentreCorrected(final HearingDaysWithoutCourtCentreCorrected hearingDaysWithoutCourtCentreCorrected) {

        if (momento.getHearing() == null || momento.getHearing().getHearingDays() == null) {
            return;
        }

        final uk.gov.justice.core.courts.HearingDay correctedHearingDay = hearingDaysWithoutCourtCentreCorrected.getHearingDays().get(0);

        momento.getHearing().getHearingDays().forEach(hearingDay -> {
            if (hearingDay.getCourtCentreId() == null) {
                hearingDay.setCourtCentreId(correctedHearingDay.getCourtCentreId());
            }

            if (hearingDay.getCourtRoomId() == null) {
                hearingDay.setCourtRoomId(correctedHearingDay.getCourtRoomId());
            }
        });
    }

    public Stream<Object> deleteHearing(final UUID hearingId) {
        if (isNull(momento.getHearing())) {
            return Stream.empty();
        }
        final List<UUID> prosecutionCaseIds = isNotEmpty(momento.getHearing().getProsecutionCases()) ? getProsecutionCaseIds(momento.getHearing()) : null;
        final List<UUID> defendantIds = isNotEmpty(momento.getHearing().getProsecutionCases()) ? getDefendantIds(momento.getHearing()) : null;
        final List<UUID> offenceIds = isNotEmpty(momento.getHearing().getProsecutionCases()) ? getOffenceIds(momento.getHearing()) : null;
        final List<UUID> courtApplicationIds = isNotEmpty(momento.getHearing().getCourtApplications()) ? getCourtApplicationIds(momento.getHearing()) : null;

        return Stream.of(new HearingDeleted(prosecutionCaseIds, defendantIds, offenceIds, courtApplicationIds, hearingId));
    }

    public Stream<Object> unAllocateHearing(final UUID hearingId, final List<UUID> offencesToBeRemoved) {

        final List<UUID> defendantsToBeRemoved = momento.getHearing().getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> defendant
                        .getOffences()
                        .stream()
                        .filter(offence -> !offencesToBeRemoved.contains(offence.getId()))
                        .collect(Collectors.toList()).isEmpty())
                .map(Defendant::getId)
                .collect(toList());

        final List<UUID> prosecutionCasesToBeRemoved = momento.getHearing().getProsecutionCases()
                .stream()
                .filter(prosecutionCase -> prosecutionCase
                        .getDefendants()
                        .stream().filter(defendant -> !defendantsToBeRemoved.contains(defendant.getId()))
                        .collect(Collectors.toList()).isEmpty())
                .map(ProsecutionCase::getId)
                .collect(Collectors.toList());

        return Stream.of(new HearingUnallocated(prosecutionCasesToBeRemoved, defendantsToBeRemoved, offencesToBeRemoved, hearingId));
    }

    private ProsecutionCase createProsecutionCase(final ProsecutionCase extendedPC, final ProsecutionCase current) {
        return ProsecutionCase.prosecutionCase()
                .withId(current.getId())
                .withDefendants(addDefendants(current.getDefendants(), extendedPC.getDefendants()))
                .withStatementOfFactsWelsh(current.getStatementOfFactsWelsh())
                .withStatementOfFacts(current.getStatementOfFacts())
                .withOriginatingOrganisation(current.getOriginatingOrganisation())
                .withCaseStatus(current.getCaseStatus())
                .withClassOfCase(current.getClassOfCase())
                .withProsecutionCaseIdentifier(current.getProsecutionCaseIdentifier())
                .withAppealProceedingsPending(current.getAppealProceedingsPending())
                .withBreachProceedingsPending(current.getBreachProceedingsPending())
                .withCaseMarkers(current.getCaseMarkers())
                .withPoliceOfficerInCase(current.getPoliceOfficerInCase())
                .withRemovalReason(current.getRemovalReason())
                .withInitiationCode(current.getInitiationCode())
                .withCpsOrganisation(current.getCpsOrganisation())
                .withIsCpsOrgVerifyError(current.getIsCpsOrgVerifyError())
                .build();

    }

    private List<Defendant> addDefendants(final List<Defendant> currentDefendants, final List<Defendant> extendedDefendants) {
        final List<UUID> defendantIdsInEntities = currentDefendants.stream().map(Defendant::getId).collect(toList());
        extendedDefendants.forEach(extended -> {
            if (defendantIdsInEntities.contains(extended.getId())) {
                final Defendant current = currentDefendants.stream()
                        .filter(def -> def.getId().equals(extended.getId()))
                        .findFirst()
                        .orElse(null);
                final Defendant defendantToBeAdded = createDefendant(extended, current);
                currentDefendants.removeIf(defToBeRemoved -> defToBeRemoved.getId().equals(extended.getId()));
                currentDefendants.add(defendantToBeAdded);
            } else {
                currentDefendants.add(extended);
            }
        });
        return currentDefendants;
    }

    private static Defendant createDefendant(final Defendant extended, final Defendant current) {
        return Defendant.defendant()
                .withId(current.getId())
                .withOffences(addOffences(current.getOffences(), extended.getOffences()))
                .withMasterDefendantId(current.getMasterDefendantId())
                .withPncId(current.getPncId())
                .withCroNumber(current.getCroNumber())
                .withPersonDefendant(current.getPersonDefendant())
                .withProsecutionCaseId(current.getProsecutionCaseId())
                .withProceedingsConcluded(current.getProceedingsConcluded())
                .withAssociatedPersons(current.getAssociatedPersons())
                .withCourtProceedingsInitiated(current.getCourtProceedingsInitiated())
                .withLegalEntityDefendant(current.getLegalEntityDefendant())
                .withDefenceOrganisation(current.getDefenceOrganisation())
                .withIsYouth(current.getIsYouth())
                .withNumberOfPreviousConvictionsCited(current.getNumberOfPreviousConvictionsCited())
                .withProsecutionAuthorityReference(current.getProsecutionAuthorityReference())
                .withAssociatedDefenceOrganisation(current.getAssociatedDefenceOrganisation())
                .withLegalAidStatus(current.getLegalAidStatus())
                .withMitigation(current.getMitigation())
                .withMitigationWelsh(current.getMitigationWelsh())
                .withWitnessStatement(current.getWitnessStatement())
                .withWitnessStatementWelsh(current.getWitnessStatementWelsh())
                .withAliases(current.getAliases())
                .withAssociationLockedByRepOrder(current.getAssociationLockedByRepOrder())
                .withDefendantCaseJudicialResults(current.getDefendantCaseJudicialResults())
                .build();
    }

    private static List<Offence> addOffences(final List<Offence> currentOffences, final List<Offence> extendedOffences) {
        final List<UUID> offencesIdsInCurrent = currentOffences.stream()
                .map(Offence::getId)
                .collect(toList());

        extendedOffences.forEach(offence -> {
            if (!offencesIdsInCurrent.contains(offence.getId())) {
                currentOffences.add(offence);
            }
        });
        return currentOffences;
    }

    public void handleHearingDeleted() {
        this.momento.setDeleted(true);
    }

    public void handleHearingUnallocated(final HearingUnallocated hearingUnallocated) {
        this.momento.setDeleted(true);

        final List<UUID> offencesToBeRemoved = hearingUnallocated.getProsecutionCaseIds();
        final List<UUID> defendantsToBeRemoved = hearingUnallocated.getProsecutionCaseIds();
        final List<UUID> prosecutionCasesToBeRemoved = hearingUnallocated.getProsecutionCaseIds();

        // Remove offences from all defendants
        momento.getHearing().getProsecutionCases().forEach(
                prosecutionCase -> prosecutionCase.getDefendants().forEach(
                        defendant -> defendant.getOffences()
                                .removeIf(offence -> offencesToBeRemoved.contains(offence.getId()))
                )
        );

        // Remove defendants with no offences from all prosecution cases
        momento.getHearing().getProsecutionCases().forEach(
                prosecutionCase -> prosecutionCase.getDefendants()
                        .removeIf(defendant -> defendantsToBeRemoved.contains(defendant.getId()))
        );

        // Remove prosecution cases with no defendants
        momento.getHearing().getProsecutionCases()
                .removeIf(prosecutionCase -> prosecutionCasesToBeRemoved.contains(prosecutionCase.getId()));
    }

    public Stream<Object> changeNextHearingStartDate(final UUID hearingId, final UUID seedingHearingId, final ZonedDateTime nextHearingStartDate) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();
        streamBuilder.add(new NextHearingStartDateRecorded(hearingId, seedingHearingId, nextHearingStartDate));

        if (momento.getNextHearingStartDates().isEmpty()) {

            streamBuilder.add(new EarliestNextHearingDateChanged(hearingId, seedingHearingId, nextHearingStartDate));

        } else if (isNextHearingDateTheEarliestWhenNewNextHearing(hearingId, nextHearingStartDate)) {

            streamBuilder.add(new EarliestNextHearingDateChanged(hearingId, seedingHearingId, nextHearingStartDate));

        } else if (isUpdatedNextHearingTheOnlyNextHearing(hearingId)) {

            streamBuilder.add(new EarliestNextHearingDateChanged(hearingId, seedingHearingId, nextHearingStartDate));

        } else if (isEarliestNextHearing(hearingId, nextHearingStartDate)) {

            streamBuilder.add(new EarliestNextHearingDateChanged(hearingId, seedingHearingId, nextHearingStartDate));

        }

        return streamBuilder.build();
    }

    /**
     * This method return true when the next hearing (hearingId) is not available in the map and the
     * date 'nextHearingStartDate' for hearingId is the earliest in the map.
     *
     * @param hearingId
     * @param nextHearingStartDate
     * @return
     */
    private boolean isNextHearingDateTheEarliestWhenNewNextHearing(final UUID hearingId, final ZonedDateTime nextHearingStartDate) {

        return isNull(momento.getNextHearingStartDates().get(hearingId))
                && isEarliestNextHearingStartDate(hearingId, nextHearingStartDate, momento.getNextHearingStartDates());

    }

    /**
     * This method return true when the next hearing (hearingId) is available in the map and is the
     * only entry available in the map. It doesn't matter if the new date is earlier or later than
     * the previous entry in the map for the same hearing, the date has moved so the previous date
     * is no longer valid, so we use the new date always.
     *
     * @param hearingId
     * @return
     */
    private boolean isUpdatedNextHearingTheOnlyNextHearing(final UUID hearingId) {
        return momento.getNextHearingStartDates().size() == 1
                && nonNull(momento.getNextHearingStartDates().get(hearingId));
    }

    private boolean isEarliestNextHearing(final UUID hearingId, final ZonedDateTime nextHearingStartDate) {
        return isEarliestNextHearingStartDate(hearingId, nextHearingStartDate, momento.getNextHearingStartDates());
    }


    private boolean isEarliestNextHearingStartDate(final UUID hearingId, final ZonedDateTime nextHearingStartDate, final Map<UUID, ZonedDateTime> nextHearingStartDates) {

        final Optional<ZonedDateTime> earliestStartDate = nextHearingStartDates.entrySet().stream()
                .filter(map -> !(map.getKey().equals(hearingId)))
                .map(Map.Entry::getValue)
                .min(comparing(ZonedDateTime::toLocalDate));

        return earliestStartDate.isPresent() && nextHearingStartDate.isBefore(earliestStartDate.get());

    }

    public void handleNextHearingStartDateRecorded(final NextHearingStartDateRecorded nextHearingStartDateRecorded) {
        momento.getNextHearingStartDates().put(nextHearingStartDateRecorded.getHearingId(), nextHearingStartDateRecorded.getNextHearingStartDate());
    }

    public void handleEarliestNextHearingDateCleared() {
        momento.getNextHearingStartDates().clear();
    }

    private LocalDate getHearingDay() {
        return this.momento.getHearing().getHearingDays().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hearing Day is not present"))
                .getSittingDay()
                .toLocalDate();
    }

    private List<UUID> getProsecutionCaseIds(final Hearing hearing) {
        return hearing
                .getProsecutionCases()
                .stream()
                .map(ProsecutionCase::getId)
                .collect(toList());
    }

    private List<UUID> getDefendantIds(final Hearing hearing) {
        return hearing.getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants()
                        .stream()
                        .map(Defendant::getId))
                .collect(toList());

    }

    private List<UUID> getOffenceIds(final Hearing hearing) {
        return hearing.getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants()
                        .stream()
                        .flatMap(defendant -> defendant.getOffences()
                                .stream()
                                .map(Offence::getId)))
                .collect(toList());
    }

    private List<UUID> getCourtApplicationIds(final Hearing hearing) {
        return hearing
                .getCourtApplications()
                .stream()
                .map(CourtApplication::getId)
                .collect(toList());
    }

}
