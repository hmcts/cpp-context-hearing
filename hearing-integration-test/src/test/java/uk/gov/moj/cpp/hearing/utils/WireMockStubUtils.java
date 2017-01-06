package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils.stubPingFor;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;

/**
 * Utility class for setting stubs.
 */
public class WireMockStubUtils {

    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
    private static final String CONTENT_TYPE = "application/json";

    static {
        configureFor(HOST, 8080);
        reset();
    }

    public static void setupAsAuthorisedUser(final String userId) {
        stubPingFor("usersgroups-query-api");

        stubFor(get(urlPathEqualTo(format("/usersgroups-query-api/query/api/rest/usersgroups/users/%s/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(getPayload("stub-data/usersgroups.get-groups-by-user.json"))));
    }

}
