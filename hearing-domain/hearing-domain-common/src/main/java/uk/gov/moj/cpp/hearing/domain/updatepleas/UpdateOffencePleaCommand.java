package uk.gov.moj.cpp.hearing.domain.updatepleas;

import uk.gov.justice.core.courts.PleaModel;

import java.util.UUID;

public class UpdateOffencePleaCommand {

    private UUID hearingId;
    private PleaModel pleaModel;

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

    public PleaModel getPleaModel() {
        return pleaModel;
    }

    public UpdateOffencePleaCommand setPleaModel(final PleaModel pleaModel) {
        this.pleaModel = pleaModel;
        return this;
    }
}
