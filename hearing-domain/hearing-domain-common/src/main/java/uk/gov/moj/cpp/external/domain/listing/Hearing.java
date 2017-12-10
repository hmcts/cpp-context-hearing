package uk.gov.moj.cpp.external.domain.listing;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class Hearing implements Serializable {

    private UUID id;
    private String caseId;
    private String courtCentreId;
    private String courtCentreName;
    private String courtRoomId;
    private String courtRoomName;
    private String type;
    private Boolean notBefore;
    private ZonedDateTime startDateTime;
    private int estimateMinutes;
    private List<Defendant> defendants;
    private Judge judge;

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public String getCourtRoomId() {
        return courtRoomId;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public Judge getJudge() {
        return judge;
    }

    public String getCaseId() {
        return caseId;
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

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public int getEstimateMinutes() {
        return estimateMinutes;
    }

    public List<Defendant> getDefendants() {
        return new ArrayList(defendants);
    }

    public Boolean getNotBefore() {
        return notBefore;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setCourtCentreId(String courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public void setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
    }

    public void setCourtRoomId(String courtRoomId) {
        this.courtRoomId = courtRoomId;
    }

    public void setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNotBefore(Boolean notBefore) {
        this.notBefore = notBefore;
    }

    public void setStartDateTime(ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEstimateMinutes(int estimateMinutes) {
        this.estimateMinutes = estimateMinutes;
    }

    public void setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
    }

    public void setJudge(Judge judge) {
        this.judge = judge;
    }
}
