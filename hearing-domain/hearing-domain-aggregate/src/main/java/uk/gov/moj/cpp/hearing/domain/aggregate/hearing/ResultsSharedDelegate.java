package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.stream.Collectors.toSet;
import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.json.schemas.core.Level;
import uk.gov.justice.json.schemas.core.Prompt;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResultsSharedDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ResultsSharedDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleResultsShared(ResultsShared resultsShared) {
        this.momento.setPublished(true);
        this.momento.setVariantDirectory(resultsShared.getVariantDirectory());
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

        final Set<UUID> resultLineIdsToUpdate = resultLinesStatusUpdated.getSharedResultLines().stream()
                .map(SharedResultLineId::getSharedResultLineId)
                .collect(toSet());

        momento.getTargets().values().stream()
                .peek(target -> {
                    if (target.getResultLines() == null) {
                        target.setResultLines(Collections.emptyList());
                    }
                })
                .flatMap(target -> target.getResultLines().stream())
                .filter(line -> resultLineIdsToUpdate.contains(line.getResultLineId()))
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

    private ResultLine convert(final SharedResultsCommandResultLine resultLineIn) {
        return ResultLine.resultLine()
                .withResultLineId(resultLineIn.getResultLineId())
                .withIsModified(true)
                .withIsComplete(true)
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
                                .withId(pin.getId())
                                .withLabel(pin.getLabel())
                                .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    public Stream<Object> shareResults(final UUID hearingId, final uk.gov.justice.json.schemas.core.CourtClerk courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLine> resultLines) {

        final Map<UUID, Target> targets = new HashMap<>();
        resultLines.forEach(
                rl -> {
                    Target target = null;
                    if (targets.containsKey(rl.getTargetId())) {
                        target = targets.get(rl.getTargetId());
                    } else {
                        target = new Target(rl.getDefendantId(), null, hearingId, rl.getOffenceId(), new ArrayList<>(), rl.getTargetId());
                        targets.put(target.getTargetId(), target);
                    }
                    target.getResultLines().add(convert(rl));
                }
        );

        targets.values().forEach(
                target -> {
                    if (this.momento.getTargets().containsKey(target.getTargetId()) && !StringUtils.isEmpty(target.getDraftResult())) {
                        //retain draft result
                        target.setDraftResult(this.momento.getTargets().get(target.getTargetId()).getDraftResult());
                    }
                    this.momento.getTargets().put(target.getTargetId(), target);
                }
        );

        this.momento.getHearing().setTargets(new ArrayList<>(this.momento.getTargets().values()));


        return Stream.of(ResultsShared.builder()
                .withHearingId(hearingId)
                .withSharedTime(sharedTime)
                .withCourtClerk(courtClerk)
                .withVariantDirectory(calculateNewVariants())

                .withHearing(this.momento.getHearing())
                .withProsecutionCounsels(this.momento.getProsecutionCounsels())
                .withDefenceCounsels(this.momento.getDefenceCounsels())
                .withPleas(this.momento.getPleas())
                .withVerdicts(this.momento.getVerdicts())
                .withCompletedResultLinesStatus(this.momento.getCompletedResultLinesStatus())
                .build());
    }

    public Stream<Object> updateResultLinesStatus(final UUID hearingId, final uk.gov.justice.json.schemas.core.CourtClerk courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines) {
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
