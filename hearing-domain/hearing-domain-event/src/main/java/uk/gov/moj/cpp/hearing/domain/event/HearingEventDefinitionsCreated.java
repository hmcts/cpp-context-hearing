package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.Collections.unmodifiableList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.hearing-event-definitions-created")
public class HearingEventDefinitionsCreated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private List<HearingEventDefinition> eventDefinitions;

    @JsonCreator
    public HearingEventDefinitionsCreated(
            @JsonProperty("uuid") final UUID uuid,
            @JsonProperty("eventDefinitions") final List<HearingEventDefinition> eventDefinitions) {
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
