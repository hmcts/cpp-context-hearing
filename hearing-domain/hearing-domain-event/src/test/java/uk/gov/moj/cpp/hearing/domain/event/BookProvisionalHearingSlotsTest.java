package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class BookProvisionalHearingSlotsTest {

    @Test
    public void shouldPreserveDurationWhenSlotsArriveAsMaps() {
        final UUID courtScheduleId = randomUUID();
        final Map<String, Object> slotAsMap = Map.of(
                "courtScheduleId", courtScheduleId.toString(),
                "hearingStartTime", "2026-10-14T10:00:00.000Z",
                "duration", 90);

        final BookProvisionalHearingSlots event = new BookProvisionalHearingSlots(
                randomUUID(), List.of(slotAsMap), null, null, null, null);

        assertThat(event.getSlots().get(0).getDuration(), is(90));
    }

    @Test
    public void shouldTolerateSlotMapsWithNoDuration() {
        final UUID courtScheduleId = randomUUID();
        final Map<String, Object> slotAsMap = Map.of(
                "courtScheduleId", courtScheduleId.toString(),
                "hearingStartTime", "2026-10-14T10:00:00.000Z");

        final BookProvisionalHearingSlots event = new BookProvisionalHearingSlots(
                randomUUID(), List.of(slotAsMap), null, null, null, null);

        assertThat(event.getSlots().get(0).getDuration(), is(nullValue()));
    }

    @Test
    public void shouldCarryBookingIdThroughTheEvent() {
        final BookProvisionalHearingSlots event = BookProvisionalHearingSlots.bookProvisionalHearingSlots()
                .withHearingId(randomUUID())
                .withSlots(List.of())
                .withBookingId("existing-booking-1")
                .build();

        assertThat(event.getBookingId(), is("existing-booking-1"));
    }

    @Test
    public void shouldLeaveBookingIdNullWhenNotSupplied() {
        final BookProvisionalHearingSlots event = BookProvisionalHearingSlots.bookProvisionalHearingSlots()
                .withHearingId(randomUUID())
                .withSlots(List.of())
                .build();

        assertThat(event.getBookingId(), is(nullValue()));
    }
}
