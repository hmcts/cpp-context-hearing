package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.book-provisional-hearing-slots")
public class BookProvisionalHearingSlots implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final List<ProvisionalHearingSlotInfo> slots;

    @JsonCreator
    public BookProvisionalHearingSlots(@JsonProperty("hearingId") final UUID hearingId,
                                       @JsonProperty("slots") final List<ProvisionalHearingSlotInfo> slots) {
        this.hearingId = hearingId;
        this.slots = new ArrayList<>(slots);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<ProvisionalHearingSlotInfo> getSlots() {
        return new ArrayList(slots);
    }

    public static BookProvisionalHearingSlotsBuilder bookProvisionalHearingSlots() {
        return new BookProvisionalHearingSlotsBuilder();
    }

    public static final class BookProvisionalHearingSlotsBuilder {
        private UUID hearingId;
        private List<ProvisionalHearingSlotInfo> slots;

        private BookProvisionalHearingSlotsBuilder() {
        }

        public BookProvisionalHearingSlotsBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public BookProvisionalHearingSlotsBuilder withSlots(final List<ProvisionalHearingSlotInfo> slots) {
            this.slots = new ArrayList(slots);
            return this;
        }

        public BookProvisionalHearingSlots build() {
            return new BookProvisionalHearingSlots(hearingId, slots);
        }
    }

}
