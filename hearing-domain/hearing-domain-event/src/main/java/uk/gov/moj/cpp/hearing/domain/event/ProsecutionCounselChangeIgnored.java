package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.prosecution-counsel-change-ignored")
public class ProsecutionCounselChangeIgnored implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;

    private final String reason;

    private final ProsecutionCounsel prosecutionCounsel;

    private final UUID hearingId;

    private final String caseURN;

    public ProsecutionCounselChangeIgnored(final String reason, final ProsecutionCounsel prosecutionCounsel, final UUID hearingId, final String caseURN) {
        this.reason = reason;
        this.prosecutionCounsel = prosecutionCounsel;
        this.hearingId = hearingId;
        this.caseURN = caseURN;
    }

    public String getReason() {
        return reason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ProsecutionCounsel getProsecutionCounsel() {
        return prosecutionCounsel;
    }

    public String getCaseURN() { return caseURN; }
}
