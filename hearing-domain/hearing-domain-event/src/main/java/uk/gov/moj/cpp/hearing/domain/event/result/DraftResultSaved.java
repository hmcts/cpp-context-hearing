package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
@Event("hearing.draft-result-saved")
public class DraftResultSaved implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID targetId;
    private final UUID hearingId;
    private final UUID defendantId;
    private final UUID offenceId;
    private final String draftResult;

    @JsonCreator
    protected DraftResultSaved(@JsonProperty("targetId") final UUID targetId, 
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("defendantId") final UUID defendantId, 
            @JsonProperty("offenceId") final UUID offenceId,
            @JsonProperty("draftResult") final String draftResult) {
        this.targetId = targetId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.draftResult = draftResult;
        this.hearingId = hearingId;
    }

    @JsonIgnore
    private DraftResultSaved(final Builder builder) {
        this.targetId = builder.targetId;
        this.hearingId = builder.hearingId;
        this.defendantId = builder.defendantId;
        this.offenceId = builder.offenceId;
        this.draftResult = builder.draftResult;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getHearingId() {
        return hearingId;
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

    @SuppressWarnings("PMD.BeanMembersShouldSerialize")
    public static final class Builder {

        private UUID targetId;
        private UUID defendantId;
        private UUID offenceId;
        private String draftResult;
        private UUID hearingId;

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

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public DraftResultSaved build() {
            return new DraftResultSaved(this);
        }
    }
}