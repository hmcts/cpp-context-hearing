package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;
import java.util.List;

public class Court implements Serializable {
    private static final long serialVersionUID = -4151921355339340656L;

    private String courtName;

    private List<CourtSite> courtSites;

    public Court(final String courtName, final List<CourtSite> courtSites) {
        this.courtName = courtName;
        this.courtSites = courtSites;
    }

    public String getCourtName() {
        return courtName;
    }

    public List<CourtSite> getCourtSites() {
        return courtSites;
    }

    public static Builder court() {
        return new Court.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        final Court that = (Court) obj;

        return java.util.Objects.equals(this.courtName, that.courtName) &&
                java.util.Objects.equals(this.courtSites, that.courtSites);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtName, courtSites);
    }

    @Override
    public String toString() {
        return "Court{" +
                "courtName='" + courtName + "'," +
                "courtSites='" + courtSites + "'" +
                "}";
    }

    public Court setCourtName(String courtName) {
        this.courtName = courtName;
        return this;
    }

    public Court setCourtSites(List<CourtSite> courtSites) {
        this.courtSites = courtSites;
        return this;
    }

    public static class Builder {
        private String courtName;

        private List<CourtSite> courtSites;

        public Builder withCourtName(final String courtName) {
            this.courtName = courtName;
            return this;
        }

        public Builder withCourtSites(final List<CourtSite> courtSites) {
            this.courtSites = courtSites;
            return this;
        }

        public Court build() {
            return new Court(courtName, courtSites);
        }
    }
}
