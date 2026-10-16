package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.time.ZonedDateTime.parse;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo.bookProvisionalHearingSlotsCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.bookHearingSlots;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.utils.CourtSchedulerStub.stubProvisionalBookSlots;
import static uk.gov.moj.cpp.hearing.utils.CourtSchedulerStub.stubProvisionalBookSlotsRefusedWith;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;

import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("squid:S1607")
public class BookProvisionalHearingSlotsIT extends AbstractIT {

    @BeforeEach
    public void setUp() {
        stubProvisionalBookSlots();
    }

    @Test
    public void shouldBookProvisionalHearingSlots() throws Exception {
        final UUID hearingId = randomUUID();
        final UUID courtSchedulingId1 = randomUUID();
        final UUID courtSchedulingId2 = randomUUID();
        final ZonedDateTime hearingStartTime1 = parse("2020-08-25T10:00:00.000Z");
        final ZonedDateTime hearingStartTime2 = parse("2020-08-27T11:00:00.000Z");

        final EventListener eventListener = listenFor("public.hearing.hearing-slots-provisionally-booked", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(hasJsonPath("$.bookingId"));

        bookHearingSlots(getRequestSpec(), hearingId, Arrays.asList(
                bookProvisionalHearingSlotsCommand().setCourtScheduleId(courtSchedulingId1).setHearingStartTime(hearingStartTime1),
                bookProvisionalHearingSlotsCommand().setCourtScheduleId(courtSchedulingId2).setHearingStartTime(hearingStartTime2)));

        eventListener.waitFor();
    }

    /**
     * A refusal must arrive under its OWN event name, carrying a code. Worth an IT rather than
     * only a unit test because the framework validates every published event against the schema
     * registered for its name in public-publications-descriptor.yaml - a payload the new schema
     * does not accept is dropped at publication time, which no unit test here would notice.
     *
     * <p>409 with a capacity message is the NO_CAPACITY path: the one refusal the UI may report
     * to the clerk as "pick another session".
     */
    @Test
    public void shouldPublishTheFailureEventWhenCourtSchedulerRefusesTheBooking() throws Exception {
        stubProvisionalBookSlotsRefusedWith(409, "Could not reserve sessions: no remaining capacity");

        final UUID hearingId = randomUUID();
        final UUID courtSchedulingId = randomUUID();
        final ZonedDateTime hearingStartTime = parse("2020-08-25T10:00:00.000Z");

        final EventListener eventListener =
                listenFor("public.hearing.hearing-slots-unconfirmed-booking-failed", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                        .withFilter(hasJsonPath("$.error.code", is("NO_CAPACITY")));

        bookHearingSlots(getRequestSpec(), hearingId, Arrays.asList(
                bookProvisionalHearingSlotsCommand().setCourtScheduleId(courtSchedulingId).setHearingStartTime(hearingStartTime)));

        eventListener.waitFor();
    }

}
