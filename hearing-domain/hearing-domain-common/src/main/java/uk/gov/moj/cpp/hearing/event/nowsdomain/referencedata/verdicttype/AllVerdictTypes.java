package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.verdicttype;

import uk.gov.justice.core.courts.VerdictType;

import java.util.List;

@SuppressWarnings({"squid:S2384"})
public class AllVerdictTypes {

    private List<VerdictType> verdictTypes;

    public List<VerdictType> getVerdictTypes() {
        return verdictTypes;
    }

    public void setVerdictTypes(final List<VerdictType> verdictTypes) {
        this.verdictTypes = verdictTypes;
    }
}
