package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.registered-hearing-against-defendant")
public final class RegisteredHearingAgainstDefendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID defendantId;

    private final UUID hearingId;

    @JsonCreator
    public RegisteredHearingAgainstDefendant(
            @JsonProperty("defendantId") final UUID defendantId,
            @JsonProperty("hearingId") final UUID hearingId) {
        this.defendantId = defendantId;
        this.hearingId = hearingId;
    }

    public static Builder builder() {
        return new RegisteredHearingAgainstDefendant.Builder();
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static class Builder {

        private UUID defendantId;

        private UUID hearingId;

        private Builder() {
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public RegisteredHearingAgainstDefendant build() {
            return new RegisteredHearingAgainstDefendant(defendantId, hearingId);
        }
    }
}
