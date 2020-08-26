package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

//we need to find a proper way to mock 3rd party services (https://tools.hmcts.net/jira/browse/GPE-13734)
public class AzureScheduleServiceStub {

    private static final String ROTA_SL_ENDPOINT_URL = "/fa-ste-ccm-scsl";
    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");

    private static final String PROVISIONAL_BOOKING = "/provisionalBooking";

    public static final String STUB_DATA_PROVISIONAL_BOOKING_BOOK_SLOTS_RESPONSE = "stub-data/rotasl.provisionalbooking-book-slots-response.json";

    static {
        configureFor(HOST, 8080);
    }

    public static void stubProvisionalHearingSlot(final UUID mockBookingId) {
        final JsonObject bookingId = Json.createObjectBuilder().add("bookingId", mockBookingId.toString()).build();

        stubFor(post(urlPathMatching(ROTA_SL_ENDPOINT_URL))
                .withHeader("Ocp-Apim-Subscription-Key", equalTo("5008a9d7baff47aa86210b3a9c6efd3d"))
                .willReturn(aResponse().withStatus(ACCEPTED.getStatusCode())
                        .withBody(bookingId.toString())));

    }

    public static void stubProvisionalBookSlots() {
        stubFor(post(urlPathMatching(format("%s", ROTA_SL_ENDPOINT_URL + PROVISIONAL_BOOKING)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withBody(getPayload(STUB_DATA_PROVISIONAL_BOOKING_BOOK_SLOTS_RESPONSE))
                        .withHeader("Content-Type", "application/json")
                ));
    }
}
