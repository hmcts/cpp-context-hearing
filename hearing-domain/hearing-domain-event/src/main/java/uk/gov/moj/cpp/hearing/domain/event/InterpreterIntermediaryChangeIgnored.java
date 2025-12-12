package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("hearing.interpreter-intermediary-change-ignored")
public class InterpreterIntermediaryChangeIgnored implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;
    private final String reason;

    public InterpreterIntermediaryChangeIgnored(final String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
