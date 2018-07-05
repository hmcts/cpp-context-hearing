package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class ResultsSharedDelegate {

    private final HearingAggregateMomento momento;

    public ResultsSharedDelegate(final HearingAggregateMomento momento){
        this.momento = momento;
    }

    public void handleResultsShared(ResultsShared resultsShared){
        this.momento.setPublished(true);
        resultsShared.getCompletedResultLines().forEach(completedResultLine -> {
            //only update result line status if resultline is modified or resultline is new
            if (!(this.momento.getCompletedResultLines().containsKey(completedResultLine.getId()) && completedResultLine.equals(this.momento.getCompletedResultLines().get(completedResultLine.getId())))) {
                this.momento.getCompletedResultLinesStatus().put(completedResultLine.getId(), CompletedResultLineStatus.builder().withId(completedResultLine.getId()).build());
            }
            this.momento.getCompletedResultLines().put(completedResultLine.getId(), completedResultLine);
        });
    }

    public void handleResultLinesStatusUpdated(ResultLinesStatusUpdated resultLinesStatusUpdated){
        resultLinesStatusUpdated.getSharedResultLines().forEach(sharedResultLineId ->
            this.momento.getCompletedResultLinesStatus().computeIfPresent(sharedResultLineId.getSharedResultLineId(), (k, sl) -> {
                sl.setCourtClerk(resultLinesStatusUpdated.getCourtClerk());
                sl.setLastSharedDateTime(resultLinesStatusUpdated.getLastSharedDateTime());
                return sl;
            })
        );
    }

    public Stream<Object> shareResults(final ShareResultsCommand command, final ZonedDateTime sharedTime) {
        return Stream.of(ResultsShared.builder()
                .withHearingId(command.getHearingId())
                .withSharedTime(sharedTime)
                .withCourtClerk(command.getCourtClerk())
                .withUncompletedResultLines(command.getUncompletedResultLines())
                .withCompletedResultLines(command.getCompletedResultLines())
                .withHearing(this.momento.getHearing())
                .withCases(this.momento.getCases())
                .withProsecutionCounsels(this.momento.getProsecutionCounsels())
                .withDefenceCounsels(this.momento.getDefenceCounsels())
                .withPleas(this.momento.getPleas())
                .withVerdicts(this.momento.getVerdicts())
                .withVariantDirectory(new ArrayList<>(this.momento.getVariantDirectory().values()))
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
