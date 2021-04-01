package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

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
