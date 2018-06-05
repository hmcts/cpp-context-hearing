package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class SaveDraftResultCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private  UUID hearingId;
    private final UUID targetId;
    private final UUID defendantId;
    private final UUID offenceId;
    private final String draftResult;

    @JsonCreator
    protected SaveDraftResultCommand(@JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("targetId") final UUID targetId,
            @JsonProperty("defendantId") final UUID defendantId, 
            @JsonProperty("offenceId") final UUID offenceId, 
            @JsonProperty("draftResult") final String draftResult) {
        this.hearingId = hearingId;
        this.targetId = targetId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.draftResult = draftResult;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public String getDraftResult() {
        return draftResult;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;
        private UUID targetId;
        private UUID defendantId;
        private UUID offenceId;
        private String draftResult;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withTargetId(final UUID targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withDraftResult(final String draftResult) {
            this.draftResult = draftResult;
            return this;
        }

        public SaveDraftResultCommand build() {
            return new SaveDraftResultCommand(this.hearingId, this.targetId, this.defendantId, this.offenceId, this.draftResult);
        }
    }
}