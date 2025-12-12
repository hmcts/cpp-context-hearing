package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.registered-hearing-against-offence-v2")
public final class RegisteredHearingAgainstOffenceV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID offenceId;

    private final List<UUID> hearingIds;

    @JsonCreator
    private RegisteredHearingAgainstOffenceV2(@JsonProperty("offenceId") final UUID offenceId, @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.offenceId = offenceId;
        this.hearingIds = hearingIds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public static class Builder {

        private UUID offenceId;

        private List<UUID> hearingIds;

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withHearingIds(final List<UUID> hearingIds) {
            this.hearingIds = hearingIds.stream().toList();
            return this;
        }

        public RegisteredHearingAgainstOffenceV2 build() {
            return new RegisteredHearingAgainstOffenceV2(offenceId, hearingIds);
        }
    }
}
