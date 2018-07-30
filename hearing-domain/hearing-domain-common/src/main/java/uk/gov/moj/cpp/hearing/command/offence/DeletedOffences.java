package uk.gov.moj.cpp.hearing.command.offence;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeletedOffences {
    private  UUID defendantId;
    private  UUID caseId;
    private  List<UUID> offences;


    @JsonCreator
    public DeletedOffences(@JsonProperty("defendantId") final UUID defendantId,
                           @JsonProperty("caseId") final UUID caseId,
                           @JsonProperty("offences") final List<UUID> offences) {
        this.defendantId = defendantId;
        this.caseId = caseId;
        this.offences = offences;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public List<UUID> getOffences() {
        return offences;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }

    public void setOffences(List<UUID> offences) {
        this.offences = offences;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;


    }

    public static DeletedOffences.Builder builder() {
        return new DeletedOffences.Builder();
    }

    public static class Builder {
        private List<UUID> offences;
        private UUID defendantId;
        private UUID caseId;

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendantId(UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withOffences(List<UUID> offences) {
            this.offences = offences;
            return this;
        }


        public DeletedOffences build() {
            return new DeletedOffences(this.defendantId, this.caseId, this.offences);
        }
    }
}
