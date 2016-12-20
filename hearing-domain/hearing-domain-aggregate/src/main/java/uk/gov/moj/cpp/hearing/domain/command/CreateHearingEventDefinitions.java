package uk.gov.moj.cpp.hearing.domain.command;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CreateHearingEventDefinitions {

    final private UUID uuid;

    final private List<HearingEventDefinition> eventDefinitions;

    public CreateHearingEventDefinitions(UUID uuid, List<HearingEventDefinition> list) {
        this.uuid = uuid;
        this.eventDefinitions = list;
    }

    public List<HearingEventDefinition> getEventDefinitions() {
        return Collections.unmodifiableList(eventDefinitions);
    }

    public UUID getUUID() {
        return uuid;
    }
}
