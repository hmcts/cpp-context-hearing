package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.List;

public class HearingTypeMappingList {

    private List<HearingTypeMapping> hearingTypes;

    public HearingTypeMappingList(final List<HearingTypeMapping> hearingTypes) {
        this.hearingTypes = hearingTypes;
    }

    public List<HearingTypeMapping> getHearingTypes() {
        return hearingTypes;
    }
}
