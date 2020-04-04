package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.stream.Collectors.toSet;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("squid:S3776")
public class ResultsSharedDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ResultsSharedDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleResultsShared(ResultsShared resultsShared) {
        this.momento.setPublished(true);
        this.momento.setVariantDirectory(resultsShared.getVariantDirectory());
        this.momento.setSavedTargets(resultsShared.getTargets().stream().filter(Objects::nonNull).collect(Collectors.toMap(Target::getTargetId, target -> target, (target1, target2) -> target1)));
    }

    public void handleResultLinesStatusUpdated(ResultLinesStatusUpdated resultLinesStatusUpdated) {

        resultLinesStatusUpdated.getSharedResultLines().forEach(sharedResultLine ->

                this.momento.getCompletedResultLinesStatus().computeIfAbsent(sharedResultLine.getSharedResultLineId(), resultLineId ->
                        CompletedResultLineStatus.builder()
                                .withId(resultLineId)
                                .build()
                )
                        .setCourtClerk(resultLinesStatusUpdated.getCourtClerk())
                        .setLastSharedDateTime(resultLinesStatusUpdated.getLastSharedDateTime())

        );

        momento.getTargets().values().stream()
                .map(target -> {
                    if (target.getResultLines() == null) {
                        target.setResultLines(Collections.emptyList());
                    }
                    return target;
                })
                .flatMap(target -> target.getResultLines().stream())
                .filter(line -> resultLinesStatusUpdated.getSharedResultLines().stream()
                        .map(SharedResultLineId::getSharedResultLineId)
                        .collect(toSet()).contains(line.getResultLineId()))
                .forEach(resultLine -> resultLine.setIsModified(false));

    }

    public void handleDraftResultSaved(final DraftResultSaved draftResultSaved) {
        final UUID targetId = draftResultSaved.getTarget().getTargetId();
        if (this.momento.getTargets().containsKey(targetId)) {
            // assuming existing target matches defendantId, offenceId
            this.momento.getTargets().get(targetId).setDraftResult(draftResultSaved.getTarget().getDraftResult());
        } else {
            draftResultSaved.getTarget().setResultLines(Collections.emptyList());
            this.momento.getTargets().put(targetId, draftResultSaved.getTarget());
        }

    }

    public Stream<Object> saveDraftResult(final Target target) {
        return Stream.of(new DraftResultSaved(target));
    }

    public Stream<Object> applicationDraftResult(final UUID targetId, final UUID applicationId, final UUID hearingId, final String draftResult, final CourtApplicationOutcomeType applicationOutcomeType, final LocalDate applicationOutcomeDate) {
        return Stream.of(ApplicationDraftResulted.applicationDraftResulted()
                .setApplicationId(applicationId)
                .setTargetId(targetId)
                .setHearingId(hearingId)
                .setDraftResult(draftResult)
                .setApplicationOutcomeType(applicationOutcomeType)
                .setApplicationOutcomeDate(applicationOutcomeDate));
    }

    public void handleApplicationDraftResulted(final ApplicationDraftResulted applicationDraftResulted) {
        //place holder method
    }

    private ResultLine convert(final SharedResultsCommandResultLine resultLineIn) {
        return ResultLine.resultLine()
                .withResultLineId(resultLineIn.getResultLineId())
                .withIsModified(resultLineIn.getIsModified())
                .withIsComplete(resultLineIn.getIsComplete())
                .withSharedDate(resultLineIn.getSharedDate())
                .withOrderedDate(resultLineIn.getOrderedDate())
                .withResultLabel(resultLineIn.getResultLabel())
                .withResultDefinitionId(resultLineIn.getResultDefinitionId())
                .withLevel(Level.valueOf(resultLineIn.getLevel()))
                .withPrompts(
                        resultLineIn.getPrompts().stream().map(pin -> Prompt.prompt()
                                .withFixedListCode(pin.getFixedListCode())
                                .withValue(pin.getValue())
                                .withWelshValue(pin.getWelshValue())
                                .withWelshLabel(pin.getWelshLabel())
                                .withId(pin.getId())
                                .withLabel(pin.getLabel())
                                .build())
                                .collect(Collectors.toList())
                )
                .withDelegatedPowers(resultLineIn.getDelegatedPowers())
                .withAmendmentDate(resultLineIn.getAmendmentDate())
                .withAmendmentReasonId(resultLineIn.getAmendmentReasonId())
                .withAmendmentReason(resultLineIn.getAmendmentReason())
                .withApprovedDate(resultLineIn.getApprovedDate())
                .withFourEyesApproval(resultLineIn.getFourEyesApproval())
                .withIsDeleted(resultLineIn.getIsDeleted())
                .build();
    }

    public Stream<Object> shareResults(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLine> resultLines) {
        final Map<UUID, Target> finalTargets = new HashMap<>();
        final Map<UUID, Target> targets = new HashMap<>();
        resultLines.forEach(
                rl -> {
                    Target target;
                    if (targets.containsKey(rl.getTargetId())) {
                        target = targets.get(rl.getTargetId());
                    } else {
                        target = new Target(rl.getApplicationId(), rl.getDefendantId(), null, hearingId, rl.getOffenceId(), new ArrayList<>(), rl.getTargetId());
                        targets.put(target.getTargetId(), target);
                    }
                    target.getResultLines().add(convert(rl));
                }
        );

        targets.values().forEach(
                target -> {
                    if (finalTargets.containsKey(target.getTargetId()) && !StringUtils.isEmpty(target.getDraftResult())) {
                        //retain draft result
                        target.setDraftResult(this.momento.getTargets().get(target.getTargetId()).getDraftResult());
                    }
                    finalTargets.put(target.getTargetId(), target);
                }
        );

        return Stream.concat(enrichHearing(resultLines), Stream.of(ResultsShared.builder()
                .withHearingId(hearingId)
                .withSharedTime(sharedTime)
                .withCourtClerk(courtClerk)
                .withVariantDirectory(calculateNewVariants())
                .withHearing(this.momento.getHearing())
                .withTargets(new ArrayList<>(finalTargets.values()))
                .withSavedTargets(new ArrayList<>(momento.getSavedTargets().values()))
                .withCompletedResultLinesStatus(this.momento.getCompletedResultLinesStatus())
                .build()));
    }

    private Stream<Object> enrichHearing(final List<SharedResultsCommandResultLine> resultLines) {
        setSharedResults(resultLines);
        updateCounsels();
        updateCompanyRepresentatives();
        updateOffence();
        return updateApplicationOutcomes(resultLines);
    }

    private void setSharedResults(final List<SharedResultsCommandResultLine> resultLines) {
        this.momento.getHearing().setHasSharedResults(resultLines.stream().filter(SharedResultsCommandResultLine::getIsComplete).count() == resultLines.size());
        this.momento.getHearing().setHasSharedResults(true);
    }

    private Stream<Object> updateApplicationOutcomes(final List<SharedResultsCommandResultLine> resultLines) {
        Stream<Object> applicationChanged = Stream.empty();
        if (this.momento.getHearing().getCourtApplications() != null) {
            final List<CourtApplication> courtApplications = momento.getHearing().getCourtApplications();
            final Map<UUID, CourtApplicationOutcome> courtApplicationOutcomeMap =
                    resultLines.stream().map(SharedResultsCommandResultLine::getApplicationOutcome).filter(Objects::nonNull).collect(Collectors.toMap(CourtApplicationOutcome::getApplicationId, courtApplicationOutcome -> courtApplicationOutcome, (courtApplicationOutcome1, courtApplicationOutcome2) -> courtApplicationOutcome1));
            for (final CourtApplication courtApplication : courtApplications) {
                if (courtApplicationOutcomeMap.containsKey(courtApplication.getId()) && !courtApplicationOutcomeMap.get(courtApplication.getId()).equals(courtApplication.getApplicationOutcome())) {
                    applicationChanged = Stream.concat(checkAndUpdateApplicationOutcomes(courtApplicationOutcomeMap, courtApplication), applicationChanged);
                }
            }
        }
        return applicationChanged;
    }

    private void updateOffence() {
        if (this.momento.getHearing().getProsecutionCases() != null && !this.momento.getHearing().getProsecutionCases().isEmpty()) {
            this.momento.getHearing().getProsecutionCases().forEach(prosecutionCase -> prosecutionCase.getDefendants().forEach(defendant -> defendant.getOffences().forEach(offence -> {
                if (momento.getPleas() != null && momento.getPleas().containsKey(offence.getId())) {
                    offence.setPlea(momento.getPleas().get(offence.getId()));
                }
                if (momento.getIndicatedPlea() != null && momento.getIndicatedPlea().containsKey(offence.getId())) {
                    offence.setIndicatedPlea(momento.getIndicatedPlea().get(offence.getId()));
                }
                if (momento.getAllocationDecision() != null && momento.getAllocationDecision().containsKey(offence.getId())) {
                    offence.setAllocationDecision(momento.getAllocationDecision().get(offence.getId()));
                }
                if (momento.getVerdicts().containsKey(offence.getId())) {
                    offence.setVerdict(momento.getVerdicts().get(offence.getId()));
                }
                if (momento.getConvictionDates().containsKey(offence.getId())) {
                    offence.setConvictionDate(momento.getConvictionDates().get(offence.getId()));
                }
            })));
        }
    }

    private void updateCounsels() {
        if (!this.momento.getProsecutionCounsels().isEmpty()) {
            this.momento.getHearing().setProsecutionCounsels(new ArrayList<>(this.momento.getProsecutionCounsels().values()));
        }
        if (!this.momento.getDefenceCounsels().isEmpty()) {
            this.momento.getHearing().setDefenceCounsels(new ArrayList<>(this.momento.getDefenceCounsels().values()));
        }
        if (!this.momento.getApplicantCounsels().isEmpty()) {
            this.momento.getHearing().setApplicantCounsels(new ArrayList<>(this.momento.getApplicantCounsels().values()));
        }
        if (!this.momento.getRespondentCounsels().isEmpty()) {
            this.momento.getHearing().setRespondentCounsels(new ArrayList<>(this.momento.getRespondentCounsels().values()));
        }
    }

    private void updateCompanyRepresentatives() {
        if (!this.momento.getCompanyRepresentatives().isEmpty()) {
            this.momento.getHearing().setCompanyRepresentatives(new ArrayList<>(this.momento.getCompanyRepresentatives().values()));
        }
    }

    private Stream<Object> checkAndUpdateApplicationOutcomes(final Map<UUID, CourtApplicationOutcome> courtApplicationOutcomeMap, final CourtApplication courtApplication) {
        final CourtApplicationOutcome courtApplicationOutcome = courtApplicationOutcomeMap.get(courtApplication.getId());
        courtApplicationOutcome.setOriginatingHearingId(momento.getHearing().getId());
        courtApplication.setApplicationOutcome(courtApplicationOutcome);
        return Stream.of(new ApplicationDetailChanged(momento.getHearing().getId(), courtApplication));
    }

    public Stream<Object> updateResultLinesStatus(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines) {
        return Stream.of(ResultLinesStatusUpdated.builder()
                .withHearingId(hearingId)
                .withLastSharedDateTime(lastSharedDateTime)
                .withSharedResultLines(sharedResultLines)
                .withCourtClerk(courtClerk)
                .build());
    }

    private List<Variant> calculateNewVariants() {

        //We cull variants that have result lines that are no longer present.

        List<UUID> completedResultLineIds = this.momento.getTargets().values().stream()
                .flatMap(t -> t.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .map(ResultLine::getResultLineId)
                .collect(Collectors.toList());

        return this.momento.getVariantDirectory().stream()
                .filter(variant -> {
                    List<UUID> resultLineIds = variant.getValue().getResultLines().stream().map(ResultLineReference::getResultLineId).collect(Collectors.toList());

                    resultLineIds.removeAll(completedResultLineIds);

                    return resultLineIds.isEmpty();
                    //if not empty, it means we have a variant that is based on some completed result lines that are no longer in the incoming set.
                    //The incoming set is an exhaustive set of completed result lines since the UI is the authority on those lines.
                    //If a line is not included, it has been deleted.
                    //Which means we have a variant that is based on a line that has been deleted.
                    //We should delete the variant.
                })
                .collect(Collectors.toList());
    }
}