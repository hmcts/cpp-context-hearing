package uk.gov.moj.cpp.hearing.domain.event.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.result-amendments-cancelled")
public class ResultAmendmentsCancelled implements Serializable {
    private static final long serialVersionUID = 1L;
    private final  UUID hearingId;
    private final UUID userId;
    private final List<Target> latestSharedTargets;
    private final ZonedDateTime lastSharedDateTime;

    @JsonCreator
    public ResultAmendmentsCancelled(final UUID hearingId, final UUID userId, final List<Target> latestSharedTargets, final ZonedDateTime lastSharedDateTime) {
        this.hearingId = hearingId;
        this.userId = userId;
        this.latestSharedTargets = new ArrayList<>(latestSharedTargets);
        this.lastSharedDateTime = lastSharedDateTime;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<Target> getLatestSharedTargets() {
        return new ArrayList<>(latestSharedTargets);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }
}
