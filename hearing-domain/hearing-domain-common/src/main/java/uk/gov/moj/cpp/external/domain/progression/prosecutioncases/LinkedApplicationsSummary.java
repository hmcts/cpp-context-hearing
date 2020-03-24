package uk.gov.moj.cpp.external.domain.progression.prosecutioncases;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LinkedApplicationsSummary implements Serializable {
  private static final long serialVersionUID = -3147513000721146739L;

  private final String applicantDisplayName;

  private final UUID applicationId;

  private final String applicationReference;

  private final String applicationStatus;

  private final String applicationTitle;

  private final Boolean isAppeal;

  private final List<String> respondentDisplayNames;

  @JsonCreator
  public LinkedApplicationsSummary(@JsonProperty(value = "applicantDisplayName") final String applicantDisplayName,
                                   @JsonProperty(value = "applicationId") final UUID applicationId,
                                   @JsonProperty(value = "applicationReference") final String applicationReference,
                                   @JsonProperty(value = "applicationStatus") final String applicationStatus,
                                   @JsonProperty(value = "applicationTitle") final String applicationTitle,
                                   @JsonProperty(value = "isAppeal") final Boolean isAppeal,
                                   @JsonProperty(value = "respondentDisplayNames") final List<String> respondentDisplayNames) {
    this.applicantDisplayName = applicantDisplayName;
    this.applicationId = applicationId;
    this.applicationReference = applicationReference;
    this.applicationStatus = applicationStatus;
    this.applicationTitle = applicationTitle;
    this.isAppeal = isAppeal;
    this.respondentDisplayNames = respondentDisplayNames;
  }

  public String getApplicantDisplayName() {
    return applicantDisplayName;
  }

  public UUID getApplicationId() {
    return applicationId;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getApplicationStatus() {
    return applicationStatus;
  }

  public String getApplicationTitle() {
    return applicationTitle;
  }

  public Boolean getIsAppeal() {
    return isAppeal;
  }

  public List<String> getRespondentDisplayNames() {
    return respondentDisplayNames;
  }

  public static Builder linkedApplicationsSummary() {
    return new LinkedApplicationsSummary.Builder();
  }

  public static class Builder {
    private String applicantDisplayName;

    private UUID applicationId;

    private String applicationReference;

    private String applicationStatus;

    private String applicationTitle;

    private Boolean isAppeal;

    private List<String> respondentDisplayNames;

    public Builder withApplicantDisplayName(final String applicantDisplayName) {
      this.applicantDisplayName = applicantDisplayName;
      return this;
    }

    public Builder withApplicationId(final UUID applicationId) {
      this.applicationId = applicationId;
      return this;
    }

    public Builder withApplicationReference(final String applicationReference) {
      this.applicationReference = applicationReference;
      return this;
    }

    public Builder withApplicationStatus(final String applicationStatus) {
      this.applicationStatus = applicationStatus;
      return this;
    }

    public Builder withApplicationTitle(final String applicationTitle) {
      this.applicationTitle = applicationTitle;
      return this;
    }

    public Builder withIsAppeal(final Boolean isAppeal) {
      this.isAppeal = isAppeal;
      return this;
    }

    public Builder withRespondentDisplayNames(final List<String> respondentDisplayNames) {
      this.respondentDisplayNames = respondentDisplayNames;
      return this;
    }

    public LinkedApplicationsSummary build() {
      return new LinkedApplicationsSummary(applicantDisplayName, applicationId, applicationReference, applicationStatus, applicationTitle, isAppeal, respondentDisplayNames);
    }
  }
}
