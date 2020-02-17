package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import com.github.tomakehurst.wiremock.client.WireMock;
import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProgressionStub {

    public static final String COMMAND_URL = "/progression-service/command/api/rest/progression/nows";
    public static final String COMMAND_MEDIA_TYPE = "application/vnd.progression.generate-nows+json";

    public static void stubProgressionGenerateNows() {

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

}
