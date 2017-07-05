package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.text.MessageFormat.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils.stubPingFor;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;

import java.time.LocalDate;

import javax.ws.rs.core.Response.Status;

import java.util.UUID;

/**
 * Utility class for setting stubs.
 */
public class WireMockStubUtils {

    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
    private static final String CONTENT_TYPE = "application/json";
    private static final String MEDIA_TYPE_QUERY_GROUPS = "application/vnd.usersgroups.groups+json";

    static {
        configureFor(HOST, 8080);
        reset();
    }

    public static void setupAsAuthorisedUser(final UUID userId) {
        stubPingFor("usersgroups-query-api");

        stubFor(get(urlPathEqualTo(format("/usersgroups-query-api/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader("Content-Type", CONTENT_TYPE)
                        .withBody(getPayload("stub-data/usersgroups.get-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-query-api/query/api/rest/usersgroups/users/{0}/groups", userId), MEDIA_TYPE_QUERY_GROUPS);
    }

    protected static void waitForStubToBeReady(final String resource, final String mediaType) {
        waitForStubToBeReady(resource, mediaType, Status.OK);
    }

    /* TODO
    public static String getJsonBodyStr(final String path, final String caseId,
                                        final String defendantId, final String defendant2Id, final String caseProgressionId) {
        final String payload = getPayload(path);
        return payload.replace("RANDOM_ID", caseProgressionId).replace("RANDOM_CASE_ID", caseId)
                .replace("DEF_ID_1", defendantId)
                .replace("DEF_ID_2", defendant2Id)
                .replace("DEF_PRG_ID", defendantId)
                .replace("TODAY", LocalDate.now().toString());
    }
    */

    private static void waitForStubToBeReady(final String resource, final String mediaType, final Status expectedStatus) {
        poll(requestParams(format("{0}/{1}", getBaseUri(), resource), mediaType)).until(status().is(expectedStatus));
    }


}
