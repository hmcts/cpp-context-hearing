package uk.gov.moj.cpp.hearing.event.message.eventlog;

public class Hearing {

    private CourtCentre courtCentre;
    private String hearingType;


    public Hearing(CourtCentre courtCentre, String hearingType) {
        this.courtCentre = courtCentre;
        this.hearingType = hearingType;
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
    public static Builder builder() {
        return new Builder();
    }
}
