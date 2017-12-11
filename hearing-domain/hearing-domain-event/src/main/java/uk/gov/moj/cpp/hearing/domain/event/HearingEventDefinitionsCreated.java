package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.Collections.unmodifiableList;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.util.List;
import java.util.UUID;

@Event("hearing.hearing-event-definitions-created")
public class HearingEventDefinitionsCreated {

    private UUID id;
    private List<HearingEventDefinition> eventDefinitions;

    public HearingEventDefinitionsCreated(final UUID uuid, final List<HearingEventDefinition> eventDefinitions) {
        this.id = uuid;
        this.eventDefinitions = unmodifiableList(eventDefinitions);
    }

    public HearingEventDefinitionsCreated() {
        // default constructor for Jackson serialisation
    }

    public List<HearingEventDefinition> getEventDefinitions() {
        return unmodifiableList(eventDefinitions);
    }

    public UUID getId() {
        return id;
    }

}
