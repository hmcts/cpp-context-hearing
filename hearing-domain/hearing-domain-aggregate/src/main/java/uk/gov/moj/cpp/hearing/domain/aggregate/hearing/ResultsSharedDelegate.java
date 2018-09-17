package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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
        resultLinesStatusUpdated.getSharedResultLines().forEach(sharedResultLineId ->

                this.momento.getCompletedResultLinesStatus().computeIfAbsent(sharedResultLineId.getSharedResultLineId(), resultLineId ->
                        CompletedResultLineStatus.builder()
                                .withId(resultLineId)
                                .build()
                )
                        .setCourtClerk(resultLinesStatusUpdated.getCourtClerk())
                        .setLastSharedDateTime(resultLinesStatusUpdated.getLastSharedDateTime())
        );
    }

    public void handleDraftResultShared(final DraftResultSaved draftResultSaved) {
        this.momento.getTargets().put(draftResultSaved.getTarget().getTargetId(), draftResultSaved.getTarget());
        this.momento.getHearing().setTargets(new ArrayList<>(this.momento.getTargets().values()));


        momento.getTargets().values().stream()
                .flatMap(target -> target.getResultLines().stream())
                .forEach(resultLine -> {
                    if (resultLine.getIsModified() && this.momento.getCompletedResultLinesStatus().containsKey(resultLine.getResultLineId())){
                        momento.getCompletedResultLinesStatus().get(resultLine.getResultLineId())
                                .setLastSharedDateTime(null)
                                .setCourtClerk(null);
                    }
                });
    }

    public Stream<Object> saveDraftResult(final Target target) {
        return Stream.of(new DraftResultSaved(target));
    }

    public Stream<Object> shareResults(final ShareResultsCommand command, final ZonedDateTime sharedTime) {

        return Stream.of(ResultsShared.builder()
                .withHearingId(command.getHearingId())
                .withSharedTime(sharedTime)
                .withCourtClerk(command.getCourtClerk())
                .withVariantDirectory(calculateNewVariants())

                .withHearing(this.momento.getHearing())
                .withProsecutionCounsels(this.momento.getProsecutionCounsels())
                .withDefenceCounsels(this.momento.getDefenceCounsels())
                .withPleas(this.momento.getPleas())
                .withVerdicts(this.momento.getVerdicts())
                .withCompletedResultLinesStatus(this.momento.getCompletedResultLinesStatus())
                .build());
    }

    public Stream<Object> updateResultLinesStatus(final UpdateResultLinesStatusCommand command) {
        return Stream.of(ResultLinesStatusUpdated.builder()
                .withHearingId(command.getHearingId())
                .withLastSharedDateTime(command.getLastSharedDateTime())
                .withSharedResultLines(command.getSharedResultLines())
                .withCourtClerk(command.getCourtClerk())
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
