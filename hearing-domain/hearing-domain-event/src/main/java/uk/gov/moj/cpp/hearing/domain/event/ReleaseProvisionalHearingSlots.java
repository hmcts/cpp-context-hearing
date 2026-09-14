package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.release-provisional-hearing-slots")
public class ReleaseProvisionalHearingSlots implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final String bookingId;

    @JsonCreator
    public ReleaseProvisionalHearingSlots(@JsonProperty("hearingId") final UUID hearingId,
                                          @JsonProperty("bookingId") final String bookingId) {
        this.hearingId = hearingId;
        this.bookingId = bookingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getBookingId() {
        return bookingId;
    }
}
