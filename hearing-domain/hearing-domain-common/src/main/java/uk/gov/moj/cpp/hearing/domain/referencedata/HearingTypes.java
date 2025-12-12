package uk.gov.moj.cpp.hearing.domain.referencedata;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;

import java.util.List;
@SuppressWarnings({"squid:S1700","squid:S2384"})
public class HearingTypes {

    private List<HearingType> hearingTypes;
    public static CrackedIneffectiveVacatedTrialTypes allHearingTypes() {
        return new CrackedIneffectiveVacatedTrialTypes();
    }

    public List<HearingType> getHearingTypes() {
        return hearingTypes;
    }

    public void setHearingTypes(final List<HearingType> hearingTypes) {
        this.hearingTypes = hearingTypes;
    }

}
