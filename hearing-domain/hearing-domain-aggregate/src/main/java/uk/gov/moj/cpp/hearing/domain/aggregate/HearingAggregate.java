package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;

@SuppressWarnings("squid:S1068")
public class HearingAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;
    
    private final Set<UUID> sharedResultIds = new HashSet<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(ResultAmended.class)
                        .apply(this::recordAmendedResult),
                otherwiseDoNothing()
        );
    }

    private void recordAmendedResult(final ResultAmended resultAmended) {
        this.sharedResultIds.add(resultAmended.getId());
    }
}
