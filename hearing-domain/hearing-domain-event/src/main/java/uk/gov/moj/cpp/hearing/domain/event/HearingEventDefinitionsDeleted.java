package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.hearing-event-definitions-deleted")
public class HearingEventDefinitionsDeleted {

    private final UUID id;

    public HearingEventDefinitionsDeleted(final UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
