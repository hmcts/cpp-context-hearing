package uk.gov.moj.cpp.hearing.domain.event.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.event.approval-requestedV2")
public class ApprovalRequestedV2 implements Serializable {
    private static final long serialVersionUID = -5995314363348475391L;

    private final UUID hearingId;

    private final UUID userId;


    @JsonCreator
    public ApprovalRequestedV2(@JsonProperty("hearingId") final UUID hearingId,
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


    public static ApprovalRequestedV2Builder approvalRequestedBuilder() {
        return new ApprovalRequestedV2Builder();
    }


    public static final class ApprovalRequestedV2Builder {

        private UUID hearingId;

        private UUID userId;

        private ApprovalRequestedV2Builder() {
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



        public ApprovalRequestedV2Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ApprovalRequestedV2Builder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }


        public ApprovalRequestedV2 build() {
            return new ApprovalRequestedV2(hearingId, userId);
        }
    }
}
