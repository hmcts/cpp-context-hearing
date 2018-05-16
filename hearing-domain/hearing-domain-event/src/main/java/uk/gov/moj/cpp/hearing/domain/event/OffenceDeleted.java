package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.offence-deleted")
public class OffenceDeleted {

    private final UUID id;

    private final UUID hearingId;

    private OffenceDeleted(@JsonProperty("id") final UUID id, @JsonProperty("hearingId") final UUID hearingId) {
        this.id = id;
        this.hearingId = hearingId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;

        private UUID hearingId;

        private Builder() {
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public OffenceDeleted build() {
            return new OffenceDeleted(id, hearingId);
        }

    }
}