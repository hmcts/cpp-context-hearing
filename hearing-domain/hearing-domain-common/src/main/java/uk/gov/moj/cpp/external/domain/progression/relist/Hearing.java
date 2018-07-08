package uk.gov.moj.cpp.external.domain.progression.relist;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class Hearing implements Serializable {

    private static final long serialVersionUID = -7393868029752131025L;
    private final UUID id;
    private final String courtCentreId;
    private final String type;
    private final String startDate;
    private final String startTime;
    private final int estimateMinutes;
    private final List<Defendant> defendants;

    @JsonCreator
    public Hearing(@JsonProperty(value = "id") final UUID id,
                   @JsonProperty(value = "courtCentreId") final String courtCentreId,
                   @JsonProperty(value = "type") final String type,
                   @JsonProperty(value = "startDate") final String startDate,
                   @JsonProperty(value = "startTime") final String startTime,
                   @JsonProperty(value = "estimateMinutes") final int estimateMinutes,
                   @JsonProperty(value = "defendants") final List<Defendant> defendants) {
        this.id = id;
        this.courtCentreId = courtCentreId;
        this.type = type;
        this.startDate = startDate;
        this.startTime = startTime;
        this.estimateMinutes = estimateMinutes;
        this.defendants = defendants;
    }

    public UUID getId() {
        return id;
    }

    public String getCourtCentreId() {
        return courtCentreId;
    }

    public String getType() {
        return type;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getEstimateMinutes() {
        return estimateMinutes;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }
}
