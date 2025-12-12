package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.hearing-locked")
public class HearingLocked implements Serializable {


    private UUID hearingId;


    @JsonCreator
    private HearingLocked(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }


    public static HearingLocked.Builder builder() {
        return new HearingLocked.Builder();
    }



    public static final class Builder {

        private UUID hearingId;

        public UUID getHearingId() {
            return hearingId;
        }

        public void setHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
        }

        public HearingLocked.Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public HearingLocked build() {
            return new HearingLocked(hearingId);
        }
    }
}