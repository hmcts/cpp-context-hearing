package uk.gov.moj.cpp.hearing.domain.event.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingState;

import java.util.UUID;

@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
@Event("hearing.replication-of-share-results-failed")
public class ReplicationOfShareResultsFailed {

    private UUID hearingId;

    private HearingState hearingState;

    private UUID amendedByUserId;

    @JsonCreator
    private ReplicationOfShareResultsFailed(@JsonProperty("hearingId") final UUID hearingId,
                                            @JsonProperty("hearingState") final HearingState hearingState,
                                            @JsonProperty("amendedByUserId") final UUID amendedByUserId) {
        this.hearingId = hearingId;
        this.hearingState = hearingState;
        this.amendedByUserId = amendedByUserId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearing(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public HearingState getHearingState() {
        return hearingState;
    }

    public void setHearingState(final HearingState hearingState) {
        this.hearingState = hearingState;
    }

    public UUID getAmendedByUserId() {
        return amendedByUserId;
    }

    public void setAmendedByUserId(final UUID amendedByUserId) {
        this.amendedByUserId = amendedByUserId;
    }

    @SuppressWarnings("PMD:BeanMembersShouldSerialize")
    public static final class Builder {

        private UUID hearingId;

        private HearingState hearingState;

        private UUID amendedByUserId;

        public ReplicationOfShareResultsFailed.Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ReplicationOfShareResultsFailed.Builder withHearingState(final HearingState hearingState) {
            this.hearingState = hearingState;
            return this;
        }

        public ReplicationOfShareResultsFailed.Builder withAmendedByUserId(final UUID amendedByUserId) {
            this.amendedByUserId = amendedByUserId;
            return this;
        }

        public ReplicationOfShareResultsFailed build() {
            return new ReplicationOfShareResultsFailed(hearingId, hearingState, amendedByUserId);
        }
    }

}
