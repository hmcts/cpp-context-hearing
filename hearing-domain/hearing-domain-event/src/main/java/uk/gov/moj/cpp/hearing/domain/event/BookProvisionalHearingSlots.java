package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.book-provisional-hearing-slots")
public class BookProvisionalHearingSlots implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final List<ProvisionalHearingSlotInfo> slots;

    private final String bookingType;

    private final String priority;

    private final List<String> specialRequirements;

    @JsonCreator
    public BookProvisionalHearingSlots(@JsonProperty("hearingId") final UUID hearingId,
                                       @JsonProperty("slots") final List<Object> slots,
                                       @JsonProperty("bookingType") final String bookingType,
                                       @JsonProperty("priority") final String priority,
                                       @JsonProperty("specialRequirements") final List<String> specialRequirements) {
        this.hearingId = hearingId;
        this.priority = priority;
        this.bookingType = bookingType;
        this.specialRequirements = nonNull(specialRequirements) ? new ArrayList<>(specialRequirements) : specialRequirements;
        this.slots = new ArrayList<>();
        if (isEmpty(slots)) {
            return;
        }

        slots.forEach(this::addSlotInfo);
    }

    private void addSlotInfo(final Object slot) {
        if (slot instanceof ProvisionalHearingSlotInfo) {
            this.slots.add((ProvisionalHearingSlotInfo) slot);
        }

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
        final ZonedDateTime hearingStartTime = nonNull(hearingStartTimeObject) ? ZonedDateTime.parse(hearingStartTimeObject.toString()) : null;
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

    public String getBookingType() {
        return bookingType;
    }

    public String getPriority() {
        return priority;
    }

    public List<String> getSpecialRequirements() {
        return isNotEmpty(specialRequirements) ? new ArrayList<>(specialRequirements) : null;
    }

    @SuppressWarnings({"PMD.BeanMembersShouldSerialize", "squid:S2384"})
    public static final class BookProvisionalHearingSlotsBuilder {
        private UUID hearingId;
        private List<Object> slots;
        private String bookingType;
        private String priority;
        private List<String> specialRequirements;

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

        public BookProvisionalHearingSlotsBuilder withBookingType(final String bookingType) {
            this.bookingType = bookingType;
            return this;
        }

        public BookProvisionalHearingSlotsBuilder withPriority(final String priority) {
            this.priority = priority;
            return this;
        }

        public BookProvisionalHearingSlotsBuilder withSpecialRequirements(final List<String> specialRequirements) {
            this.specialRequirements = specialRequirements;
            return this;
        }

        public BookProvisionalHearingSlots build() {
            return new BookProvisionalHearingSlots(hearingId, new ArrayList<>(slots), bookingType, priority, specialRequirements);
        }
    }

}
