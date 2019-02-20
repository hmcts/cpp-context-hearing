package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.CourtClerk;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CompletedResultLineStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private uk.gov.justice.core.courts.CourtClerk courtClerk;

    private ZonedDateTime lastSharedDateTime;


    @JsonCreator
    private CompletedResultLineStatus(@JsonProperty("id") final UUID id,
                                      @JsonProperty("lastSharedDateTime") final ZonedDateTime lastSharedDateTime,
                                      @JsonProperty("courtClerk") final uk.gov.justice.core.courts.CourtClerk courtClerk) {
        this.id = id;
        this.lastSharedDateTime = lastSharedDateTime;
        this.courtClerk = courtClerk;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public CompletedResultLineStatus setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
        this.lastSharedDateTime = lastSharedDateTime;
        return this;
    }

    public uk.gov.justice.core.courts.CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public CompletedResultLineStatus setCourtClerk(final uk.gov.justice.core.courts.CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public static final class Builder {

        private UUID id;

        private ZonedDateTime lastSharedDateTime;

        private uk.gov.justice.core.courts.CourtClerk courtClerk;

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
