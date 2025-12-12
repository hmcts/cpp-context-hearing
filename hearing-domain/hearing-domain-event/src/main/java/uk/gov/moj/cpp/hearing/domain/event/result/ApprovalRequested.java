package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.approval-requested")
public class ApprovalRequested implements Serializable {
    private static final long serialVersionUID = -5995314363348475391L;

    private final UUID hearingId;

    private final UUID userId;


    @JsonCreator
    public ApprovalRequested(@JsonProperty("hearingId") final UUID hearingId,
                             @JsonProperty("userId") final UUID userId) {
        this.hearingId = hearingId;
        this.userId = userId;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public UUID getUserId() {
        return this.userId;
    }


    public static ApprovalRequested.ApprovalRequestedBuilder approvalRequestedBuilder() {
        return new ApprovalRequested.ApprovalRequestedBuilder();
    }


    public static final class ApprovalRequestedBuilder {

        private UUID hearingId;
        private UUID userId;
        private ApprovalRequestedBuilder() {
        }
        public UUID getHearingId() {
            return hearingId;
        }

        public UUID getUserId() {
            return userId;
        }



        public ApprovalRequested.ApprovalRequestedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ApprovalRequested.ApprovalRequestedBuilder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }


        public ApprovalRequested build() {
            return new ApprovalRequested(hearingId, userId);
        }
    }
}
