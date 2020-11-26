package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Target;

import java.util.List;

@Event("hearing.multiple-draft-results-saved")
public class MultipleDraftResultsSaved {
    private int numberOfTargets;

    public MultipleDraftResultsSaved(int numberOfTargets) {
        this.numberOfTargets = numberOfTargets;
    }

    public int getNumberOfTargets() {
        return numberOfTargets;
    }

    public void setNumberOfTargets(int numberOfTargets) {
        this.numberOfTargets = numberOfTargets;
    }
}
