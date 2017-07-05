package uk.gov.moj.cpp.hearing.steps.data;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.util.List;
import java.util.UUID;

public class HearingEventDefinitionData {

    private final UUID id;
    private final List<HearingEventDefinition> eventDefinitions;

    public HearingEventDefinitionData(final UUID id, final List<HearingEventDefinition> eventDefinitions) {
        this.id = id;
        this.eventDefinitions = eventDefinitions;
    }

    public UUID getId() {
        return id;
    }

    public List<HearingEventDefinition> getEventDefinitions() {
        return eventDefinitions;
    }
}
