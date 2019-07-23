package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("hearing.respondent-counsel-change-ignored")
public class RespondentCounselChangeIgnored implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;
    private final String reason;

    public RespondentCounselChangeIgnored(final String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
