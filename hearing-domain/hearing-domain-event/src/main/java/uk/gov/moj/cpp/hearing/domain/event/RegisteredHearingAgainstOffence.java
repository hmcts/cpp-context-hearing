package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.registered-hearing-against-offence")
public final class RegisteredHearingAgainstOffence implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID offenceId;

    private final UUID hearingId;

    @JsonCreator
    private RegisteredHearingAgainstOffence(@JsonProperty("offenceId") final UUID offenceId, @JsonProperty("hearingId") final UUID hearingId) {
        this.offenceId = offenceId;
        this.hearingId = hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID offenceId;

        private UUID hearingId;

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public RegisteredHearingAgainstOffence build() {
            return new RegisteredHearingAgainstOffence(offenceId, hearingId);
        }
    }
}
