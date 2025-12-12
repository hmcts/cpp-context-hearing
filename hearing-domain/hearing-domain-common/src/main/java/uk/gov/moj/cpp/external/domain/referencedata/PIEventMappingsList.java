package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.List;

@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
public class PIEventMappingsList {

    private List<PIEventMapping> cpPIHearingEventMappings;

    public PIEventMappingsList(final List<PIEventMapping> cpPIHearingEventMappings) {
        this.cpPIHearingEventMappings = cpPIHearingEventMappings;
    }

    public List<PIEventMapping> getCpPIHearingEventMappings() {
        return cpPIHearingEventMappings;
    }
}
