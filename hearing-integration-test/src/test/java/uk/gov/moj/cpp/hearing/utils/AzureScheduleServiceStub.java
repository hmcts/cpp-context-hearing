package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static javax.ws.rs.core.Response.Status.ACCEPTED;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

//we need to find a proper way to mock 3rd party services (https://tools.hmcts.net/jira/browse/GPE-13734)
public class AzureScheduleServiceStub {

    private static final String ROTA_SL_ENDPOINT_URL = "/provisionalBooking";
    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "fa-ste-ccm-scsl.azurewebsites.net");

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
}
