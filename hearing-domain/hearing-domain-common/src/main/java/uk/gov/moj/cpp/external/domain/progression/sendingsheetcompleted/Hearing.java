package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Hearing {
  private final UUID caseId;

  private final String caseUrn;

  private final String courtCentreId;

  private final String courtCentreName;

  private final List<Defendant> defendants;

  private final LocalDate sendingCommittalDate;

  private final String type;

  public Hearing(final UUID caseId, final String caseUrn, final String courtCentreId, final String courtCentreName, final List<Defendant> defendants, final LocalDate sendingCommittalDate, final String type) {
    this.caseId = caseId;
    this.caseUrn = caseUrn;
    this.courtCentreId = courtCentreId;
    this.courtCentreName = courtCentreName;
    this.defendants = defendants;
    this.sendingCommittalDate = sendingCommittalDate;
    this.type = type;
  }

  public UUID getCaseId() {
    return caseId;
  }

  public String getCaseUrn() {
    return caseUrn;
  }

  public String getCourtCentreId() {
    return courtCentreId;
  }

  public String getCourtCentreName() {
    return courtCentreName;
  }

  public List<Defendant> getDefendants() {
    return defendants;
  }

  public LocalDate getSendingCommittalDate() {
    return sendingCommittalDate;
  }

  public String getType() {
    return type;
  }

  public static Builder hearing() {
    return new Hearing.Builder();
  }

  public static class Builder {
    private UUID caseId;

    private String caseUrn;

    private String courtCentreId;

    private String courtCentreName;

    private List<Defendant> defendants;

    private LocalDate sendingCommittalDate;

    private String type;

    public Builder withCaseId(final UUID caseId) {
      this.caseId = caseId;
      return this;
    }

    public Builder withCaseUrn(final String caseUrn) {
      this.caseUrn = caseUrn;
      return this;
    }

    public Builder withCourtCentreId(final String courtCentreId) {
      this.courtCentreId = courtCentreId;
      return this;
    }

    public Builder withCourtCentreName(final String courtCentreName) {
      this.courtCentreName = courtCentreName;
      return this;
    }

    public Builder withDefendants(final List<Defendant> defendants) {
      this.defendants = defendants;
      return this;
    }

    public Builder withSendingCommittalDate(final LocalDate sendingCommittalDate) {
      this.sendingCommittalDate = sendingCommittalDate;
      return this;
    }

    public Builder withType(final String type) {
      this.type = type;
      return this;
    }

    public Hearing build() {
      return new Hearing(caseId, caseUrn, courtCentreId, courtCentreName, defendants, sendingCommittalDate, type);
    }
  }
}
