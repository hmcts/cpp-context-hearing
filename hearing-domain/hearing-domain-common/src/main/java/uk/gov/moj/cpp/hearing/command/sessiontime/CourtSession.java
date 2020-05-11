package uk.gov.moj.cpp.hearing.command.sessiontime;

import java.util.List;
import java.util.UUID;

public class CourtSession {
    private List<CourtSessionJudiciary> judiciaries;
    private String startTime;
    private String endTime;
    private UUID courtClerkId;
    private UUID courtAssociateId;
    private UUID legalAdviserId;

    public List<CourtSessionJudiciary> getJudiciaries() {
        return judiciaries;
    }

    public void setJudiciaries(List<CourtSessionJudiciary> judiciaries) {
        this.judiciaries = judiciaries;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public UUID getCourtClerkId() {
        return courtClerkId;
    }

    public void setCourtClerkId(UUID courtClerkId) {
        this.courtClerkId = courtClerkId;
    }

    public UUID getCourtAssociateId() {
        return courtAssociateId;
    }

    public void setCourtAssociateId(UUID courtAssociateId) {
        this.courtAssociateId = courtAssociateId;
    }

    public UUID getLegalAdviserId() {
        return legalAdviserId;
    }

    public void setLegalAdviserId(UUID legalAdviserId) {
        this.legalAdviserId = legalAdviserId;
    }


    @Override
    public String toString() {
        return "CourtSession{" +
                "judiciaries=" + judiciaries +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", courtClerkId=" + courtClerkId +
                ", courtAssociateId=" + courtAssociateId +
                ", legalAdviserId=" + legalAdviserId +
                '}';
    }


    @Override
    @SuppressWarnings({"squid:S00121","squid:S1067"})
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourtSession)) return false;

        CourtSession that = (CourtSession) o;

        if (judiciaries != null ? !judiciaries.equals(that.judiciaries) : that.judiciaries != null)
            return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null)
            return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (courtClerkId != null ? !courtClerkId.equals(that.courtClerkId) : that.courtClerkId != null)
            return false;
        if (courtAssociateId != null ? !courtAssociateId.equals(that.courtAssociateId) : that.courtAssociateId != null)
            return false;
        return legalAdviserId != null ? legalAdviserId.equals(that.legalAdviserId) : that.legalAdviserId == null;
    }

    @Override
    public int hashCode() {
        int result = judiciaries != null ? judiciaries.hashCode() : 0;
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (courtClerkId != null ? courtClerkId.hashCode() : 0);
        result = 31 * result + (courtAssociateId != null ? courtAssociateId.hashCode() : 0);
        result = 31 * result + (legalAdviserId != null ? legalAdviserId.hashCode() : 0);
        return result;
    }


}
