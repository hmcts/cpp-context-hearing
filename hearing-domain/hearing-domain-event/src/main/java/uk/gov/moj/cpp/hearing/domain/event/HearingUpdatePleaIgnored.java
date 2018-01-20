package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;

import java.util.UUID;

@Event("hearing.hearing-update-plea-ignored")
public class HearingUpdatePleaIgnored {
    private String reason;

    private UUID caseId;

    private HearingUpdatePleaCommand hearingUpdatePleaCommand;

    public HearingUpdatePleaIgnored(final UUID caseId,final String reason, final HearingUpdatePleaCommand hearingUpdatePleaCommand) {
        this.caseId = caseId;
        this.reason = reason;
        this.hearingUpdatePleaCommand = hearingUpdatePleaCommand;
    }

    public HearingUpdatePleaIgnored() {
        // default constructor for Jackson serialisation
    }

    public String getReason() {
        return reason;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public HearingUpdatePleaCommand getHearingUpdatePleaCommand() {
        return hearingUpdatePleaCommand;
    }
}
