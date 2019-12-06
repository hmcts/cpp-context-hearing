package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;

public class CurrentCourtStatus implements Serializable {
  private static final long serialVersionUID = -4151921355339340656L;

  private Court court;

  private String pageName;

  public CurrentCourtStatus(final Court court, final String pageName) {
    this.court = court;
    this.pageName = pageName;
  }

  public Court getCourt() {
    return court;
  }

  public String getPageName() {
    return pageName;
  }

  public static Builder currentCourtStatus() {
    return new CurrentCourtStatus.Builder();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj){
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final CurrentCourtStatus that = (CurrentCourtStatus) obj;

    return java.util.Objects.equals(this.court, that.court) &&
    java.util.Objects.equals(this.pageName, that.pageName);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(court, pageName);}

  @Override
  public String toString() {
    return "CurrentCourtStatus{" +
    	"court='" + court + "'," +
    	"pageName='" + pageName + "'" +
    "}";
  }

  public CurrentCourtStatus setCourt(Court court) {
    this.court = court;
    return this;
  }

  public CurrentCourtStatus setPageName(String pageName) {
    this.pageName = pageName;
    return this;
  }

  public static class Builder {
    private Court court;

    private String pageName;

    public Builder withCourt(final Court court) {
      this.court = court;
      return this;
    }

    public Builder withPageName(final String pageName) {
      this.pageName = pageName;
      return this;
    }

    public CurrentCourtStatus build() {
      return new CurrentCourtStatus(court, pageName);
    }
  }
}
