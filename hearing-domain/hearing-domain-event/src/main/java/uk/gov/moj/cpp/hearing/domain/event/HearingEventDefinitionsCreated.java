package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.Collections.unmodifiableList;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.hearing-event-definitions-created")
public class HearingEventDefinitionsCreated {

    private final UUID id;
    private final List<HearingEventDefinition> eventDefinitions;

    public HearingEventDefinitionsCreated(final UUID uuid, final List<HearingEventDefinition> eventDefinitions) {
        this.id = uuid;
        this.eventDefinitions = eventDefinitions;
    }

    public List<HearingEventDefinition> getEventDefinitions() {
        return unmodifiableList(eventDefinitions);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEventDefinitionsCreated that = (HearingEventDefinitionsCreated) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getEventDefinitions(), that.getEventDefinitions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEventDefinitions());
    }

    @Override
    public String toString() {
        return "HearingEventDefinitionsCreated{" +
                "id=" + id +
                ", eventDefinitions=" + eventDefinitions +
                '}';
    }
}
