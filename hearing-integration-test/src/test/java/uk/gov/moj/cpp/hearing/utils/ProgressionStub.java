package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.text.MessageFormat.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

import com.github.tomakehurst.wiremock.client.WireMock;
import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProgressionStub {

    private static final String PROGRESSION_SERVICE_NAME = "progression-service";

    public static final String COMMAND_URL = "/progression-service/command/api/rest/progression/nows";
    public static final String COMMAND_MEDIA_TYPE = "application/vnd.progression.generate-nows+json";

    private static final String PROGRESSION_PROSECUTION_CASE_QUERY_URL = "/progression-service/query/api/rest/progression/prosecutioncases/{0}";
    private static final String PROGRESSION_PROSECUTION_CASE_MEDIA_TYPE = "application/vnd.progression.query.prosecutioncase+json";

    public static void stubProgressionGenerateNows() {
        InternalEndpointMockUtils.stubPingFor(PROGRESSION_SERVICE_NAME);

        stubFor(post(urlPathMatching(COMMAND_URL + ".*"))
                .withHeader(CONTENT_TYPE, equalTo(COMMAND_MEDIA_TYPE))
                .willReturn(aResponse().withStatus(ACCEPTED.getStatusCode())
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)));
    }

    public static List<String> findNowsRequestStringsByHearingId(final UUID hearingId) {
        return WireMock.findAll(postRequestedFor(urlPathMatching(COMMAND_URL + ".*"))
                .withRequestBody(containing(hearingId.toString()))).stream()
                .map(request -> request.getBodyAsString())
                .collect(Collectors.toList());
    }

    public static void stubGetProgressionProsecutionCases(final UUID caseId) {
        InternalEndpointMockUtils.stubPingFor(PROGRESSION_SERVICE_NAME);

        final String stringUrl = format(PROGRESSION_PROSECUTION_CASE_QUERY_URL, caseId);
        final String payload = getPayload("stub-data/progression.query.prosecutioncase.json");
        stubFor(get(urlPathEqualTo(stringUrl))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", PROGRESSION_PROSECUTION_CASE_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(stringUrl, PROGRESSION_PROSECUTION_CASE_MEDIA_TYPE);
    }
}
