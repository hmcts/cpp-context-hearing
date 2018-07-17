package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

public final class CompletedResultLineStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private CourtClerk courtClerk;

    private ZonedDateTime lastSharedDateTime;


    @JsonCreator
    private CompletedResultLineStatus(@JsonProperty("id") final UUID id,
                                      @JsonProperty("lastSharedDateTime") final ZonedDateTime lastSharedDateTime,
                                      @JsonProperty("courtClerk") final CourtClerk courtClerk) {
        this.id = id;
        this.lastSharedDateTime = lastSharedDateTime;
        this.courtClerk = courtClerk;
    }

    public UUID getId() {
        return id;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public CompletedResultLineStatus setCourtClerk(final CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public CompletedResultLineStatus setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
        this.lastSharedDateTime = lastSharedDateTime;
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID id;

        private ZonedDateTime lastSharedDateTime;

        private CourtClerk courtClerk;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public Builder withCourtClerk(final CourtClerk courtClerk) {
            this.courtClerk = courtClerk;
            return this;
        }

        public CompletedResultLineStatus build() {
            return new CompletedResultLineStatus(id, lastSharedDateTime, courtClerk);
        }
    }
}
