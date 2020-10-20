package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.ApprovalType;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RequestApprovalCommand {

    private UUID hearingId;
    private UUID userId;
    private ZonedDateTime requestApprovalTime;
    private ApprovalType approvalType;


    public RequestApprovalCommand() {
    }

    private RequestApprovalCommand(final UUID hearingId,
                                   final UUID userId,
                                   final ZonedDateTime requestApprovalTime,
                                   final ApprovalType approvalType) {
        this.hearingId = hearingId;
        this.userId = userId;
        this.requestApprovalTime = requestApprovalTime;
        this.approvalType = approvalType;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final RequestApprovalCommand copy) {
        final Builder builder = new Builder();
        builder.hearingId = copy.getHearingId();
        builder.userId = copy.getUserId();
        builder.requestApprovalTime = copy.getRequestApprovalTime();
        builder.approvalType = copy.getApprovalType();
        return builder;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ZonedDateTime getRequestApprovalTime() {
        return requestApprovalTime;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public static final class Builder {
        private UUID hearingId;
        private UUID userId;
        private ZonedDateTime requestApprovalTime;
        private ApprovalType approvalType;

        private Builder() {
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder withRequestApprovalTime(final ZonedDateTime requestApprovalTime) {
            this.requestApprovalTime = requestApprovalTime;
            return this;
        }

        public Builder withApprovalType(final ApprovalType approvalType) {
            this.approvalType = approvalType;
            return this;
        }

        public RequestApprovalCommand build() {
            return new RequestApprovalCommand(hearingId, userId, requestApprovalTime, approvalType);
        }
    }
}
