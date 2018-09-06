package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;

import java.util.stream.Stream;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsDeleted;

public class HearingEventDefinitionAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                otherwiseDoNothing()
        );
    }

    public Stream<Object> createEventDefinitions(final CreateHearingEventDefinitionsCommand createHearingEventDefinitionsCommand) {
        return apply(Stream.of(
                new HearingEventDefinitionsDeleted(createHearingEventDefinitionsCommand.getId()),
                new HearingEventDefinitionsCreated(createHearingEventDefinitionsCommand.getId(), createHearingEventDefinitionsCommand.getEventDefinitions())));
    }
}
