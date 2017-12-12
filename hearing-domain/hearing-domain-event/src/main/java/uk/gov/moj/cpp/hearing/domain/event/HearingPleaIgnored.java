package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;

@Event("hearing.hearing-update-plea-ignored")
public class HearingPleaIgnored {
    private String reason;
    private HearingUpdatePleaCommand hearingUpdatePleaCommand;

    public HearingPleaIgnored(final String reason, final HearingUpdatePleaCommand hearingUpdatePleaCommand) {
        this.reason = reason;
        this.hearingUpdatePleaCommand = hearingUpdatePleaCommand;
    }

    public HearingPleaIgnored() {
        // default constructor for Jackson serialisation
    }

    public String getReason() {
        return reason;
    }

    public HearingUpdatePleaCommand getHearingUpdatePleaCommand() {
        return hearingUpdatePleaCommand;
    }
}
