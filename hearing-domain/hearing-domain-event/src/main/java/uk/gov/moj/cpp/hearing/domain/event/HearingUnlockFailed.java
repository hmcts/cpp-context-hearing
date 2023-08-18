package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-unlock-failed")
public class HearingUnlockFailed implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;

    private final UUID hearingId;
    private final String reason;

    @JsonCreator
    public HearingUnlockFailed(@JsonProperty("hearingId") final UUID hearingId,
                           @JsonProperty("reason") final String reason) {
        this.hearingId = hearingId;
        this.reason = reason;
    }

    public static HearingUnlockFailed.HearingUnlockedFailedBuilder hearingUnlockedFailedBuilder() {
        return new HearingUnlockFailed.HearingUnlockedFailedBuilder();
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public String getReason() {
        return this.reason;
    }

    public static final class HearingUnlockedFailedBuilder {

        private UUID hearingId;
        private String reason;

        private HearingUnlockedFailedBuilder() {
        }

        public UUID getHearingId() {
            return hearingId;
        }

        public void setHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(final String reason) {
            this.reason = reason;
        }

        public HearingUnlockFailed.HearingUnlockedFailedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public HearingUnlockFailed.HearingUnlockedFailedBuilder withReason(final String reason) {
            this.reason = reason;
            return this;
        }

        public HearingUnlockFailed build() {
            return new HearingUnlockFailed(hearingId, reason);
        }
    }

}
