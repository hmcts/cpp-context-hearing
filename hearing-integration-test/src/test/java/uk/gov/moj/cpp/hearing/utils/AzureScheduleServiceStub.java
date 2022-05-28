package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;

//we need to find a proper way to mock 3rd party services (https://tools.hmcts.net/jira/browse/GPE-13734)
public class AzureScheduleServiceStub {

    private static final String ROTA_SL_ENDPOINT_URL = "/fa-ste-ccm-scsl";
    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");

    private static final String PROVISIONAL_BOOKING = "/provisionalBooking";

    public static final String STUB_DATA_PROVISIONAL_BOOKING_BOOK_SLOTS_RESPONSE = "stub-data/rotasl.provisionalbooking-book-slots-response.json";

    static {
        configureFor(HOST, 8080);
    }

    public static void stubProvisionalBookSlots() {
        stubFor(post(urlPathMatching(format("%s", ROTA_SL_ENDPOINT_URL + PROVISIONAL_BOOKING)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withBody(getPayload(STUB_DATA_PROVISIONAL_BOOKING_BOOK_SLOTS_RESPONSE))
                        .withHeader("Content-Type", "application/json")
                ));
    }


}
