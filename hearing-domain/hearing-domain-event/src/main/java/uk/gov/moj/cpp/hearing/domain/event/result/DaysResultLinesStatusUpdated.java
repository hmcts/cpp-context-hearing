package uk.gov.moj.cpp.hearing.domain.event.result;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.days-result-lines-status-updated")
public class DaysResultLinesStatusUpdated implements Serializable {

    private static final long serialVersionUID = 1553542790402775808L;

    private UUID hearingId;

    private List<SharedResultLineId> sharedResultLines;

    private ZonedDateTime lastSharedDateTime;

    private DelegatedPowers courtClerk;

    private LocalDate hearingDay;

    public DaysResultLinesStatusUpdated() {}

    @JsonCreator
    private DaysResultLinesStatusUpdated(@JsonProperty("hearingId") final UUID hearingId,
                                         @JsonProperty("lastSharedDateTime") final ZonedDateTime lastSharedDateTime,
                                         @JsonProperty("courtClerk") final DelegatedPowers courtClerk,
                                         @JsonProperty("sharedResultLines") final List<SharedResultLineId> sharedResultLines,
                                         @JsonProperty("hearingDay") final LocalDate hearingDay) {
        this.hearingId = hearingId;
        this.lastSharedDateTime = lastSharedDateTime;
        this.courtClerk = courtClerk;
        this.sharedResultLines = ofNullable(sharedResultLines).orElseGet(ArrayList::new);
        this.hearingDay = hearingDay;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public DelegatedPowers getCourtClerk() {
        return courtClerk;
    }

    public void setCourtClerk(final DelegatedPowers courtClerk) {
        this.courtClerk = courtClerk;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public void setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
        this.lastSharedDateTime = lastSharedDateTime;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public List<SharedResultLineId> getSharedResultLines() {
        return sharedResultLines;
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

        public Builder withLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
            this.lastSharedDateTime = lastSharedDateTime;
            return this;
        }

        public Builder withCourtClerk(final DelegatedPowers courtClerk) {
            this.courtClerk = courtClerk;
            return this;
        }

        public Builder withSharedResultLines(final List<SharedResultLineId> sharedResultLines) {
            this.sharedResultLines = sharedResultLines;
            return this;
        }

        public Builder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public DaysResultLinesStatusUpdated build() {
            return new DaysResultLinesStatusUpdated(
                    hearingId,
                    lastSharedDateTime,
                    courtClerk,
                    sharedResultLines,
                    hearingDay);
        }

    }
}