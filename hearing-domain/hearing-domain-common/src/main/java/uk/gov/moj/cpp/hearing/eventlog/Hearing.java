package uk.gov.moj.cpp.hearing.eventlog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hearing {

    private CourtCentre courtCentre;
    private String hearingType;

    @JsonCreator
    public Hearing(@JsonProperty("courtCentre") final CourtCentre courtCentre,
                   @JsonProperty("hearingType") final String hearingType) {
        this.courtCentre = courtCentre;
        this.hearingType = hearingType;
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

    public static class Builder {

        private CourtCentre.Builder courtCentre;
        private String hearingType;

        public Builder withCourtCentre(CourtCentre.Builder courtCentre) {
            this.courtCentre = courtCentre;
            return this;
        }

        public Builder withHearingType(String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Hearing build() {
            return new Hearing(courtCentre.build(), hearingType);
        }
    }
}
