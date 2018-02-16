package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;

import java.util.UUID;

@Event("hearing.hearing-update-verdict-ignored")
public class HearingUpdateVerdictIgnored {
    private String reason;

    private UUID hearingId;

    private HearingUpdateVerdictCommand hearingUpdateVerdictCommand;

    public HearingUpdateVerdictIgnored(final UUID hearingId, final String reason, final HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {
        this.hearingId = hearingId;
        this.reason = reason;
        this.hearingUpdateVerdictCommand = hearingUpdateVerdictCommand;
    }

    public HearingUpdateVerdictIgnored() {
        // default constructor for Jackson serialisation
    }

    public String getReason() {
        return reason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingUpdateVerdictCommand getHearingUpdateVerdictCommand() {
        return hearingUpdateVerdictCommand;
    }
}
