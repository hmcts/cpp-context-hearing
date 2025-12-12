package uk.gov.moj.cpp.hearing.event.model;

import java.time.LocalDate;
import java.util.UUID;

public class ExtendedCustodyTimeLimit {

    private UUID hearingId;
    private UUID offenceId;
    private LocalDate extendedTimeLimit;

    public ExtendedCustodyTimeLimit() {
    }

    public ExtendedCustodyTimeLimit(final UUID hearingId, final UUID offenceId, final LocalDate extendedTimeLimit) {
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.extendedTimeLimit = extendedTimeLimit;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public void setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
    }

    public LocalDate getExtendedTimeLimit() {
        return extendedTimeLimit;
    }

    public void setExtendedTimeLimit(LocalDate extendedTimeLimit) {
        this.extendedTimeLimit = extendedTimeLimit;
    }
}
