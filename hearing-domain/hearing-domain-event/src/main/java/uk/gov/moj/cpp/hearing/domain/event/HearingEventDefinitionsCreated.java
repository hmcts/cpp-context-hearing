package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Event("hearing.hearing-event-definitions-created")
public class HearingEventDefinitionsCreated {

    final private UUID id;

    final private List<HearingEventDefinition> eventDefinitions;

    public HearingEventDefinitionsCreated(UUID uuid, List<HearingEventDefinition> list) {
        this.id = uuid;
        this.eventDefinitions = list;
    }

    public List<HearingEventDefinition> getEventDefinitions() {
        return Collections.unmodifiableList(eventDefinitions);
    }

    public UUID getId() {
        return id;
    }
}
