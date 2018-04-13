package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.ResultLine;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;
import uk.gov.moj.cpp.hearing.domain.event.ResultsShared;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

@SuppressWarnings("squid:S1068")
public class HearingAggregate implements Aggregate {

    private boolean resultsShared;
    private final Set<UUID> sharedResultIds = new HashSet<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(ResultsShared.class)
                        .apply(resultsSharedResult -> recordSharedResults(resultsSharedResult.getResultLines())),
                when(ResultAmended.class)
                        .apply(this::recordAmendedResult),
                otherwiseDoNothing()
        );
    }



    public Stream<Object> shareResults(final UUID hearingId, final ZonedDateTime sharedTime, final List<ResultLine> resultLines) {
        final LinkedList<Object> events = new LinkedList<>();

        if (this.resultsShared) {
            events.addAll(resultLines.stream()
                    .filter(resultLine -> !this.sharedResultIds.contains(resultLine.getLastSharedResultId())
                            ||
                            !this.sharedResultIds.contains(resultLine.getId()))
                    .map(resultLine -> new ResultAmended(resultLine.getId(), resultLine.getLastSharedResultId(),
                            sharedTime, hearingId, resultLine.getCaseId(), resultLine.getPersonId(), resultLine.getOffenceId(),
                            resultLine.getLevel(), resultLine.getResultLabel(), resultLine.getPrompts(), resultLine.getCourt(), resultLine.getCourtRoom(), resultLine.getClerkOfTheCourtId(), resultLine.getClerkOfTheCourtFirstName(), resultLine.getClerkOfTheCourtLastName())
                    )
                    .collect(toList()));
        } else {
            events.add(new ResultsShared(hearingId, sharedTime, resultLines));
        }

        return apply(events.stream());
    }

    private void recordAmendedResult(final ResultAmended resultAmended) {
        this.sharedResultIds.add(resultAmended.getId());
    }

    private void recordSharedResults(final List<ResultLine> resultLines) {
        this.resultsShared = true;
        this.sharedResultIds.addAll(resultLines.stream().map(ResultLine::getId).collect(toSet()));
    }

}
