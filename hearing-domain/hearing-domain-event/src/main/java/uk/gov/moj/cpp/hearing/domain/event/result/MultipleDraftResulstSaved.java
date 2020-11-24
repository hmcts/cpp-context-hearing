package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Target;

import java.util.List;

@Event("hearing.multiple-draft-results-saved")
public class MultipleDraftResulstSaved {
    private List<Target> targets;

    public MultipleDraftResulstSaved(List<Target> targets) {
        this.targets = targets;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }
}
