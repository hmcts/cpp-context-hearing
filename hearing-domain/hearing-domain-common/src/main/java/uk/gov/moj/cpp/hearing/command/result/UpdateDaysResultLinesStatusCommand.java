package uk.gov.moj.cpp.hearing.command.result;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.DelegatedPowers;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class UpdateDaysResultLinesStatusCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final ZonedDateTime lastSharedDateTime;

    private final DelegatedPowers courtClerk;

    private final List<SharedResultLineId> sharedResultLines;

    private final LocalDate hearingDay;

    @JsonCreator
    protected UpdateDaysResultLinesStatusCommand(@JsonProperty("hearingId") final UUID hearingId,
                                                 @JsonProperty("courtClerk") final DelegatedPowers courtClerk,
                                                 @JsonProperty("lastSharedDateTime") final ZonedDateTime lastSharedDateTime,
                                                 @JsonProperty("sharedResultLines") final List<SharedResultLineId> sharedResultLines,
                                                 @JsonProperty("hearingDay") final LocalDate hearingDay) {
        this.hearingId = hearingId;
        this.lastSharedDateTime = lastSharedDateTime;
        this.courtClerk = courtClerk;
        this.sharedResultLines = unmodifiableList(ofNullable(sharedResultLines).orElseGet(ArrayList::new));
        this.hearingDay = hearingDay;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public DelegatedPowers getCourtClerk() {
        return courtClerk;
    }

    public List<SharedResultLineId> getSharedResultLines() {
        return sharedResultLines;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public static final class Builder {

        private UUID hearingId;

        private ZonedDateTime lastSharedDateTime;

        private DelegatedPowers courtClerk;

        private List<SharedResultLineId> sharedResultLines;

        private LocalDate hearingDay;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withSharedResultLines(final List<SharedResultLineId> sharedResultLines) {
            this.sharedResultLines = sharedResultLines;
            return this;
        }

        public Builder withLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public Builder withCourtClerk(final DelegatedPowers courtClerk) {
            this.courtClerk = courtClerk;
            return this;
        }

        public Builder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public UpdateDaysResultLinesStatusCommand build() {
            return new UpdateDaysResultLinesStatusCommand(this.hearingId, courtClerk, lastSharedDateTime, sharedResultLines, hearingDay);
        }

    }
}