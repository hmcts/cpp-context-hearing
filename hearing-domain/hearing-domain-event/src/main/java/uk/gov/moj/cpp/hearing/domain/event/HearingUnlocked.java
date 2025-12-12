package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-unlocked")
public class HearingUnlocked implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;

    private final UUID hearingId;

    private final UUID userId;


    @JsonCreator
    public HearingUnlocked(@JsonProperty("hearingId") final UUID hearingId,
                           @JsonProperty("userId") final UUID userId) {
        this.hearingId = hearingId;
        this.userId = userId;
    }

    public static HearingUnlocked.HearingUnlockedBuilder hearingUnlockedBuilder() {
        return new HearingUnlocked.HearingUnlockedBuilder();
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public UUID getUserId() {
        return this.userId;
    }

    public static final class HearingUnlockedBuilder {

        private UUID hearingId;
        private UUID userId;

        private HearingUnlockedBuilder() {
        }

        public UUID getHearingId() {
            return hearingId;
        }

        public void setHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(final UUID userId) {
            this.userId = userId;
        }

        public HearingUnlocked.HearingUnlockedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public HearingUnlocked.HearingUnlockedBuilder withUserId(final UUID userId) {
            this.userId = userId;
            return this;
        }
        public HearingUnlocked build() {
            return new HearingUnlocked(hearingId, userId);
        }
    }
}
