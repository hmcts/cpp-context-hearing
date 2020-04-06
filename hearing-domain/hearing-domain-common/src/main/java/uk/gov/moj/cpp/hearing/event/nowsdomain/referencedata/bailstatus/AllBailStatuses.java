package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus;

import java.util.List;

@SuppressWarnings({"squid:S2384"})
public class AllBailStatuses {

    private List<BailStatus> bailStatuses;

    public List<BailStatus> getBailStatuses() {
        return bailStatuses;
    }

    public void setBailStatuses(final List<BailStatus> bailStatuses) {
        this.bailStatuses = bailStatuses;
    }
}
