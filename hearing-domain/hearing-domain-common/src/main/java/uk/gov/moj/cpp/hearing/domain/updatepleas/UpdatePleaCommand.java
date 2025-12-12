package uk.gov.moj.cpp.hearing.domain.updatepleas;

import uk.gov.justice.core.courts.PleaModel;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class UpdatePleaCommand implements Serializable {

    private List<PleaModel> pleas;

    private UUID hearingId;

    public static UpdatePleaCommand updatePleaCommand() {
        return new UpdatePleaCommand();
    }

    public List<PleaModel> getPleas() {
        return this.pleas;
    }

    public UpdatePleaCommand setPleas(List<PleaModel> pleas) {
        this.pleas = pleas;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UpdatePleaCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }
}