package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class CourtSite implements Serializable {
    private static final long serialVersionUID = -4151921355339340656L;

    private UUID id;

    private List<CourtRoom> courtRooms;

    private String courtSiteName;

    public CourtSite(final List<CourtRoom> courtRooms, final String courtSiteName, final UUID id) {
        this.id = id;
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

    public UUID getId() {
        return id;
    }

    @SuppressWarnings({"squid:S00121", "squid:S00122"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourtSite courtSite = (CourtSite) o;

        return id.equals(courtSite.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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
        private UUID id;

        public Builder withCourtRooms(final List<CourtRoom> courtRooms) {
            this.courtRooms = courtRooms;
            return this;
        }

        public Builder withCourtSiteName(final String courtSiteName) {
            this.courtSiteName = courtSiteName;
            return this;
        }


        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public CourtSite build() {
            return new CourtSite(courtRooms, courtSiteName, id);
        }
    }
}
