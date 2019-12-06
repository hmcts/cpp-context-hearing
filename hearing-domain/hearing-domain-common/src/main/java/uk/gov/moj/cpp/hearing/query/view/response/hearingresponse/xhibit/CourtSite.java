package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;
import java.util.List;

public class CourtSite implements Serializable {
    private static final long serialVersionUID = -4151921355339340656L;

    private List<CourtRoom> courtRooms;

    private String courtSiteName;

    public CourtSite(final List<CourtRoom> courtRooms, final String courtSiteName) {
        this.courtRooms = courtRooms;
        this.courtSiteName = courtSiteName;
    }

    public List<CourtRoom> getCourtRooms() {
        return courtRooms;
    }

    public String getCourtSiteName() {
        return courtSiteName;
    }

    public static Builder courtSite() {
        return new CourtSite.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        final CourtSite that = (CourtSite) obj;

        return java.util.Objects.equals(this.courtRooms, that.courtRooms) &&
                java.util.Objects.equals(this.courtSiteName, that.courtSiteName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtRooms, courtSiteName);
    }

    @Override
    public String toString() {
        return "CourtSite{" +
                "courtRooms='" + courtRooms + "'," +
                "courtSiteName='" + courtSiteName + "'" +
                "}";
    }

    public CourtSite setCourtRooms(List<CourtRoom> courtRooms) {
        this.courtRooms = courtRooms;
        return this;
    }

    public CourtSite setCourtSiteName(String courtSiteName) {
        this.courtSiteName = courtSiteName;
        return this;
    }

    public static class Builder {
        private List<CourtRoom> courtRooms;

        private String courtSiteName;

        public Builder withCourtRooms(final List<CourtRoom> courtRooms) {
            this.courtRooms = courtRooms;
            return this;
        }

        public Builder withCourtSiteName(final String courtSiteName) {
            this.courtSiteName = courtSiteName;
            return this;
        }

        public CourtSite build() {
            return new CourtSite(courtRooms, courtSiteName);
        }
    }
}
