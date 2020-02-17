package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

public class AuthorisationServiceStub {

    private static final String CAPABILITY_ENABLEMENT_QUERY_URL = "/authorisation-service-server/rest/capabilities/%s";
    private static final String CAPABILITY_ENABLEMENT_QUERY_MEDIA_TYPE = "application/vnd.authorisation.capability+json";

    public static void stubSetStatusForCapability(String capabilityName, boolean statusToReturn) {
        String url = format(CAPABILITY_ENABLEMENT_QUERY_URL, capabilityName);
        stubEnableCapabilities(url, statusToReturn);
    }

    public static void stubEnableAllCapabilities() {
        String url = format(CAPABILITY_ENABLEMENT_QUERY_URL, ".*");
        stubEnableCapabilities(url, true);
    }

    private static void stubEnableCapabilities(String stubUrl, boolean statusToReturn) {
        String responsePayload = createObjectBuilder().add("enabled", statusToReturn).build().toString();

        stubFor(get(urlMatching(stubUrl))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(responsePayload)));

        waitForStubToBeReady(stubUrl, CAPABILITY_ENABLEMENT_QUERY_MEDIA_TYPE);
    }
}
