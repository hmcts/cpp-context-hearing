package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.util.List;

@SuppressWarnings({"squid:S1700"})
public class CrackedIneffectiveVacatedTrialTypes {
    private List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes;

    public static CrackedIneffectiveVacatedTrialTypes allCrackedIneffectiveVacatedTrialTypes() {
        return new CrackedIneffectiveVacatedTrialTypes();
    }

    public List<CrackedIneffectiveVacatedTrialType> getCrackedIneffectiveVacatedTrialTypes() {
        return this.crackedIneffectiveVacatedTrialTypes;
    }

    public CrackedIneffectiveVacatedTrialTypes setCrackedIneffectiveVacatedTrialTypes(List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes) {
        this.crackedIneffectiveVacatedTrialTypes = crackedIneffectiveVacatedTrialTypes;
        return this;
    }
}
