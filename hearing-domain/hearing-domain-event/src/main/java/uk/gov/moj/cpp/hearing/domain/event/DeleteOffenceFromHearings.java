package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.delete-case-defendant-offence-enriched-with-hearing-ids")
public class DeleteOffenceFromHearings {

    private final UUID id;

    private final List<UUID> hearingIds;

    private DeleteOffenceFromHearings(@JsonProperty("id") final UUID id, @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.id = id;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public UUID getId() {
        return id;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;

        private List<UUID> hearingIds;

        private Builder() {
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withHearingIds(final List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
            return this;
        }

        public DeleteOffenceFromHearings build() {
            return new DeleteOffenceFromHearings(id, hearingIds);
        }
    }
}
