package uk.gov.moj.cpp.hearing.domain.event.sessiontime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CourtSession implements Serializable {
    private static final long serialVersionUID = 8208397390870813811L;

    private UUID courtAssociateId;

    private UUID courtClerkId;

    private String endTime;

    private List<CourtSessionJudiciary> judiciaries = new ArrayList<>();

    private UUID legalAdviserId;

    private String startTime;

    @JsonCreator
    public CourtSession(@JsonProperty("courtAssociateId") final UUID courtAssociateId,
                        @JsonProperty("courtClerkId") final UUID courtClerkId,
                        @JsonProperty("legalAdviserId") final UUID legalAdviserId,
                        @JsonProperty("startTime") final String startTime,
                        @JsonProperty("endTime") final String endTime,
                        @JsonProperty("judiciaries") final List<CourtSessionJudiciary> judiciaries) {
        this.courtAssociateId = courtAssociateId;
        this.courtClerkId = courtClerkId;
        this.endTime = endTime;
        this.judiciaries = judiciaries;
        this.legalAdviserId = legalAdviserId;
        this.startTime = startTime;
    }

    public UUID getCourtAssociateId() {
        return courtAssociateId;
    }

    public UUID getCourtClerkId() {
        return courtClerkId;
    }

    public String getEndTime() {
        return endTime;
    }

    public List<CourtSessionJudiciary> getJudiciaries() {
        return judiciaries;
    }

    public UUID getLegalAdviserId() {
        return legalAdviserId;
    }

    public String getStartTime() {
        return startTime;
    }

    public static Builder courtSession() {
        return new CourtSession.Builder();
    }

    @Override
    @SuppressWarnings({"squid:S00121","squid:S1067"})
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final CourtSession that = (CourtSession) obj;

        return java.util.Objects.equals(this.courtAssociateId, that.courtAssociateId) &&
                java.util.Objects.equals(this.courtClerkId, that.courtClerkId) &&
                java.util.Objects.equals(this.endTime, that.endTime) &&
                java.util.Objects.equals(this.judiciaries, that.judiciaries) &&
                java.util.Objects.equals(this.legalAdviserId, that.legalAdviserId) &&
                java.util.Objects.equals(this.startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtAssociateId, courtClerkId, endTime, judiciaries, legalAdviserId, startTime);
    }

    @Override
    public String toString() {
        return "CourtSession{" +
                "courtAssociateId='" + courtAssociateId + "'," +
                "courtClerkId='" + courtClerkId + "'," +
                "endTime='" + endTime + "'," +
                "judiciaries='" + judiciaries + "'," +
                "legalAdviserId='" + legalAdviserId + "'," +
                "startTime='" + startTime + "'" +
                "}";
    }

    public CourtSession setCourtAssociateId(UUID courtAssociateId) {
        this.courtAssociateId = courtAssociateId;
        return this;
    }

    public CourtSession setCourtClerkId(UUID courtClerkId) {
        this.courtClerkId = courtClerkId;
        return this;
    }

    public CourtSession setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public CourtSession setJudiciaries(List<CourtSessionJudiciary> judiciaries) {
        this.judiciaries = judiciaries;
        return this;
    }

    public CourtSession setLegalAdviserId(UUID legalAdviserId) {
        this.legalAdviserId = legalAdviserId;
        return this;
    }

    public CourtSession setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public static class Builder {
        private UUID courtAssociateId;

        private UUID courtClerkId;

        private String endTime;

        private List<CourtSessionJudiciary> judiciaries;

        private UUID legalAdviserId;

        private String startTime;

        public Builder withCourtAssociateId(final UUID courtAssociateId) {
            this.courtAssociateId = courtAssociateId;
            return this;
        }

        public Builder withCourtClerkId(final UUID courtClerkId) {
            this.courtClerkId = courtClerkId;
            return this;
        }

        public Builder withEndTime(final String endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder withJudiciaries(final List<CourtSessionJudiciary> judiciaries) {
            this.judiciaries = judiciaries;
            return this;
        }

        public Builder withLegalAdviserId(final UUID legalAdviserId) {
            this.legalAdviserId = legalAdviserId;
            return this;
        }

        public Builder withStartTime(final String startTime) {
            this.startTime = startTime;
            return this;
        }

        public CourtSession build() {
            return new CourtSession(courtAssociateId, courtClerkId, legalAdviserId, startTime, endTime, judiciaries);
        }
    }
}
