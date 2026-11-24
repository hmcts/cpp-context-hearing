package uk.gov.moj.cpp.hearing.domain.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots.Reason.DROPPED_FROM_DRAFT;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReleaseProvisionalHearingSlotsTest {

    private static final String STORED_PAYLOAD = """
            {"hearingId":"9a72a84b-30e3-47d9-b4bf-87d730d29d08",
             "bookingIds":["4b778972-0d5e-4c3a-9a51-2f4f6f6f2a10"],
             "reason":"DROPPED_FROM_DRAFT"}""";

    @Test
    void shouldKeepTheEventNameWrittenByReserveASlot() {
        assertThat(ReleaseProvisionalHearingSlots.class.getAnnotation(Event.class).value(),
                is("hearing.event.release-provisional-hearing-slots"));
    }

    @Test
    void shouldReadAStoredPayloadWithTheFrameworkObjectMapper() throws Exception {
        final ReleaseProvisionalHearingSlots event = new ObjectMapperProducer().objectMapper()
                .readValue(STORED_PAYLOAD, ReleaseProvisionalHearingSlots.class);

        assertThat(event.getHearingId(), is(UUID.fromString("9a72a84b-30e3-47d9-b4bf-87d730d29d08")));
        assertThat(event.getBookingIds(), contains("4b778972-0d5e-4c3a-9a51-2f4f6f6f2a10"));
        assertThat(event.getReason(), is(DROPPED_FROM_DRAFT));
    }
}
