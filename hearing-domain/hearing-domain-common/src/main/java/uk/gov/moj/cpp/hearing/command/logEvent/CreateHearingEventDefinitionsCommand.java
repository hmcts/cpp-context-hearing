package uk.gov.moj.cpp.hearing.command.logEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CreateHearingEventDefinitionsCommand implements Serializable {

    private static final long serialVersionUID = -1L;

    private final List<HearingEventDefinition> eventDefinitions;
    private final UUID id;

    @JsonCreator
    public CreateHearingEventDefinitionsCommand(@JsonProperty("id") final UUID id,
                                                @JsonProperty("eventDefinitions") final List<HearingEventDefinition> eventDefinitions) {
        this.eventDefinitions = eventDefinitions;
        this.id = id;
    }

    public List<HearingEventDefinition> getEventDefinitions() {
        return eventDefinitions;
    }

    public UUID getId() {
        return id;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CreateHearingEventDefinitionsCommand that = (CreateHearingEventDefinitionsCommand) obj;

        return Objects.equals(this.eventDefinitions, that.eventDefinitions)
                && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventDefinitions, id);
    }

    @Override
    public String toString() {
        return "CreateHearingEventDefinitionsCommand{" + "eventDefinitions='" + eventDefinitions + "'," + "id='" + id
                + "'" + "}";
    }

    public static class Builder {

        private List<HearingEventDefinition> eventDefinitions;
        private UUID id;

        public Builder withEventDefinitions(final List<HearingEventDefinition> eventDefinitions) {
            this.eventDefinitions = eventDefinitions;
            return this;
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public CreateHearingEventDefinitionsCommand build() {
            return new CreateHearingEventDefinitionsCommand(id, eventDefinitions);
        }
    }
}
