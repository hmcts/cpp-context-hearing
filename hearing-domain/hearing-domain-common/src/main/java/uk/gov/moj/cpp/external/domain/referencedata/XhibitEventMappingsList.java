package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.List;

public class XhibitEventMappingsList {

    private List<XhibitEventMapping> cpXhibitHearingEventMappings;

    public XhibitEventMappingsList(final List<XhibitEventMapping> cpXhibitHearingEventMappings) {
        this.cpXhibitHearingEventMappings = cpXhibitHearingEventMappings;
    }

    public List<XhibitEventMapping> getCpXhibitHearingEventMappings() {
        return cpXhibitHearingEventMappings;
    }
}
