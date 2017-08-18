package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsDeleted;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingEventDefinitionAggregate implements Aggregate {

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                otherwiseDoNothing()
        );
    }

    public Stream<Object> createEventDefinitions(final UUID hearingEventDefinitionId, final List<HearingEventDefinition> eventDefinitions) {
        return apply(Stream.of(
                new HearingEventDefinitionsDeleted(hearingEventDefinitionId),
                new HearingEventDefinitionsCreated(hearingEventDefinitionId, eventDefinitions)));
    }
}
