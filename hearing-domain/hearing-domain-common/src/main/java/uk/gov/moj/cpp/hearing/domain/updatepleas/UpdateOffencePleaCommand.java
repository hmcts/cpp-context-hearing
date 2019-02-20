package uk.gov.moj.cpp.hearing.domain.updatepleas;

import uk.gov.justice.core.courts.Plea;

import java.util.UUID;

public class UpdateOffencePleaCommand {

    private UUID hearingId;
    private Plea plea;

    public static UpdateOffencePleaCommand updateOffencePleaCommand() {
        return new UpdateOffencePleaCommand();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UpdateOffencePleaCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public Plea getPlea() {
        return plea;
    }

    public UpdateOffencePleaCommand setPlea(Plea plea) {
        this.plea = plea;
        return this;
    }
}
