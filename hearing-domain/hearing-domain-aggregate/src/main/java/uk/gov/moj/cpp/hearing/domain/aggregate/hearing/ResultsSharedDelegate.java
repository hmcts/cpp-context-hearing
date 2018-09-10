package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.json.schemas.core.Target;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
public class ResultsSharedDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ResultsSharedDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleResultsShared(ResultsShared resultsShared) {
        this.momento.setPublished(true);

        resultsShared.getCompletedResultLines().forEach(completedResultLine -> {

            boolean resultLineHasBeenModified = this.momento.getCompletedResultLines().containsKey(completedResultLine.getId())
                    && !completedResultLine.equals(this.momento.getCompletedResultLines().get(completedResultLine.getId()));

            if (resultLineHasBeenModified) {
                ofNullable(this.momento.getCompletedResultLinesStatus().get(completedResultLine.getId()))
                        .ifPresent(status -> status
                                .setLastSharedDateTime(null)
                                .setCourtClerk(null));
            }
        });

        this.momento.setCompletedResultLines(resultsShared.getCompletedResultLines().stream().collect(toMap(CompletedResultLine::getId, Function.identity())));

        this.momento.setVariantDirectory(resultsShared.getVariantDirectory()); //variants might be deleted if their result lines have been deleted.
    }

    public void handleResultLinesStatusUpdated(ResultLinesStatusUpdated resultLinesStatusUpdated) {
        resultLinesStatusUpdated.getSharedResultLines().forEach(sharedResultLineId ->

                this.momento.getCompletedResultLinesStatus().computeIfAbsent(sharedResultLineId.getSharedResultLineId(), resultLineId ->
                        CompletedResultLineStatus.builder()
                                .withId(resultLineId)
                                .build()
                )
                        //TODO GPE 5480 reinstate this line
                        //.setCourtClerk(resultLinesStatusUpdated.getCourtClerk())
                        .setLastSharedDateTime(resultLinesStatusUpdated.getLastSharedDateTime())
         );
    }

    public void handleDraftResultShared(final DraftResultSaved draftResultSaved ) {
        this.momento.getTargets().put(draftResultSaved.getTarget().getTargetId(), draftResultSaved.getTarget());
    }

    public Stream<Object> saveDraftResult(final Target target) {
           return Stream.of(new DraftResultSaved(target));
    }

    public Stream<Object> shareResults(final ShareResultsCommand command, final ZonedDateTime sharedTime) {
        List<UUID> completedResultLineIds = this.momento.getTargets().values().stream()
                .flatMap(t->t.getResultLines().stream())
                .filter(rl->rl.getIsComplete())
                .map(rl->rl.getResultLineId())
                .collect(Collectors.toList());

//        List<UUID> completedResultLineIds = command.getCompletedResultLines().stream().map(CompletedResultLine::getId).collect(Collectors.toList());
        final List<Variant> variants = this.momento.getVariantDirectory().stream()
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

        return Stream.of(ResultsShared.builder()
                .withHearingId(command.getHearingId())
                .withSharedTime(sharedTime)
                .withCourtClerk(command.getCourtClerk())
                //TODO GPE-5480 address this commented out code
                //.withUncompletedResultLines(command.getUncompletedResultLines())
                //.withCompletedResultLines(command.getCompletedResultLines())
//                .withHearing(this.momento.getHearing())
//                .withCases(this.momento.getCases())
                .withProsecutionCounsels(this.momento.getProsecutionCounsels())
                .withDefenceCounsels(this.momento.getDefenceCounsels())
                .withPleas(this.momento.getPleas())
                .withVerdicts(this.momento.getVerdicts())
                .withVariantDirectory(variants)
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
}
