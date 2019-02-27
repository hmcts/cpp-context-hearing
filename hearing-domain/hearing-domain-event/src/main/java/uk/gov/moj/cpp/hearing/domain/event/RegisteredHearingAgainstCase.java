package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.registered-hearing-against-case")
public class RegisteredHearingAgainstCase implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID caseId;

    private final UUID hearingId;

    @JsonCreator
    private RegisteredHearingAgainstCase(@JsonProperty("caseId") UUID caseId,
                                         @JsonProperty("hearingId") UUID hearingId) {
        this.caseId = caseId;
        this.hearingId = hearingId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static class Builder {

        private UUID caseId;

        private UUID hearingId;

        private Builder() {
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public RegisteredHearingAgainstCase build() {
            return new RegisteredHearingAgainstCase(caseId, hearingId);
        }
    }
}
