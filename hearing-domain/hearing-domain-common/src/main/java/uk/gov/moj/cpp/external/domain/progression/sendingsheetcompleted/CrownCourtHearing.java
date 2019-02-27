package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CrownCourtHearing implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String ccHearingDate;

    private final UUID courtCentreId;

    private final String courtCentreName;

    @JsonCreator
    public CrownCourtHearing(@JsonProperty("ccHearingDate") final String ccHearingDate,
                             @JsonProperty("courtCentreId") final UUID courtCentreId,
                             @JsonProperty("courtCentreName") final String courtCentreName) {
        this.ccHearingDate = ccHearingDate;
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
    }

    public static Builder crownCourtHearing() {
        return new CrownCourtHearing.Builder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCcHearingDate() {
        return ccHearingDate;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public static class Builder {
        private String ccHearingDate;

        private UUID courtCentreId;

        private String courtCentreName;

        public Builder withCcHearingDate(final String ccHearingDate) {
            this.ccHearingDate = ccHearingDate;
            return this;
        }

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtCentreName(final String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public CrownCourtHearing build() {
            return new CrownCourtHearing(ccHearingDate, courtCentreId, courtCentreName);
        }
    }
}
