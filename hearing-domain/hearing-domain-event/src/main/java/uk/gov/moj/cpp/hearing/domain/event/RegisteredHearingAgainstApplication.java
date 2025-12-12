package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.registered-hearing-against-application")
public class RegisteredHearingAgainstApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID applicationId;

    private final UUID hearingId;

    @JsonCreator
    public RegisteredHearingAgainstApplication(@JsonProperty("applicationId") UUID applicationId,
                                                @JsonProperty("hearingId") UUID hearingId) {
        this.applicationId = applicationId;
        this.hearingId = hearingId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static class Builder {

        private UUID applicationId;

        private UUID hearingId;

        private Builder() {
        }

        public Builder withApplicationId(final UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public RegisteredHearingAgainstApplication build() {
            return new RegisteredHearingAgainstApplication(applicationId, hearingId);
        }
    }
}
