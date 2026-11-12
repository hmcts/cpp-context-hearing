package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gives back the unconfirmed slot holds taken under these bookingIds. courtscheduler releases only
 * holds that no share has confirmed yet, so a booking a share has already turned into a listing is
 * left alone whatever this event names.
 */
@Event("hearing.event.release-provisional-hearing-slots")
public class ReleaseProvisionalHearingSlots implements Serializable {

    private static final long serialVersionUID = 2L;

    public enum Reason {
        /** A draft save, or the deletion of the draft, left no line carrying the booking. */
        DROPPED_FROM_DRAFT
    }

    private final UUID hearingId;

    private final List<String> bookingIds;

    private final Reason reason;

    @JsonCreator
    public ReleaseProvisionalHearingSlots(@JsonProperty("hearingId") final UUID hearingId,
                                          @JsonProperty("bookingIds") final List<String> bookingIds,
                                          @JsonProperty("reason") final Reason reason) {
        this.hearingId = hearingId;
        this.bookingIds = new ArrayList<>(bookingIds);
        this.reason = reason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<String> getBookingIds() {
        return new ArrayList<>(bookingIds);
    }

    public Reason getReason() {
        return reason;
    }
}
