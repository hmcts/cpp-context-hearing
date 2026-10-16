package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;

public class CourtSchedulerStub {

    private static final String COURT_SCHEDULER_BASE_URL = "/listingcourtscheduler-api/rest/courtscheduler";
    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");

    private static final String UNCONFIRMED_BOOKING = "/unconfirmedBooking";

    public static final String STUB_DATA_UNCONFIRMED_BOOKING_BOOK_SLOTS_RESPONSE = "stub-data/courtscheduler.provisionalbooking-book-slots-response.json";

    static {
        configureFor(HOST, 8080);
    }

    public static void stubProvisionalBookSlots() {
        stubFor(post(urlPathMatching(format("%s", COURT_SCHEDULER_BASE_URL + UNCONFIRMED_BOOKING)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withBody(getPayload(STUB_DATA_UNCONFIRMED_BOOKING_BOOK_SLOTS_RESPONSE))
                        .withHeader("Content-Type", "application/json")
                ));
    }

    /**
     * courtscheduler refusing the reservation, in the exact shape its GlobalExceptionHandler
     * produces: a status plus {@code {"error":"<message>"}}. The status and message together are
     * what ProvisionalBookingService turns into a failure code, so a stub that invented its own
     * shape would prove nothing - it would exercise a mapping that never runs in production.
     *
     * <p>Registered after {@link #stubProvisionalBookSlots()} in a test that wants a refusal:
     * WireMock prefers the most recently added matching stub.
     */
    public static void stubProvisionalBookSlotsRefusedWith(final int status, final String message) {
        stubFor(post(urlPathMatching(format("%s", COURT_SCHEDULER_BASE_URL + UNCONFIRMED_BOOKING)))
                .willReturn(aResponse().withStatus(status)
                        .withBody(format("{\"error\":\"%s\"}", message))
                        .withHeader("Content-Type", "application/json")
                ));
    }


}
