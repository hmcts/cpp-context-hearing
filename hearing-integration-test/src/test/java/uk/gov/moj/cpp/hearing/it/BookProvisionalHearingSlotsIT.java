package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.time.ZonedDateTime.parse;
import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo.bookProvisionalHearingSlotsCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.bookHearingSlots;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.utils.CourtSchedulerStub.stubProvisionalBookSlots;
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

}
