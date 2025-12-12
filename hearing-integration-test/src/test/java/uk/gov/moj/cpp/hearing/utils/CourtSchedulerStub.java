package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;

public class CourtSchedulerStub {

    private static final String COURT_SCHEDULER_BASE_URL = "/listingcourtscheduler-api/rest/courtscheduler";
    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");

    private static final String PROVISIONAL_BOOKING = "/provisionalBooking";

    public static final String STUB_DATA_PROVISIONAL_BOOKING_BOOK_SLOTS_RESPONSE = "stub-data/courtscheduler.provisionalbooking-book-slots-response.json";

    static {
        configureFor(HOST, 8080);
    }

    public static void stubProvisionalBookSlots() {
        stubFor(post(urlPathMatching(format("%s", COURT_SCHEDULER_BASE_URL + PROVISIONAL_BOOKING)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withBody(getPayload(STUB_DATA_PROVISIONAL_BOOKING_BOOK_SLOTS_RESPONSE))
                        .withHeader("Content-Type", "application/json")
                ));
    }


}
