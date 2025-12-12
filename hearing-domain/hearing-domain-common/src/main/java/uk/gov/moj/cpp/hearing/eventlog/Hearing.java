package uk.gov.moj.cpp.hearing.eventlog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hearing {

    private CourtCentre courtCentre;
    private String hearingType;
    private String jurisdictionType;

    @JsonCreator
    public Hearing(@JsonProperty("courtCentre") final CourtCentre courtCentre,
                   @JsonProperty("hearingType") final String hearingType,
                   @JsonProperty("jurisdictionType") final String jurisdictionType) {
        this.courtCentre = courtCentre;
        this.hearingType = hearingType;
        this.jurisdictionType = jurisdictionType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public String getHearingType() {
        return hearingType;
    }

    public String getJurisdictionType() { return jurisdictionType; }

    public static class Builder {

        private CourtCentre.Builder courtCentre;
        private String hearingType;
        private String jurisdictionType;

        public Builder withCourtCentre(CourtCentre.Builder courtCentre) {
            this.courtCentre = courtCentre;
            return this;
        }

        public Builder withHearingType(String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Builder withJurisdictionType(String jurisdictionType) {
            this.jurisdictionType = jurisdictionType;
            return this;
        }

        public Hearing build() {
            return new Hearing(courtCentre.build(), hearingType, jurisdictionType);
        }
    }
}
