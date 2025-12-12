package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.interpreter-intermediary-added")
public class InterpreterIntermediaryAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private final InterpreterIntermediary interpreterIntermediary;

    private final UUID hearingId;

    public InterpreterIntermediaryAdded(final InterpreterIntermediary interpreterIntermediary, final UUID hearingId) {
        this.interpreterIntermediary = interpreterIntermediary;
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public InterpreterIntermediary getInterpreterIntermediary() {
        return interpreterIntermediary;
    }
}
