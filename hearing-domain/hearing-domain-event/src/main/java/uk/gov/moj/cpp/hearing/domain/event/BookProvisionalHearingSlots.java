package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;

@Event("hearing.event.book-provisional-hearing-slots")
public class BookProvisionalHearingSlots implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final List<ProvisionalHearingSlotInfo> slots;

    @JsonCreator
    public BookProvisionalHearingSlots(@JsonProperty("hearingId") final UUID hearingId,
                                       @JsonProperty("slots") final List<Object> slots) {
        this.hearingId = hearingId;
        this.slots = new ArrayList<>();
        if (CollectionUtils.isEmpty(slots)) {
            return;
        }

        slots.forEach(this::addSlotInfo);
    }

    private void addSlotInfo(final Object slot) {
        if (slot instanceof String) {
            final UUID slotUUID = UUID.fromString(slot.toString());
            final ProvisionalHearingSlotInfo provisionalHearingSlotInfo = new ProvisionalHearingSlotInfo().setCourtScheduleId(slotUUID);
            slots.add(provisionalHearingSlotInfo);
        }
        if (slot instanceof Map) {
            addSlotInfoFromMap((Map<String, Object>) slot);
        }
    }

    private void addSlotInfoFromMap(final Map<String, Object> slot) {
        final Map<String, Object> provisionalHearingSlotInfoMap = slot;
        final Object hearingStartTimeObject = provisionalHearingSlotInfoMap.get("hearingStartTime");
        final UUID courtScheduleIdUUID = UUID.fromString(provisionalHearingSlotInfoMap.get("courtScheduleId").toString());
        final ZonedDateTime hearingStartTime = Objects.nonNull(hearingStartTimeObject) ? ZonedDateTime.parse(hearingStartTimeObject.toString()) : null;
        final ProvisionalHearingSlotInfo provisionalHearingSlotInfo = new ProvisionalHearingSlotInfo().setCourtScheduleId(courtScheduleIdUUID).setHearingStartTime(hearingStartTime);
        slots.add(provisionalHearingSlotInfo);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<ProvisionalHearingSlotInfo> getSlots() {
        return slots;
    }

    public static BookProvisionalHearingSlotsBuilder bookProvisionalHearingSlots() {
        return new BookProvisionalHearingSlotsBuilder();
    }

    public static final class BookProvisionalHearingSlotsBuilder {
        private UUID hearingId;
        private List<Object> slots;

        private BookProvisionalHearingSlotsBuilder() {
        }

        public BookProvisionalHearingSlotsBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public BookProvisionalHearingSlotsBuilder withSlots(final List<Object> slots) {
            this.slots = new ArrayList<>(slots);
            return this;
        }

        public BookProvisionalHearingSlots build() {
            return new BookProvisionalHearingSlots(hearingId, new ArrayList<>(slots));
        }
    }

}
