package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.Objects;
import java.util.UUID;

@Event("hearing.hearing-event-deletion-ignored")
public class HearingEventDeletionIgnored {

    private final UUID hearingEventId;
    private final String reason;

    public HearingEventDeletionIgnored(final UUID hearingEventId, final String reason) {
        this.hearingEventId = hearingEventId;
        this.reason = reason;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEventDeletionIgnored that = (HearingEventDeletionIgnored) o;
        return Objects.equals(getHearingEventId(), that.getHearingEventId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHearingEventId());
    }

    @Override
    public String toString() {
        return "HearingEventDeletionIgnored{" +
                "hearingEventId=" + hearingEventId +
                ", reason='" + reason + '\'' +
                '}';
    }
}
