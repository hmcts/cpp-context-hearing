package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.case-registered")
public class RegisteredHearingAgainstCase implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID caseId;

    private final UUID hearingId;

    private RegisteredHearingAgainstCase(@JsonProperty("caseId") UUID caseId,
                                         @JsonProperty("hearingId") UUID hearingId) {
        this.caseId = caseId;
        this.hearingId = hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static Builder builder() {
        return new Builder();
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
