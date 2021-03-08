package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.hearing-locked-by-other-user")
public class HearingLockedByOtherUser implements Serializable {


    private UUID hearingId;


    @JsonCreator
    private HearingLockedByOtherUser(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {

        return this.hearingId;
    }

    public void setHearingId(final UUID hearingId) {

        this.hearingId = hearingId;
    }

    public static HearingLockedByOtherUser.Builder builder() {
        return new HearingLockedByOtherUser.Builder();
    }



    public static final class Builder {


        private UUID hearingId;

        public UUID getHearingId() {
            return hearingId;
        }

        public void setHearingId(final UUID hearingId) {

            this.hearingId = hearingId;
        }

        public HearingLockedByOtherUser.Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public HearingLockedByOtherUser build() {
            return new HearingLockedByOtherUser(hearingId);
        }
    }
}