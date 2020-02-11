package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.defence-counsel-change-ignored")
public class DefenceCounselChangeIgnored implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;

    private final String reason;

    private final DefenceCounsel defenceCounsel;

    private final UUID hearingId;

    private final String caseURN;

    public DefenceCounselChangeIgnored(final String reason, final DefenceCounsel defenceCounsel, final UUID hearingId, final String caseURN) {
        this.reason = reason;
        this.defenceCounsel = defenceCounsel;
        this.hearingId = hearingId;
        this.caseURN = caseURN;
    }

    public String getReason() {
        return reason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public DefenceCounsel getDefenceCounsel() {
        return defenceCounsel;
    }

    public String getCaseURN() { return caseURN; }
}
