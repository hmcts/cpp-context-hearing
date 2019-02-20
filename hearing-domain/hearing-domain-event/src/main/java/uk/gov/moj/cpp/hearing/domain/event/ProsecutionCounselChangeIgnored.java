package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("hearing.prosecution-counsel-change-ignored")
public class ProsecutionCounselChangeIgnored implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;
    private final String reason;

    public ProsecutionCounselChangeIgnored(final String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
