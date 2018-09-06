package uk.gov.moj.cpp.hearing.domain.updatepleas;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class UpdatePleaCommand implements Serializable {

    private List<Plea> pleas;
    private UUID hearingId;

    public List<Plea> getPleas() {
        return this.pleas;
    }

    public UpdatePleaCommand setPleas(List<Plea> pleas) {
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

    public static UpdatePleaCommand updatePleaCommand() {
        return new UpdatePleaCommand();
    }
}
