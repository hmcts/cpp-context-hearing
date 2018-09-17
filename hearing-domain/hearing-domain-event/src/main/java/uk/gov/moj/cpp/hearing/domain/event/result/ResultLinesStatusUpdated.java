package uk.gov.moj.cpp.hearing.domain.event.result;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.result-lines-status-updated")
public final class ResultLinesStatusUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private ZonedDateTime lastSharedDateTime;

    private CourtClerk courtClerk;

    private final List<SharedResultLineId> sharedResultLines;


    @JsonCreator
    private ResultLinesStatusUpdated(@JsonProperty("hearingId") final UUID hearingId,
                                     @JsonProperty("lastSharedDateTime") final ZonedDateTime lastSharedDateTime,
                                     @JsonProperty("courtClerk") final CourtClerk courtClerk,
                                     @JsonProperty("sharedResultLines") final List<SharedResultLineId> sharedResultLines) {
        this.hearingId = hearingId;
        this.lastSharedDateTime = lastSharedDateTime;
        this.courtClerk = courtClerk;
        this.sharedResultLines = ofNullable(sharedResultLines).orElseGet(ArrayList::new);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public List<SharedResultLineId> getSharedResultLines() {
        return sharedResultLines;
    }

    public void setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
        this.lastSharedDateTime = lastSharedDateTime;
    }

    public void setCourtClerk(final CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;

        private ZonedDateTime lastSharedDateTime;

        private CourtClerk courtClerk;

        private List<SharedResultLineId> sharedResultLines;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
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

        public Builder withSharedResultLines(final List<SharedResultLineId> sharedResultLines) {
            this.sharedResultLines = sharedResultLines;
            return this;
        }


        public ResultLinesStatusUpdated build() {
            return new ResultLinesStatusUpdated(
                    hearingId,
                    lastSharedDateTime,
                    courtClerk,
                    sharedResultLines);
        }
    }
}