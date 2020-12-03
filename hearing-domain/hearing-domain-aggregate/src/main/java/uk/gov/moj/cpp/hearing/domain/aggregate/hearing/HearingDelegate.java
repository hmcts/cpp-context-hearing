package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.HearingResultsCleanerUtil.removeResultsFromHearing;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysCancelled;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiateIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicate;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacatedTrialDetailUpdated;
import uk.gov.moj.cpp.hearing.domain.event.MasterDefendantIdAdded;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("squid:S00107")
public class HearingDelegate implements Serializable {

    private static final long serialVersionUID = 2L;

    private final HearingAggregateMomento momento;

    public HearingDelegate(HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleHearingInitiated(HearingInitiated hearingInitiated) {
        final Hearing hearing = hearingInitiated.getHearing();
        this.momento.setHearing(hearing);

        if (isNull(hearing) || isNull(hearing.getProsecutionCases())) {
            return;
        }

        this.momento.getHearing().getProsecutionCases().forEach(
                prosecutionCase -> prosecutionCase.getDefendants().forEach(
                        defendant -> defendant.getOffences().forEach(offence -> {

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
                        })));
    }

    public void handleHearingExtended(final HearingExtended hearingExtended) {
        if (nonNull(this.momento.getHearing()) && nonNull(hearingExtended.getCourtApplication())) {
            final List<CourtApplication> oldCourtApplications = this.momento.getHearing().getCourtApplications();
            final List<CourtApplication> newCourtApplications = oldCourtApplications == null ? new ArrayList<>() :
                    oldCourtApplications.stream()
                            .filter(ca -> !ca.getId().equals(hearingExtended.getCourtApplication().getId()))
                            .collect(Collectors.toList());
            newCourtApplications.add(hearingExtended.getCourtApplication());
            this.momento.getHearing().setCourtApplications(newCourtApplications);
        }

        if (nonNull(this.momento.getHearing()) && isNotEmpty(hearingExtended.getProsecutionCases())) {
            final List<ProsecutionCase> oldProsecutionCases = this.momento.getHearing().getProsecutionCases();
            final List<ProsecutionCase> newProsecutionCases = oldProsecutionCases == null ? new ArrayList<>() :
                    oldProsecutionCases.stream()
                            .filter(ca -> !(hearingExtended.getProsecutionCases().stream()
                                    .map(ProsecutionCase::getId)
                                    .collect(Collectors.toList()))
                                    .contains(ca.getId()))
                            .collect(Collectors.toList());
            newProsecutionCases.addAll(hearingExtended.getProsecutionCases());
            this.momento.getHearing().setProsecutionCases(newProsecutionCases);
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
        this.momento.getTargets().remove(targetId);
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
                                 final CourtApplication courtApplication, final List<ProsecutionCase> prosecutionCases,
                                 final List<UUID> shadowListedOffences) {

        return Stream.of(new HearingExtended(hearingId, courtApplication, prosecutionCases, shadowListedOffences));
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

    private HearingChangeIgnored generateHearingIgnoredMessage(final String reason,
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

    public void handleDefendantAdded(final DefendantAdded defendantAdded) {
        if (momento.getHearing() != null) {
            momento.getHearing().getProsecutionCases().forEach(prosecutionCase -> prosecutionCase.getDefendants().add(defendantAdded.getDefendant()));
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
        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting action for removing draft target as hearing is null", hearingId));
        } else if (!this.momento.getTargets().containsKey(targetId)) {
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

        return Stream.of(new HearingMarkedAsDuplicate(prosecutionCaseIds, defendantIds, offenceIds, hearingId));
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
}
