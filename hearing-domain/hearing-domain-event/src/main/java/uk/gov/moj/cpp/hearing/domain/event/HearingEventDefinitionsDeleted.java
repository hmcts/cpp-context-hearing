package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.hearing-event-definitions-deleted")
public class HearingEventDefinitionsDeleted implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    public HearingEventDefinitionsDeleted(final UUID id) {
        this.id = id;
    }

    public HearingEventDefinitionsDeleted() {
        // default constructor for Jackson serialisation
    }

    public UUID getId() {
        return id;
    }
}
