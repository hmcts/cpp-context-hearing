package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.util.List;

public class AllNows {

    private List<NowDefinition> nows;

    public static AllNows allNows() {
        return new AllNows();
    }

    public List<NowDefinition> getNows() {
        return this.nows;
    }

    public AllNows setNows(List<NowDefinition> nows) {
        this.nows = nows;
        return this;
    }
}
