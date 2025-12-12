package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.Target;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class SaveMultipleResultsCommand implements Serializable {

    private UUID hearingId;
    private List<Target> targets;

    public SaveMultipleResultsCommand(UUID hearingId, List<Target> targets) {
        this.hearingId = hearingId;
        this.targets = targets;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }


    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }
}
