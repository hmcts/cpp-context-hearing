package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.core.courts.Target.target;
import static uk.gov.justice.core.courts.Target2.target2;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.util.CustodyTimeLimitUtil;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingLocked;
import uk.gov.moj.cpp.hearing.domain.event.HearingLockedByOtherUser;
import uk.gov.moj.cpp.hearing.domain.event.result.DaysResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.HearingVacatedRequested;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
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

import javax.json.JsonObject;

@SuppressWarnings({"squid:S3776","squid:S1188", "PMD.BeanMembersShouldSerialize","pmd:NullAssignment"})
public class ResultsSharedDelegate implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String HEARING_VACATED_RESULT_DEFINITION_ID = "8cdc7be1-fc94-485b-83ee-410e710f6665";

    public static final String REASON_FOR_VACATING_TRIAL = "reasonForVacatingTrial";


    private final HearingAggregateMomento momento;

    public ResultsSharedDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleResultsShared(final ResultsShared resultsShared) {
        this.momento.setPublished(true);
        this.momento.setVariantDirectory(resultsShared.getVariantDirectory());
        final LocalDate hearingDay = getHearingDay();
        this.momento.getMultiDaySavedTargets().put(hearingDay, resultsShared.getTargets().stream().filter(Objects::nonNull).map(this::convertToTarget2).collect(Collectors.toMap(Target2::getTargetId, target -> target, (target1, target2) -> target1)));
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
                .map(this::convertToTarget2)
                .collect(Collectors.toMap(Target2::getTargetId, target -> target, (target1, target2) -> target1)));
        recordTargetsSharedAndResetTransientTargets();
        this.momento.getIsHearingDayPreviouslyShared().put(resultsShared.getHearingDay(), true);
        resultsShared.getNewAmendmentResults()
                .forEach(newAmendmentResult -> this.momento.getResultsAmendmentDateMap().put(newAmendmentResult.getId(), newAmendmentResult.getAmendmentDateTime()));
        this.momento.setLastSharedTime(resultsShared.getSharedTime());
        this.momento.getHearing().setYouthCourt(resultsShared.getHearing().getYouthCourt());
    }

    public void handleResultsSharedV3(final ResultsSharedV3 resultsShared) {
        this.momento.setPublished(true);
        this.momento.setVariantDirectory(resultsShared.getVariantDirectory());
        this.momento.getMultiDaySavedTargets().put(resultsShared.getHearingDay(), resultsShared.getTargets().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Target2::getTargetId, target -> target, (target1, target2) -> target1)));
        recordTargetsSharedAndResetTransientTargets();
        this.momento.getIsHearingDayPreviouslyShared().put(resultsShared.getHearingDay(), true);
        resultsShared.getNewAmendmentResults()
                .forEach(newAmendmentResult -> this.momento.getResultsAmendmentDateMap().put(newAmendmentResult.getId(), newAmendmentResult.getAmendmentDateTime()));
        this.momento.setLastSharedTime(resultsShared.getSharedTime());
        this.momento.getHearing().setYouthCourt(resultsShared.getHearing().getYouthCourt());
    }


    private void recordTargetsSharedAndResetTransientTargets() {
        final Collection<Target2> transientValues = this.momento.getTransientTargets().values();
        transientValues.stream().forEach(t -> {
            final Target2 target = this.momento.getSharedTargets().getOrDefault(t.getTargetId(), t);
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

        final Map<UUID, Target2> targetMap = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : new HashMap<>();

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

        final Map<UUID, Target2> targetMap = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : new HashMap<>();

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
        updateTransientTargets(targetId, convertToTarget2(target));

        if (this.momento.getMultiDayTargets().containsKey(hearingDay) && this.momento.getMultiDayTargets().get(hearingDay).containsKey(targetId)) {
            this.momento.getMultiDayTargets().get(hearingDay).get(targetId).setDraftResult(draftResultSaved.getTarget().getDraftResult());
            this.momento.getMultiDayTargets().get(hearingDay).get(targetId).setShadowListed(target.getShadowListed());
        } else {
            draftResultSaved.getTarget().setResultLines(emptyList());
            if (this.momento.getMultiDayTargets().containsKey(hearingDay)) {
                this.momento.getMultiDayTargets().get(hearingDay).put(targetId, convertToTarget2(target));
            } else {
                final Map<UUID, Target2> newTarget = new HashMap<>();
                newTarget.put(targetId, convertToTarget2(target));
                this.momento.getMultiDayTargets().put(hearingDay, newTarget);
            }
        }

    }

    private void updateTransientTargets(final UUID targetId, final Target2 target) {
        this.momento.getTransientTargets().put(targetId, target);
    }

    public Stream<Object> saveDraftResult(final Target target, final HearingState hearingState, final UUID userId) {
        return Stream.of(new DraftResultSaved(target, hearingState, userId));
    }

    public Stream<Object> saveDraftResultV2(final UUID hearingId, LocalDate hearingDay, final JsonObject draftResult, final UUID userId) {
        return Stream.of(new DraftResultSavedV2(hearingId, hearingDay, draftResult, userId));
    }

    public Stream<Object> deleteDraftResultV2(final UUID hearingId, LocalDate hearingDay, final UUID userId) {
        return Stream.of(new DraftResultDeletedV2(hearingId, hearingDay, userId));
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

    @SuppressWarnings("pmd:NullAssignment")
    private ResultLine2 convert3(final SharedResultsCommandResultLineV2 resultLineIn) {
        return ResultLine2.resultLine2()
                .withShortCode(resultLineIn.getShortCode())
                .withResultLineId(resultLineIn.getResultLineId())
                .withIsModified(resultLineIn.getIsModified())
                .withIsComplete(resultLineIn.getIsComplete())
                .withSharedDate(resultLineIn.getSharedDate())
                .withOrderedDate(resultLineIn.getOrderedDate())
                .withApplicationId(resultLineIn.getApplicationId())
                .withOffenceId(resultLineIn.getOffenceId())
                .withCaseId(resultLineIn.getCaseId())
                .withShadowListed(resultLineIn.isShadowListed())
                .withDefendantId(resultLineIn.getDefendantId())
                .withMasterDefendantId(resultLineIn.getMasterDefendantId())
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
                .withAmendmentDate(nonNull(resultLineIn.getAmendmentDate()) ? resultLineIn.getAmendmentDate() : null)
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
        final Map<UUID, Target> targetsInAggregate =
                momento.getMultiDayTargets().containsKey(hearingDay) ? Optional.of(momento.getMultiDayTargets().get(hearingDay)).map(e -> {
                    final Map<UUID, Target> map = new HashMap<>();
                    e.entrySet().stream().map(b -> map.put(b.getKey(), convertToTarget(b.getValue())));
                    return map;
                }).orElse(emptyMap()) : emptyMap();

        final Map<UUID, Target> finalTargets = new HashMap<>();

        resultLines.forEach(
                rl -> {
                    final Target target = target()
                            .withApplicationId(rl.getApplicationId())
                            .withDefendantId(rl.getDefendantId())
                            .withHearingId(hearingId)
                            .withOffenceId(rl.getOffenceId())
                            .withResultLines(new ArrayList<>())
                            .withTargetId(randomUUID())
                            .withShadowListed(rl.isShadowListed())
                            .withDraftResult(rl.getDraftResult())
                            .build();
                    finalTargets.put(target.getTargetId(), target);
                    target.getResultLines().add(convert(rl));
                }
        );

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
        final List<NewAmendmentResult> newAmendmentResults = new ArrayList<>();
        final LocalDate hearingDay = getHearingDay();
        final Map<UUID, Target> targetsInAggregate =
                momento.getMultiDayTargets().containsKey(hearingDay) ? Optional.of(momento.getMultiDayTargets().get(hearingDay)).map(e -> {
                    final Map<UUID, Target> map = new HashMap<>();
                    e.entrySet().stream().map(b -> map.put(b.getKey(), convertToTarget(b.getValue())));
                    return map;
                }).orElse(emptyMap()) : emptyMap();

        resultLines.forEach(
                rl -> {
                    final Target target = createTargetAndPutsToTargetsMap(hearingId, finalTargets, rl, hearingDay);
                    target.getResultLines().add(convert(rl));

                    if (isNewAmendmentResultOrNewResult(rl)) {
                        newAmendmentResults.add(new NewAmendmentResult(rl.getResultLineId(), rl.getAmendmentDate()));
                    }
                }
        );

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

        addParenetResultLineIds(resultLines);
        final Boolean previouslyShared = Boolean.TRUE.equals(momento.getIsHearingDayPreviouslyShared().get(hearingDay));

        final Map<UUID, Target2> finalTargets = new HashMap<>();
        final List<NewAmendmentResult> newAmendmentResults = new ArrayList<>();
        final Map<UUID, Target2> targetsInAggregate = momento.getMultiDayTargets().containsKey(hearingDay) ? momento.getMultiDayTargets().get(hearingDay) : emptyMap();

        resultLines.forEach(
                rl -> {
                    final Target2 target = createTargetAndPutsToTargetsMap3(hearingId, finalTargets, rl, hearingDay);
                    target.getResultLines().add(convert3(rl));

                    if (isNewAmendmentResultOrNewResult(rl)) {
                        newAmendmentResults.add(new NewAmendmentResult(rl.getResultLineId(), rl.getAmendmentDate()));
                    }
                }
        );


        final Stream.Builder<Object> streamBuilder = Stream.builder();

        final Hearing hearing = this.momento.getHearing();
        hearing.setYouthCourt(youthCourt);

        final ResultsSharedV3.Builder resultsSharedV2Builder = ResultsSharedV3.builder()
                .withIsReshare(previouslyShared)
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withSharedTime(sharedTime)
                .withCourtClerk(courtClerk)
                .withVariantDirectory(calculateNewVariants3(targetsInAggregate))
                .withHearing(hearing)
                .withTargets(new ArrayList<>(finalTargets.values()))
                .withSavedTargets(getSavedTargetsForHearingDay3(momento.getMultiDaySavedTargets(), hearingDay))
                .withCompletedResultLinesStatus(getCompletedResultLineStatusForHearingDay(this.momento.getMultiDayCompletedResultLinesStatus(), hearingDay))
                .withNewAmendmentResults(newAmendmentResults);
        if (!defendantDetailsChanged.isEmpty()) {
            resultsSharedV2Builder.withDefendantDetailsChanged(defendantDetailsChanged);
        }


        final ResultsSharedV3 resultsSharedV3 = resultsSharedV2Builder.build();
        streamBuilder.add(resultsSharedV3);
        isHearingVacatedRequired(hearing, resultsSharedV3, streamBuilder);

        if (!this.momento.getNextHearingStartDates().isEmpty()) {
            streamBuilder.add(new EarliestNextHearingDateCleared(hearingId));
        }

        final Stream<Object> streams = Stream.concat(enrichHearingV2(resultLines), streamBuilder.build());
        return Stream.concat(streams, CustodyTimeLimitUtil.stopCTLExpiryForV2(this.momento, resultLines));
    }

    private void isHearingVacatedRequired(final Hearing hearing, final ResultsSharedV3 resultsSharedV3, final Stream.Builder<Object> streamBuilder) {


        resultsSharedV3.getTargets().stream().forEach(target ->
            target.getResultLines().forEach(resultLine -> {
                if (HEARING_VACATED_RESULT_DEFINITION_ID.equals(resultLine.getResultDefinitionId().toString())) {

                        final Optional<UUID> hearingIdToBeVacated  = hearing.getCourtApplications().stream()
                                .filter(courtApplication -> courtApplication.getId().equals(resultLine.getApplicationId()))
                                .filter(courtApplication->nonNull(courtApplication.getHearingIdToBeVacated()))
                                .findFirst()
                                .map(CourtApplication::getHearingIdToBeVacated);



                    final String vacatedTrialReasonShortDesc = resultLine
                            .getPrompts()
                            .stream()
                            .filter(prompt -> prompt.getPromptRef().equals(REASON_FOR_VACATING_TRIAL))
                            .findFirst()
                            .map(Prompt::getValue)
                            .get();
                    if (nonNull(hearingIdToBeVacated) && hearingIdToBeVacated.isPresent() && nonNull(vacatedTrialReasonShortDesc)) {
                        final HearingVacatedRequested hearingVacatedRequested = HearingVacatedRequested.builder()
                                .withHearingIdToBeVacated(hearingIdToBeVacated.get())
                                .withVacatedTrialReasonShortDesc(vacatedTrialReasonShortDesc)
                                .build();
                        streamBuilder.add(hearingVacatedRequested);
                    }

                }
            })
        );
    }

    private void addParenetResultLineIds(final List<SharedResultsCommandResultLineV2> resultLineV2s) {
        final Map<UUID, UUID> childParentMap = new HashMap();
        resultLineV2s.stream().filter(rl -> nonNull(rl.getChildResultLineIds()) && !rl.getChildResultLineIds().isEmpty()).forEach(rl ->
                rl.getChildResultLineIds().stream().forEach(childResultLineId ->
                        childParentMap.put(childResultLineId, rl.getResultLineId())
                )
        );

        resultLineV2s.stream().forEach(rl -> {
            if (childParentMap.containsKey(rl.getResultLineId())) {
                if (rl.getParentResultLineIds() == null) {
                    rl.setParentResultLineIds(new ArrayList());
                }
                rl.getParentResultLineIds().add(childParentMap.get(rl.getResultLineId()));
            }
        });
    }

    private Target createTargetAndPutsToTargetsMap(final UUID hearingId, final Map<UUID, Target> targets, final SharedResultsCommandResultLineV2 rl, final LocalDate hearingDay) {
        Target target;
        final UUID targetId = getTargetId(rl);
        if (targets.containsKey(targetId)) {
            target = targets.get(targetId);
        } else {
            target = target()
                    .withApplicationId(rl.getApplicationId())
                    .withDefendantId(rl.getDefendantId())
                    .withHearingId(hearingId)
                    .withOffenceId(rl.getOffenceId())
                    .withResultLines(new ArrayList<>())
                    .withTargetId(targetId)
                    .withHearingDay(hearingDay)
                    .build();
            targets.put(target.getTargetId(), target);
        }
        return target;
    }

    private Target2 createTargetAndPutsToTargetsMap3(final UUID hearingId, final Map<UUID, Target2> targets, final SharedResultsCommandResultLineV2 rl, final LocalDate hearingDay) {

        final UUID taregetId = getTargetId(rl);
        if (targets.containsKey(taregetId)) {
            return targets.get(taregetId);
        }

        final Target2 target = target2()
                .withApplicationId(rl.getApplicationId())
                .withCaseId(rl.getCaseId())
                .withDefendantId(rl.getDefendantId())
                .withMasterDefendantId(rl.getMasterDefendantId())
                .withHearingId(hearingId)
                .withOffenceId(rl.getOffenceId())
                .withResultLines(new ArrayList<>())
                .withTargetId(taregetId)
                .withHearingDay(hearingDay)
                .withShadowListed(rl.isShadowListed())
                .withDraftResult(rl.getDraftResult())
                .build();

        targets.put(target.getTargetId(), target);
        return target;
    }

    private UUID getTargetId(final SharedResultsCommandResultLineV2 rl) {
        return nonNull(rl.getApplicationId()) ? rl.getApplicationId() : rl.getOffenceId();
    }

    /**
     * if it is first shared , amendmentDate is always null. It will return true if it is first
     * shared which means it does not exist in the map   or amendmentDate is changed. 
     */
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
     * This method is to calculate new variants from targets per day or saved targets per day in
     * aggregate.
     *
     * @param targetsInAggregate targets per day or saved targets per day in aggregate.
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

    /**
     * This method is to calculate new variants from targets per day or saved targets per day in
     * aggregate.
     *
     * @param targetsInAggregate targets per day or saved targets per day in aggregate.
     * @return
     */
    private List<Variant> calculateNewVariants3(final Map<UUID, Target2> targetsInAggregate) {

        //We cull variants that have result lines that are no longer present.

        final List<UUID> completedResultLineIds = targetsInAggregate.values().stream()
                .flatMap(t -> t.getResultLines().stream())
                .filter(ResultLine2::getIsComplete)
                .map(ResultLine2::getResultLineId)
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

    private List<Target> getSavedTargetsForHearingDay(final Map<LocalDate, Map<UUID, Target2>> multiDaySavedTargets, final LocalDate hearingDay) {
        return nonNull(multiDaySavedTargets.get(hearingDay))
                ? new ArrayList<>(
                multiDaySavedTargets.get(hearingDay)
                        .values()
                        .stream()
                        .map(this::convertToTarget)
                        .collect(toList()))
                : emptyList();
    }

    private List<Target2> getSavedTargetsForHearingDay3(final Map<LocalDate, Map<UUID, Target2>> multiDaySavedTargets, final LocalDate hearingDay) {
        return nonNull(multiDaySavedTargets.get(hearingDay)) ? new ArrayList<>(multiDaySavedTargets.get(hearingDay).values()) : emptyList();
    }

    /**
     * The requirement for DD-3429 is to share results on hearing day basis. As part of this Epic
     * implementation the FE sends hearing day. If the hearing is single day hearing, FE sends the
     * current sitting day as a hearing day. If the hearing is multi day hearing, FE sends the
     * selected day as a hearing day. This hearing day will be serialized as part the new events.
     * <p>
     * For the older events hearing day will be derived from the when the hearing is created to have
     * generic implementation and backward compatibility.
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

    public Stream<Object> amendHearing(final UUID hearingId, final UUID userId, final HearingState newHearingState) {
        return Stream.of(new HearingAmended(hearingId, userId, newHearingState));
    }

    public Target2 convertToTarget2(Target target) {
        return Target2
                .target2()
                .withApplicationId(target.getApplicationId())
                .withDefendantId(target.getDefendantId())
                .withDraftResult(target.getDraftResult())
                .withHearingDay(target.getHearingDay())
                .withHearingId(target.getHearingId())
                .withMasterDefendantId(target.getMasterDefendantId())
                .withOffenceId(target.getOffenceId())
                .withShadowListed(target.getShadowListed())
                .withResultLines(nonNull(target.getResultLines()) ? convertToResultLine2List(target.getResultLines()) : emptyList())
                .withReasonsList(target.getReasonsList())
                .withTargetId(target.getTargetId())
                .build();
    }

    private List<ResultLine2> convertToResultLine2List(final List<ResultLine> resultLines) {
        return resultLines
                .stream()
                .map(e -> ResultLine2
                        .resultLine2()
                        .withAmendmentDate(nonNull(e.getAmendmentDate()) ? e.getAmendmentDate().atStartOfDay(ZoneId.systemDefault()) : null)
                        .withAmendmentReason(e.getAmendmentReason())
                        .withAmendmentReasonId(e.getAmendmentReasonId())
                        .withApprovedDate(e.getApprovedDate())
                        .withChildResultLineIds(e.getChildResultLineIds())
                        .withDelegatedPowers(e.getDelegatedPowers())
                        .withFourEyesApproval(e.getFourEyesApproval())
                        .withIsComplete(e.getIsComplete())
                        .withIsDeleted(e.getIsDeleted())
                        .withIsModified(e.getIsModified())
                        .withLevel(e.getLevel())
                        .withOrderedDate(e.getOrderedDate())
                        .withParentResultLineIds(e.getParentResultLineIds())
                        .withPrompts(e.getPrompts())
                        .withResultDefinitionId(e.getResultDefinitionId())
                        .withResultLabel(e.getResultLabel())
                        .withResultLineId(e.getResultLineId())
                        .withSharedDate(e.getSharedDate())
                        .build())
                .collect(toList());
    }

    public Target convertToTarget(final Target2 target) {
        return Target
                .target()
                .withApplicationId(target.getApplicationId())
                .withDefendantId(target.getDefendantId())
                .withDraftResult(target.getDraftResult())
                .withHearingDay(target.getHearingDay())
                .withHearingId(target.getHearingId())
                .withMasterDefendantId(target.getMasterDefendantId())
                .withOffenceId(target.getOffenceId())
                .withShadowListed(target.getShadowListed())
                .withResultLines(nonNull(target.getResultLines()) ? convertToResultLineList(target.getResultLines()) : emptyList())
                .withReasonsList(target.getReasonsList())
                .withTargetId(target.getTargetId())
                .build();
    }

    private List<ResultLine> convertToResultLineList(final List<ResultLine2> resultLines) {
        return resultLines
                .stream()
                .map(e -> ResultLine.resultLine()
                        .withAmendmentDate(nonNull(e.getAmendmentDate()) ? e.getAmendmentDate().toLocalDate() : null)
                        .withAmendmentReason(e.getAmendmentReason())
                        .withAmendmentReasonId(e.getAmendmentReasonId())
                        .withApprovedDate(e.getApprovedDate())
                        .withChildResultLineIds(e.getChildResultLineIds())
                        .withDelegatedPowers(e.getDelegatedPowers())
                        .withFourEyesApproval(e.getFourEyesApproval())
                        .withIsComplete(e.getIsComplete())
                        .withIsDeleted(e.getIsDeleted())
                        .withIsModified(e.getIsModified())
                        .withLevel(e.getLevel())
                        .withOrderedDate(e.getOrderedDate())
                        .withParentResultLineIds(e.getParentResultLineIds())
                        .withPrompts(e.getPrompts())
                        .withResultDefinitionId(e.getResultDefinitionId())
                        .withResultLabel(e.getResultLabel())
                        .withResultLineId(e.getResultLineId())
                        .withSharedDate(e.getSharedDate())
                        .build())
                .collect(toList());
    }
}