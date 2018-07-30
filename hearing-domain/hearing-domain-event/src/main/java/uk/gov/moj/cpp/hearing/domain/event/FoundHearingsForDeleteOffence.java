package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.found-hearings-for-delete-offence")
public class FoundHearingsForDeleteOffence implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final List<UUID> hearingIds;

    @JsonCreator
    private FoundHearingsForDeleteOffence(@JsonProperty("id") final UUID id,
                                          @JsonProperty("hearingIds") final List<UUID> hearingIds) {
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

        public FoundHearingsForDeleteOffence build() {
            return new FoundHearingsForDeleteOffence(id, hearingIds);
        }
    }
}
