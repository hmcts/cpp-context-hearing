package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

public class AllNows {

    private java.util.List<Now> nows;

    public static AllNows allNows() {
        return new AllNows();
    }

    public java.util.List<Now> getNows() {
        return this.nows;
    }

    public AllNows setNows(java.util.List<Now> nows) {
        this.nows = nows;
        return this;
    }
}
