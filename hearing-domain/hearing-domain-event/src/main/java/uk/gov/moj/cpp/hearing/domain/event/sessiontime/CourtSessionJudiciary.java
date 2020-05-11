package uk.gov.moj.cpp.hearing.domain.event.sessiontime;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CourtSessionJudiciary implements Serializable {
  private static final long serialVersionUID = 8208397390870813811L;

  private UUID judiciaryId;

  private String judiciaryName;

  private Boolean benchChairman;
  @JsonCreator
  public CourtSessionJudiciary(
          @JsonProperty("judiciaryId") final UUID judiciaryId,
          @JsonProperty("judiciaryName") final String judiciaryName,
          @JsonProperty("benchChairman") final Boolean benchChairman) {
    this.judiciaryId = judiciaryId;
    this.judiciaryName = judiciaryName;
    this.benchChairman = benchChairman;

  }

  public Boolean getBenchChairman() {
    return benchChairman;
  }

  public UUID getJudiciaryId() {
    return judiciaryId;
  }

  public String getJudiciaryName() {
    return judiciaryName;
  }

  public static Builder courtSessionJudiciary() {
    return new CourtSessionJudiciary.Builder();
  }

  @Override
  @SuppressWarnings({"squid:S00121","squid:S1067"})
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    final CourtSessionJudiciary that = (CourtSessionJudiciary) obj;

    return java.util.Objects.equals(this.benchChairman, that.benchChairman) &&
    java.util.Objects.equals(this.judiciaryId, that.judiciaryId) &&
    java.util.Objects.equals(this.judiciaryName, that.judiciaryName);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(benchChairman, judiciaryId, judiciaryName);}

  @Override
  public String toString() {
    return "CourtSessionJudiciary{" +
    	"benchChairman='" + benchChairman + "'," +
    	"judiciaryId='" + judiciaryId + "'," +
    	"judiciaryName='" + judiciaryName + "'" +
    "}";
  }

  public CourtSessionJudiciary setBenchChairman(Boolean benchChairman) {
    this.benchChairman = benchChairman;
    return this;
  }

  public CourtSessionJudiciary setJudiciaryId(UUID judiciaryId) {
    this.judiciaryId = judiciaryId;
    return this;
  }

  public CourtSessionJudiciary setJudiciaryName(String judiciaryName) {
    this.judiciaryName = judiciaryName;
    return this;
  }

  public static class Builder {
    private Boolean benchChairman;

    private UUID judiciaryId;

    private String judiciaryName;

    public Builder withBenchChairman(final Boolean benchChairman) {
      this.benchChairman = benchChairman;
      return this;
    }

    public Builder withJudiciaryId(final UUID judiciaryId) {
      this.judiciaryId = judiciaryId;
      return this;
    }

    public Builder withJudiciaryName(final String judiciaryName) {
      this.judiciaryName = judiciaryName;
      return this;
    }

    public CourtSessionJudiciary build() {
      return new CourtSessionJudiciary(judiciaryId, judiciaryName, benchChairman);
    }
  }
}
