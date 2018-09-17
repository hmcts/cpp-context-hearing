package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

public final class UpdateResultLinesStatusCommand implements Serializable {


    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final ZonedDateTime lastSharedDateTime;
    private final uk.gov.justice.json.schemas.core.CourtClerk courtClerk;
    private final List<SharedResultLineId> sharedResultLines;

    @JsonCreator
    protected UpdateResultLinesStatusCommand( @JsonProperty("hearingId") final UUID hearingId,
                                              @JsonProperty("courtClerk") final uk.gov.justice.json.schemas.core.CourtClerk courtClerk,
                                              @JsonProperty("lastSharedDateTime") final ZonedDateTime lastSharedDateTime,
                                              @JsonProperty("sharedResultLines") final List<SharedResultLineId> sharedResultLines)  {
        this.hearingId = hearingId;
        this.lastSharedDateTime = lastSharedDateTime;
        this.courtClerk = courtClerk;
        this.sharedResultLines = unmodifiableList(ofNullable(sharedResultLines).orElseGet(ArrayList::new));;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public uk.gov.justice.json.schemas.core.CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public List<SharedResultLineId> getSharedResultLines() {
        return sharedResultLines;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;
        private ZonedDateTime lastSharedDateTime;
        private uk.gov.justice.json.schemas.core.CourtClerk courtClerk;
        private List<SharedResultLineId> sharedResultLines;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder  withSharedResultLines(final List<SharedResultLineId> sharedResultLines) {
            this.sharedResultLines = sharedResultLines;
            return this;
        }

        public Builder withLastSharedDateTime(final ZonedDateTime lastSharedDateTime){
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public Builder withCourtClerk(uk.gov.justice.json.schemas.core.CourtClerk courtClerk){
            this.courtClerk = courtClerk;
            return this;
        }

        public UpdateResultLinesStatusCommand build() {
            return new UpdateResultLinesStatusCommand(this.hearingId, courtClerk, lastSharedDateTime, sharedResultLines);
        }
    }
}