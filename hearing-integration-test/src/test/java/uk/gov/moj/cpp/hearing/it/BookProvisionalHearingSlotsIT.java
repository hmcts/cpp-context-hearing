package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static uk.gov.moj.cpp.hearing.it.UseCases.bookHearingSlots;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("squid:S1607")
public class BookProvisionalHearingSlotsIT extends AbstractIT {

    //ignore until we find a solution to stub 3rd party services
    @Ignore
    @Test
    public void shouldBookProvisionalHearingSlots() throws Exception {
//        final UUID mockBookingId = UUID.randomUUID();
//        stubProvisionalHearingSlot(mockBookingId);
//        final ListStubMappingsResult listStubMappingsResult = listAllStubMappings();
        final UUID hearingId = UUID.randomUUID();
        final UUID courtSchedulingId1 = UUID.randomUUID();
        final UUID courtSchedulingId2 = UUID.randomUUID();

        final Utilities.EventListener eventListener = listenFor("public.hearing.hearing-slots-provisionally-booked", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(hasJsonPath("$.bookingId"));

        bookHearingSlots(getRequestSpec(), hearingId, Arrays.asList(courtSchedulingId1, courtSchedulingId2));

        eventListener.waitFor();
    }

}
