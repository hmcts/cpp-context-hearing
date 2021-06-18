package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.core.courts.Target.target;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.util.CustodyTimeLimitUtil;
import uk.gov.moj.cpp.hearing.domain.event.HearingLocked;
import uk.gov.moj.cpp.hearing.domain.event.HearingLockedByOtherUser;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateCleared;
import uk.gov.moj.cpp.hearing.domain.event.result.DaysResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({"squid:S3776", "PMD.BeanMembersShouldSerialize"})
public class ResultsSharedDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ResultsSharedDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleResultsShared(final ResultsShared resultsShared) {
        this.momento.setPublished(true);
        this.momento.setVariantDirectory(resultsShared.getVariantDirectory());
        final LocalDate hearingDay = getHearingDay();
        this.momento.getMultiDaySavedTargets().put(hearingDay, resultsShared.getTargets().stream().filter(Objects::nonNull).collect(Collectors.toMap(Target::getTargetId, target -> target, (target1, target2) -> target1)));
        recordTargetsSharedAndResetTransientTargets();
        this.momento.setLastSharedTime(resultsShared.getSharedTime());
        this.momento.getIsHearingDayPreviouslyShared().put(hearingDay, true);
        this.momento.getHearing().setYouthCourt(resultsShared.getHearing().getYouthCourt());
    }

    public void handleResultsSharedV2(final ResultsSharedV2 resultsShared) {
        this.momento.setPublished(true);
        this.momento.setVariantDirectory(resultsShared.getVariantDirectory());
        this.momento.getMultiDaySavedTargets().put(resultsShared.getHearingDay(), resultsShared.getTargets().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Target::getTargetId, target -> target, (target1, target2) -> target1)));
        recordTargetsSharedAndResetTransientTargets();
        this.momento.getIsHearingDayPreviouslyShared().put(resultsShared.getHearingDay(), true);
        resultsShared.getNewAmendmentResults()
                .forEach(newAmendmentResult -> this.momento.getResultsAmendmentDateMap().put(newAmendmentResult.getId(), newAmendmentResult.getAmendmentDateTime()));
        this.momento.setLastSharedTime(resultsShared.getSharedTime());
        this.momento.getHearing().setYouthCourt(resultsShared.getHearing().getYouthCourt());
    }

    private void recordTargetsSharedAndResetTransientTargets() {
        final Collection<Target> transientValues = this.momento.getTransientTargets().values();
        transientValues.stream().forEach(t -> {final Target target = this.momento.getSharedTargets().getOrDefault(t.getTargetId(), t);
            target.setDraftResult(t.getDraftResult());
            this.momento.getSharedTargets().put(t.getTargetId(), target);
        });
        this.momento.getTransientTargets().clear();
    }

    public void handleResultLinesStatusUpdated(final ResultLinesStatusUpdated resultLinesStatusUpdated) {

        final LocalDate hearingDay = getHearingDay();

        final Map<UUID, CompletedResultLineStatus> statusMap = this.momento.getMultiDayCompletedResultLinesStatus().containsKey(hearingDay) ?
                this.momento.getMultiDayCompletedResultLinesStatus().get(hearingDay) : new HashMap<>();

        resultLinesStatusUpdated.getSharedResultLines().forEach(sharedResultLine ->

                statusMap.computeIfAbsent(sharedResultLine.getSharedResultLineId(), resultLineId -> CompletedResultLineStatus.builder()
                        .withId(resultLineId)
                        .build())
                        .setCourtClerk(resultLinesStatusUpdated.getCourtClerk())
                        .setLastSharedDateTime(resultLinesStatusUpdated.getLastSharedDateTime())
        );

        this.momento.getMultiDayCompletedResultLinesStatus().put(hearingDay, statusMap);

        final Map<UUID, Target> targetMap = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : new HashMap<>();

        targetMap.values().stream()
                .map(target -> {
                    if (target.getResultLines() == null) {
                        target.setResultLines(emptyList());
                    }
                    return target;
                })
                .flatMap(target -> target.getResultLines().stream())
                .filter(line -> resultLinesStatusUpdated.getSharedResultLines().stream()
                        .map(SharedResultLineId::getSharedResultLineId)
                        .collect(toSet()).contains(line.getResultLineId()))
                .forEach(resultLine -> resultLine.setIsModified(false));

    }

    public void handleDaysResultLinesStatusUpdated(final DaysResultLinesStatusUpdated daysResultLinesStatusUpdated) {

        final LocalDate hearingDay = daysResultLinesStatusUpdated.getHearingDay();

        final Map<UUID, CompletedResultLineStatus> statusMap = this.momento.getMultiDayCompletedResultLinesStatus().containsKey(hearingDay) ?
                this.momento.getMultiDayCompletedResultLinesStatus().get(hearingDay) : new HashMap<>();

        daysResultLinesStatusUpdated.getSharedResultLines().forEach(sharedResultLine ->

                statusMap.computeIfAbsent(sharedResultLine.getSharedResultLineId(), resultLineId -> CompletedResultLineStatus.builder()
                        .withId(resultLineId)
                        .build())
                        .setCourtClerk(daysResultLinesStatusUpdated.getCourtClerk())
                        .setLastSharedDateTime(daysResultLinesStatusUpdated.getLastSharedDateTime())
        );

        this.momento.getMultiDayCompletedResultLinesStatus().put(hearingDay, statusMap);

        final Map<UUID, Target> targetMap = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : new HashMap<>();

        targetMap.values().stream()
                .map(target -> {
                    if (target.getResultLines() == null) {
                        target.setResultLines(emptyList());
                    }
                    return target;
                })
                .flatMap(target -> target.getResultLines().stream())
                .filter(line -> daysResultLinesStatusUpdated.getSharedResultLines().stream()
                        .map(SharedResultLineId::getSharedResultLineId)
                        .collect(toSet()).contains(line.getResultLineId()))
                .forEach(resultLine -> resultLine.setIsModified(false));

    }

    public void handleDraftResultSaved(final DraftResultSaved draftResultSaved) {
        final Target target = draftResultSaved.getTarget();
        final LocalDate hearingDay = nonNull(target.getHearingDay()) ? target.getHearingDay() : getHearingDay();
        final UUID targetId = target.getTargetId();
        updateTransientTargets(targetId, target);

        if (this.momento.getMultiDayTargets().containsKey(hearingDay) && this.momento.getMultiDayTargets().get(hearingDay).containsKey(targetId)) {
            this.momento.getMultiDayTargets().get(hearingDay).get(targetId).setDraftResult(draftResultSaved.getTarget().getDraftResult());
            this.momento.getMultiDayTargets().get(hearingDay).get(targetId).setShadowListed(target.getShadowListed());
        } else {
            draftResultSaved.getTarget().setResultLines(emptyList());
            if (this.momento.getMultiDayTargets().containsKey(hearingDay)) {
                this.momento.getMultiDayTargets().get(hearingDay).put(targetId, draftResultSaved.getTarget());
            } else {
                final Map<UUID, Target> newTarget = new HashMap<>();
                newTarget.put(targetId, draftResultSaved.getTarget());
                this.momento.getMultiDayTargets().put(hearingDay, newTarget);
            }
        }

    }

    private void updateTransientTargets(final UUID targetId, final Target target) {
        this.momento.getTransientTargets().put(targetId, target);
    }

    public Stream<Object> saveDraftResult(final Target target, final HearingState hearingState, final UUID userId) {
        return Stream.of(new DraftResultSaved(target, hearingState, userId));
    }

    public Stream<Object> rejectSaveDraftResult(final Target target) {
        return Stream.of(new SaveDraftResultFailed(target));
    }

    public Stream<Object> hearingLocked(final UUID hearingId) {
        return Stream.of(HearingLocked.builder().withHearingId(hearingId).build());
    }

    public Stream<Object> hearingLockedByOtherUser(final UUID hearingId) {
        return Stream.of(HearingLockedByOtherUser.builder().withHearingId(hearingId).build());
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
                                .withPromptRef(pin.getPromptRef())
                                .build())
                                .collect(toList())
                )
                .withDelegatedPowers(resultLineIn.getDelegatedPowers())
                .withAmendmentDate(resultLineIn.getAmendmentDate())
                .withAmendmentReasonId(resultLineIn.getAmendmentReasonId())
                .withAmendmentReason(resultLineIn.getAmendmentReason())
                .withApprovedDate(resultLineIn.getApprovedDate())
                .withFourEyesApproval(resultLineIn.getFourEyesApproval())
                .withIsDeleted(resultLineIn.getIsDeleted())
                .withChildResultLineIds(resultLineIn.getChildResultLineIds())
                .withParentResultLineIds(resultLineIn.getParentResultLineIds())
                .build();
    }

    @SuppressWarnings("pmd:NullAssignment")
    private ResultLine convert(final SharedResultsCommandResultLineV2 resultLineIn) {
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
                                .withPromptRef(pin.getPromptRef())
                                .build())
                                .collect(toList())
                )
                .withDelegatedPowers(resultLineIn.getDelegatedPowers())
                .withAmendmentDate(nonNull(resultLineIn.getAmendmentDate()) ? resultLineIn.getAmendmentDate().toLocalDate() : null)
                .withAmendmentReasonId(resultLineIn.getAmendmentReasonId())
                .withAmendmentReason(resultLineIn.getAmendmentReason())
                .withApprovedDate(resultLineIn.getApprovedDate())
                .withFourEyesApproval(resultLineIn.getFourEyesApproval())
                .withIsDeleted(resultLineIn.getIsDeleted())
                .withChildResultLineIds(resultLineIn.getChildResultLineIds())
                .withParentResultLineIds(resultLineIn.getParentResultLineIds())
                .build();
    }

    public Stream<Object> shareResults(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLine> resultLines, final List<UUID> defendantDetailsChanged, final YouthCourt youthCourt) {

        final LocalDate hearingDay = getHearingDay();
        final Map<UUID, Target> targetsInAggregate = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : emptyMap();

        final Map<UUID, Target> finalTargets = new HashMap<>();
        final Map<UUID, Target> targets = new HashMap<>();

        resultLines.forEach(
                rl -> {
                    Target target;
                    if (targets.containsKey(rl.getTargetId())) {
                        target = targets.get(rl.getTargetId());
                    } else {
                        target = target()
                                .withApplicationId(rl.getApplicationId())
                                .withDefendantId(rl.getDefendantId())
                                .withHearingId(hearingId)
                                .withOffenceId(rl.getOffenceId())
                                .withResultLines(new ArrayList<>())
                                .withTargetId(rl.getTargetId())
                                .build();
                        targets.put(target.getTargetId(), target);
                    }
                    setShadowListed(target, rl.getTargetId(), targetsInAggregate);
                    target.getResultLines().add(convert(rl));
                }
        );
        targets.values().forEach(target ->
                setDraftResultAndPutFinalTargetsMap(finalTargets, target, targetsInAggregate));

        final Hearing hearing = this.momento.getHearing();
        hearing.setYouthCourt(youthCourt);

        final ResultsShared.Builder builder = ResultsShared.builder()
                .withHearingId(hearingId)
                .withSharedTime(sharedTime)
                .withCourtClerk(courtClerk)
                .withVariantDirectory(calculateNewVariants(targetsInAggregate))
                .withHearing(hearing)
                .withTargets(new ArrayList<>(finalTargets.values()))
                .withSavedTargets(getSavedTargetsForHearingDay(momento.getMultiDaySavedTargets(), hearingDay))
                .withCompletedResultLinesStatus(getCompletedResultLineStatusForHearingDay(this.momento.getMultiDayCompletedResultLinesStatus(), hearingDay));
        if (!defendantDetailsChanged.isEmpty()) {
            builder.withDefendantDetailsChanged(defendantDetailsChanged);
        }

        final Stream<Object> streams = Stream.concat(enrichHearing(resultLines), Stream.of(builder.build()));
        return Stream.concat(streams, CustodyTimeLimitUtil.stopCTLExpiry(this.momento, resultLines));
    }

    public Stream<Object> shareResultsV2(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLineV2> resultLines, final List<UUID> defendantDetailsChanged) {
        final Boolean previouslyShared = Boolean.TRUE.equals(momento.getHearing().getHasSharedResults());

        final Map<UUID, Target> finalTargets = new HashMap<>();
        final Map<UUID, Target> targets = new HashMap<>();
        final List<NewAmendmentResult> newAmendmentResults = new ArrayList<>();
        final LocalDate hearingDay = getHearingDay();
        final Map<UUID, Target> targetsInAggregate = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : emptyMap();

        resultLines.forEach(
                rl -> {
                    final Target target = createTargetAndPutsToTargetsMap(hearingId, targets, rl, hearingDay);
                    setShadowListed(target, rl.getTargetId(), targetsInAggregate);
                    target.getResultLines().add(convert(rl));

                    if (isNewAmendmentResultOrNewResult(rl)) {
                        newAmendmentResults.add(new NewAmendmentResult(rl.getResultLineId(), rl.getAmendmentDate()));
                    }
                }
        );

        targets.values().forEach(target ->
                setDraftResultAndPutFinalTargetsMap(finalTargets, target, targetsInAggregate));


        final Stream.Builder<Object> streamBuilder = Stream.builder();

        final ResultsSharedV2.Builder resultsSharedV2Builder = ResultsSharedV2.builder()
                .withIsReshare(previouslyShared)
                .withHearingId(hearingId)
                .withSharedTime(sharedTime)
                .withCourtClerk(courtClerk)
                .withVariantDirectory(calculateNewVariants(targetsInAggregate))
                .withHearing(this.momento.getHearing())
                .withTargets(new ArrayList<>(finalTargets.values()))
                .withSavedTargets(getSavedTargetsForHearingDay(momento.getMultiDaySavedTargets(), hearingDay))
                .withCompletedResultLinesStatus(getCompletedResultLineStatusForHearingDay(this.momento.getMultiDayCompletedResultLinesStatus(), hearingDay))
                .withNewAmendmentResults(newAmendmentResults);
        if (!defendantDetailsChanged.isEmpty()) {
            resultsSharedV2Builder.withDefendantDetailsChanged(defendantDetailsChanged);
        }
        streamBuilder.add(resultsSharedV2Builder.build());

        if (!this.momento.getNextHearingStartDates().isEmpty()) {
            streamBuilder.add(new EarliestNextHearingDateCleared(hearingId));
        }

        final Stream<Object> streams = Stream.concat(enrichHearingV2(resultLines), streamBuilder.build());
        return Stream.concat(streams, CustodyTimeLimitUtil.stopCTLExpiryForV2(this.momento, resultLines));
    }

    public Stream<Object> shareResultForDay(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLineV2> resultLines, final List<UUID> defendantDetailsChanged, final YouthCourt youthCourt, final LocalDate hearingDay) {

        final Boolean previouslyShared = Boolean.TRUE.equals(momento.getIsHearingDayPreviouslyShared().get(hearingDay));

        final Map<UUID, Target> finalTargets = new HashMap<>();
        final Map<UUID, Target> targets = new HashMap<>();
        final List<NewAmendmentResult> newAmendmentResults = new ArrayList<>();

        final Map<UUID, Target> targetsInAggregate = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : emptyMap();

        resultLines.forEach(
                rl -> {
                    final Target target = createTargetAndPutsToTargetsMap(hearingId, targets, rl, hearingDay);
                    setShadowListed(target, rl.getTargetId(), targetsInAggregate);
                    target.getResultLines().add(convert(rl));

                    if (isNewAmendmentResultOrNewResult(rl)) {
                        newAmendmentResults.add(new NewAmendmentResult(rl.getResultLineId(), rl.getAmendmentDate()));
                    }
                }
        );

        targets.values().forEach(target ->
                setDraftResultAndPutFinalTargetsMap(finalTargets, target, targetsInAggregate));

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        final Hearing hearing = this.momento.getHearing();
        hearing.setYouthCourt(youthCourt);

        final ResultsSharedV2.Builder resultsSharedV2Builder = ResultsSharedV2.builder()
                .withIsReshare(previouslyShared)
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withSharedTime(sharedTime)
                .withCourtClerk(courtClerk)
                .withVariantDirectory(calculateNewVariants(targetsInAggregate))
                .withHearing(hearing)
                .withTargets(new ArrayList<>(finalTargets.values()))
                .withSavedTargets(getSavedTargetsForHearingDay(momento.getMultiDaySavedTargets(), hearingDay))
                .withCompletedResultLinesStatus(getCompletedResultLineStatusForHearingDay(this.momento.getMultiDayCompletedResultLinesStatus(), hearingDay))
                .withNewAmendmentResults(newAmendmentResults);
        if (!defendantDetailsChanged.isEmpty()) {
            resultsSharedV2Builder.withDefendantDetailsChanged(defendantDetailsChanged);
        }
        streamBuilder.add(resultsSharedV2Builder.build());

        if (!this.momento.getNextHearingStartDates().isEmpty()) {
            streamBuilder.add(new EarliestNextHearingDateCleared(hearingId));
        }

        final Stream<Object> streams = Stream.concat(enrichHearingV2(resultLines), streamBuilder.build());
        return Stream.concat(streams, CustodyTimeLimitUtil.stopCTLExpiryForV2(this.momento, resultLines));
    }

    private void setDraftResultAndPutFinalTargetsMap(final Map<UUID, Target> finalTargets, final Target target, final Map<UUID, Target> targetsInAggregate) {
        if (finalTargets.containsKey(target.getTargetId()) && !StringUtils.isEmpty(target.getDraftResult())) {
            target.setDraftResult(targetsInAggregate.get(target.getTargetId()).getDraftResult());
        }
        finalTargets.put(target.getTargetId(), target);
    }

    private Target createTargetAndPutsToTargetsMap(final UUID hearingId, final Map<UUID, Target> targets, final SharedResultsCommandResultLineV2 rl, final LocalDate hearingDay) {
        Target target;
        if (targets.containsKey(rl.getTargetId())) {
            target = targets.get(rl.getTargetId());
        } else {
            target = target()
                    .withApplicationId(rl.getApplicationId())
                    .withDefendantId(rl.getDefendantId())
                    .withHearingId(hearingId)
                    .withOffenceId(rl.getOffenceId())
                    .withResultLines(new ArrayList<>())
                    .withTargetId(rl.getTargetId())
                    .withHearingDay(hearingDay)
                    .build();
            targets.put(target.getTargetId(), target);
        }
        return target;
    }

    /**
     * if it is first shared , amendmentDate is always null.
     * It will return true if it is first shared which means it does not exist in the map
     *   or amendmentDate is changed. 
     *
     * */
    private boolean isNewAmendmentResultOrNewResult(final SharedResultsCommandResultLineV2 rl) {
        final UUID resultLineId = rl.getResultLineId();
        final ZonedDateTime amendedDateTime = rl.getAmendmentDate();

        if (this.momento.getResultsAmendmentDateMap().containsKey(resultLineId)) {
            final ZonedDateTime previousAmendedDateTime = this.momento.getResultsAmendmentDateMap().get(resultLineId);
            if (isNull(previousAmendedDateTime) && nonNull(amendedDateTime)) {
                return true;
            } else if (isNull(previousAmendedDateTime)) {
                return false;
            } else {
                return !previousAmendedDateTime.equals(amendedDateTime);
            }
        } else {
            return true;
        }

    }

    private void setShadowListed(final Target target, final UUID targetId, final Map<UUID, Target> targetsInAggregate) {
        if (targetsInAggregate.containsKey(targetId)) {
            target.setShadowListed(targetsInAggregate.get(targetId).getShadowListed());
        }
    }

    private Stream<Object> enrichHearing(final List<SharedResultsCommandResultLine> resultLines) {
        setSharedResults(resultLines);
        updateCounsels();
        updateCompanyRepresentatives();
        updateOffence();
        return Stream.empty();
    }

    private Stream<Object> enrichHearingV2(final List<SharedResultsCommandResultLineV2> resultLines) {
        setSharedResultsV2(resultLines);
        updateCounsels();
        updateCompanyRepresentatives();
        updateOffence();
        return Stream.empty();
    }

    private void setSharedResults(final List<SharedResultsCommandResultLine> resultLines) {
        this.momento.getHearing().setHasSharedResults(resultLines.stream().filter(SharedResultsCommandResultLine::getIsComplete).count() == resultLines.size());
        this.momento.getHearing().setHasSharedResults(true);
    }

    private void setSharedResultsV2(final List<SharedResultsCommandResultLineV2> resultLines) {
        this.momento.getHearing().setHasSharedResults(resultLines.stream().filter(SharedResultsCommandResultLineV2::getIsComplete).count() == resultLines.size());
        this.momento.getHearing().setHasSharedResults(true);
    }

    private void updateOffence() {
        if (isNotEmpty(this.momento.getHearing().getProsecutionCases())) {
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

    public Stream<Object> updateResultLinesStatus(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines) {
        return Stream.of(ResultLinesStatusUpdated.builder()
                .withHearingId(hearingId)
                .withLastSharedDateTime(lastSharedDateTime)
                .withSharedResultLines(sharedResultLines)
                .withCourtClerk(courtClerk)
                .build());
    }

    public Stream<Object> updateDaysResultLinesStatus(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines, final LocalDate hearingDay) {
        return Stream.of(DaysResultLinesStatusUpdated.builder()
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withLastSharedDateTime(lastSharedDateTime)
                .withSharedResultLines(sharedResultLines)
                .withCourtClerk(courtClerk)
                .build());
    }

    /**
     * This method is to calculate new variants from targets per day or saved targets per day in aggregate.
     *
     * @param targetsInAggregate targets per day or saved targets per day in aggregate.
     *
     * @return
     */
    private List<Variant> calculateNewVariants(final Map<UUID, Target> targetsInAggregate) {

        //We cull variants that have result lines that are no longer present.

        final List<UUID> completedResultLineIds = targetsInAggregate.values().stream()
                .flatMap(t -> t.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .map(ResultLine::getResultLineId)
                .collect(toList());

        return this.momento.getVariantDirectory().stream()
                .filter(variant -> {
                    final List<UUID> resultLineIds = variant.getValue().getResultLines().stream().map(ResultLineReference::getResultLineId).collect(toList());

                    resultLineIds.removeAll(completedResultLineIds);

                    return resultLineIds.isEmpty();
                    //if not empty, it means we have a variant that is based on some completed result lines that are no longer in the incoming set.
                    //The incoming set is an exhaustive set of completed result lines since the UI is the authority on those lines.
                    //If a line is not included, it has been deleted.
                    //Which means we have a variant that is based on a line that has been deleted.
                    //We should delete the variant.
                })
                .collect(toList());
    }

    public boolean hasResultsShared() {
        return momento != null && Boolean.TRUE.equals(momento.isPublished());
    }

    private Map<UUID, CompletedResultLineStatus> getCompletedResultLineStatusForHearingDay(final Map<LocalDate, Map<UUID, CompletedResultLineStatus>> multiDayCompletedResultLinesStatus, final LocalDate hearingDay) {

        return nonNull(multiDayCompletedResultLinesStatus.get(hearingDay)) ? multiDayCompletedResultLinesStatus.get(hearingDay) : emptyMap();

    }

    private List<Target> getSavedTargetsForHearingDay(final Map<LocalDate, Map<UUID, Target>> multiDaySavedTargets, final LocalDate hearingDay) {

        return nonNull(multiDaySavedTargets.get(hearingDay)) ? new ArrayList<>(multiDaySavedTargets.get(hearingDay).values()) : emptyList();

    }

    /**
     * The requirement for DD-3429 is to share results on hearing day basis.
     * As part of this Epic implementation the FE sends hearing day.
     * If the hearing is single day hearing, FE sends the current sitting day as a hearing day.
     * If the hearing is multi day hearing, FE sends the selected day as a hearing day.
     * This hearing day will be serialized as part the new events.
     *
     * For the older events hearing day will be derived from the when the hearing is created to have generic implementation and backward compatibility.
     *
     * @return
     */
    private LocalDate getHearingDay() {
        return this.momento.getHearing().getHearingDays().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hearing Day is not present"))
                .getSittingDay()
                .toLocalDate();
    }

}