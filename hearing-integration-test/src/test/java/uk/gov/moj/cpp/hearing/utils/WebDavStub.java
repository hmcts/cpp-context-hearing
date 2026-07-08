package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static javax.ws.rs.core.Response.Status.OK;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.awaitility.Awaitility;

import static org.awaitility.Durations.FIVE_MINUTES;

public class WebDavStub {

    private static final String XHIBIT_GATEWAY_SEND_TO_XHIBIT_PATH_REG_EX = "/xhibit-gateway/send-to-xhibit/.*\\.xml";
    private static final String XHIBIT_GATEWAY_SEND_PUB_DISP_TO_XHIBIT_FILE_PATH_REG_EX = "/xhibit-gateway/send-to-xhibit/PublicDisplay.*\\.xml";

    public static void stubExhibitFileUpload() {
        stubFor(put(urlPathMatching(XHIBIT_GATEWAY_SEND_TO_XHIBIT_PATH_REG_EX))
                .willReturn(aResponse()
                        .withStatus(OK.getStatusCode())
                        .withHeader("CPPID", UUID.randomUUID().toString())));
    }

    public static String getSentXmlForPubDisplay() {
        return getFileForPath(XHIBIT_GATEWAY_SEND_PUB_DISP_TO_XHIBIT_FILE_PATH_REG_EX);
    }

    public static int getPutRequestCount(final String filePath) {
        return findAll(putRequestedFor(urlPathMatching(filePath))).size();
    }

    public static int getWebPagePutRequestCount() {
        return getPutRequestCount(XHIBIT_GATEWAY_SEND_TO_XHIBIT_PATH_REG_EX);
    }

    public static int getPublicDisplayPutRequestCount() {
        return getPutRequestCount(XHIBIT_GATEWAY_SEND_PUB_DISP_TO_XHIBIT_FILE_PATH_REG_EX);
    }

    public static String getFileForPath(final String filePath) {
        final List<LoggedRequest> putRequests = findAll(putRequestedFor(urlPathMatching(filePath)));
        final LoggedRequest loggedRequest = putRequests.get(putRequests.size() - 1);

        return loggedRequest.getBodyAsString();
    }

    public static String getFileForPathSince(final String filePath, final int requestsBefore) {
        final List<LoggedRequest> putRequests = findAll(putRequestedFor(urlPathMatching(filePath)));
        if (putRequests.size() <= requestsBefore) {
            throw new IllegalStateException(String.format(
                    "Expected a new request matching %s, but request count is still %d",
                    filePath, putRequests.size()));
        }
        return putRequests.get(requestsBefore).getBodyAsString();
    }

    public static String awaitFileForPathSinceContaining(final String filePath, final int requestsBefore, final String expectedContent) {
        return Awaitility.await()
                .atMost(FIVE_MINUTES)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> findFileForPathSinceContaining(filePath, requestsBefore, expectedContent), Objects::nonNull);
    }

    public static String getSentXmlForPubDisplaySince(final int requestsBefore) {
        return getFileForPathSince(XHIBIT_GATEWAY_SEND_PUB_DISP_TO_XHIBIT_FILE_PATH_REG_EX, requestsBefore);
    }

    public static String awaitSentXmlForPubDisplaySinceContaining(final int requestsBefore, final String expectedContent) {
        return awaitFileForPathSinceContaining(XHIBIT_GATEWAY_SEND_PUB_DISP_TO_XHIBIT_FILE_PATH_REG_EX, requestsBefore, expectedContent);
    }

    public static String awaitFileForPathSinceContainingAll(final String filePath, final int requestsBefore, final String... expectedContents) {
        return Awaitility.await()
                .atMost(FIVE_MINUTES)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> findFileForPathSinceContainingAll(filePath, requestsBefore, expectedContents), Objects::nonNull);
    }

    public static String awaitSentXmlForPubDisplaySinceContainingAll(final int requestsBefore, final String... expectedContents) {
        return awaitFileForPathSinceContainingAll(XHIBIT_GATEWAY_SEND_PUB_DISP_TO_XHIBIT_FILE_PATH_REG_EX, requestsBefore, expectedContents);
    }

    private static String findFileForPathSinceContainingAll(final String filePath, final int requestsBefore, final String... expectedContents) {
        final List<LoggedRequest> putRequests = findAll(putRequestedFor(urlPathMatching(filePath)));
        for (int index = requestsBefore; index < putRequests.size(); index++) {
            final String body = putRequests.get(index).getBodyAsString();
            if (java.util.Arrays.stream(expectedContents).allMatch(body::contains)) {
                return body;
            }
        }
        return null;
    }

    private static String findFileForPathSinceContaining(final String filePath, final int requestsBefore, final String expectedContent) {
        final List<LoggedRequest> putRequests = findAll(putRequestedFor(urlPathMatching(filePath)));
        for (int index = requestsBefore; index < putRequests.size(); index++) {
            final String body = putRequests.get(index).getBodyAsString();
            if (body.contains(expectedContent)) {
                return body;
            }
        }
        return null;
    }
}
