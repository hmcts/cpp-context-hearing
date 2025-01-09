package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

@Event("hearing.events.result-amendments-cancelled-v2")
public class ResultAmendmentsCancelledV2 implements Serializable {
    private static final long serialVersionUID = 1L;
    private final  UUID hearingId;
    private final LocalDate hearingDay;
    private final UUID userId;
    private final List<Target2> latestSharedTargets;
    private final ZonedDateTime lastSharedDateTime;

    @JsonCreator
    public ResultAmendmentsCancelledV2(final UUID hearingId, final LocalDate hearingDay, final UUID userId, final List<Target2> latestSharedTargets, final ZonedDateTime lastSharedDateTime) {
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.userId = userId;
        this.latestSharedTargets = new ArrayList<>(latestSharedTargets);
        this.lastSharedDateTime = lastSharedDateTime;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<Target2> getLatestSharedTargets() {
        return new ArrayList<>(latestSharedTargets);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }
}
