package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.util.UUID;

public class CrownCourtHearing {
  private String ccHearingDate;

  private UUID courtCentreId;

  private String courtCentreName;

  public CrownCourtHearing() {
  }

  public CrownCourtHearing(final String ccHearingDate, final UUID courtCentreId, final String courtCentreName) {
    this.ccHearingDate = ccHearingDate;
    this.courtCentreId = courtCentreId;
    this.courtCentreName = courtCentreName;
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

  public static Builder crownCourtHearing() {
    return new CrownCourtHearing.Builder();
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

  public static Builder builder(){
    return new Builder();
  }
}
