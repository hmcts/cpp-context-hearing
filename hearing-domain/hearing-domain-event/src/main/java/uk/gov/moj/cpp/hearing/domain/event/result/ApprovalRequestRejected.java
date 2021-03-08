package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.approval-rejected")
public class ApprovalRequestRejected implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;

    private final UUID hearingId;

    private final UUID userId;


    @JsonCreator
    public ApprovalRequestRejected(@JsonProperty("hearingId") final UUID hearingId,
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


    public static ApprovalRequestRejected.ApprovalRejectedBuilder approvalRejectedBuilder() {
        return new ApprovalRequestRejected.ApprovalRejectedBuilder();
    }


    public static final class ApprovalRejectedBuilder {

        private UUID hearingId;
        private UUID userId;

        private ApprovalRejectedBuilder() {
        }

        public void setHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
        }

        public void setUserId(final UUID userId) {
            this.userId = userId;
        }



        public UUID getHearingId() {
            return hearingId;
        }

        public UUID getUserId() {
            return userId;
        }

        public ApprovalRequestRejected.ApprovalRejectedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ApprovalRequestRejected.ApprovalRejectedBuilder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }


        public ApprovalRequestRejected build() {
            return new ApprovalRequestRejected(hearingId, userId);
        }
    }
}
