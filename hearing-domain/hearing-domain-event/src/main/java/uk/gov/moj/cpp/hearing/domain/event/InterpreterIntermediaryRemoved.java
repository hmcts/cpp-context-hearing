package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.interpreter-intermediary-removed")
public class InterpreterIntermediaryRemoved implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID hearingId;


    public InterpreterIntermediaryRemoved(final UUID id, final UUID hearingId) {
        this.id = id;
        this.hearingId = hearingId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

}
