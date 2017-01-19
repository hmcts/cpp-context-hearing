package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.Objects;
import java.util.UUID;

@Event("hearing.hearing-event-deleted")
public class HearingEventDeleted {

    private final UUID hearingEventId;

    public HearingEventDeleted(final UUID hearingEventId) {
        this.hearingEventId = hearingEventId;
    }

    public UUID getHearingEventId() {
        return hearingEventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEventDeleted that = (HearingEventDeleted) o;
        return Objects.equals(getHearingEventId(), that.getHearingEventId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHearingEventId());
    }

    @Override
    public String toString() {
        return "HearingEventDeleted{" +
                "hearingEventId=" + hearingEventId +
                '}';
    }
}
