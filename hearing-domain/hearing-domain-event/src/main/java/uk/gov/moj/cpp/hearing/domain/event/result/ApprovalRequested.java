package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.ApprovalType;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.approval-requested")
public class ApprovalRequested implements Serializable {
    private static final long serialVersionUID = -5995314363348475391L;

    private final UUID hearingId;

    private final UUID userId;

    private final ZonedDateTime requestApprovalTime;

    private final ApprovalType approvalType;

    @JsonCreator
    public ApprovalRequested(@JsonProperty("hearingId") final UUID hearingId,
                             @JsonProperty("userId") final UUID userId,
                             @JsonProperty("requestApprovalTime") final ZonedDateTime requestApprovalTime,
                             @JsonProperty("approvalType") final ApprovalType approvalType) {

        this.hearingId = hearingId;
        this.userId = userId;
        this.requestApprovalTime = requestApprovalTime;
        this.approvalType = approvalType;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public ZonedDateTime getRequestApprovalTime() {
        return this.requestApprovalTime;
    }

    public ApprovalType getApprovalType() {
        return this.approvalType;
    }

    public static ApprovalRequested.ApprovalRequestedBuilder approvalRequestedBuilder() {
        return new ApprovalRequested.ApprovalRequestedBuilder();
    }


    public static final class ApprovalRequestedBuilder {
        private UUID hearingId;
        private UUID userId;
        private ZonedDateTime requestApprovalTime;
        private ApprovalType approvalType;

        private ApprovalRequestedBuilder() {
        }

        public ApprovalRequested.ApprovalRequestedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ApprovalRequested.ApprovalRequestedBuilder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }

        public ApprovalRequested.ApprovalRequestedBuilder withRequestApprovalTime(final ZonedDateTime requestApprovalTime) {
            this.requestApprovalTime = requestApprovalTime;
            return this;
        }

        public ApprovalRequested.ApprovalRequestedBuilder withApprovalType(final ApprovalType approvalType) {
            this.approvalType = approvalType;
            return this;
        }

        public ApprovalRequested build() {
            return new ApprovalRequested(hearingId, userId, requestApprovalTime, approvalType);
        }
    }
}
