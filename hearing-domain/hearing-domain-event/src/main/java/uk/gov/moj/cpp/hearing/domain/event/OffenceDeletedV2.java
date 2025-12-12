package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.offence-deleted-v2")
public class OffenceDeletedV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<UUID> ids;

    private final UUID hearingId;

    @JsonCreator
    private OffenceDeletedV2(@JsonProperty("ids") final List<UUID> ids,
                             @JsonProperty("hearingId") final UUID hearingId) {
        this.ids = ids;
        this.hearingId = hearingId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<UUID> getIds() {
        return ids;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static class Builder {

        private List<UUID> ids;

        private UUID hearingId;

        private Builder() {
        }

        public Builder withIds(final List<UUID> ids) {
            this.ids = ids;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public OffenceDeletedV2 build() {
            return new OffenceDeletedV2(ids, hearingId);
        }

    }
}