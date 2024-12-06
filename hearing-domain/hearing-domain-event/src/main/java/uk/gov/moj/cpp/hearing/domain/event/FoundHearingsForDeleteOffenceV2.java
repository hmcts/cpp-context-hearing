package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.found-hearings-for-delete-offence-v2")
public class FoundHearingsForDeleteOffenceV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<UUID> ids;

    private final List<UUID> hearingIds;

    @JsonCreator
    private FoundHearingsForDeleteOffenceV2(@JsonProperty("ids") final List<UUID> ids,
                                            @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.ids = ids;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<UUID> getIds() {
        return ids;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public static class Builder {

        private List<UUID> ids;

        private List<UUID> hearingIds;

        private Builder() {
        }

        public Builder withIds(final List<UUID> ids) {
            this.ids = ids;
            return this;
        }

        public Builder withHearingIds(final List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
            return this;
        }

        public FoundHearingsForDeleteOffenceV2 build() {
            return new FoundHearingsForDeleteOffenceV2(ids, hearingIds);
        }
    }
}
