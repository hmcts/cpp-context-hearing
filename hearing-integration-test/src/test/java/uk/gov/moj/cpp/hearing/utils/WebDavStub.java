package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.*;
import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.Status.*;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForPutStubToBeReady;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;

public class WebDavStub {

    public static final String XHIBIT_GATEWAY_SEND_TO_XHIBIT_PATH_REG_EX = "/xhibit-gateway/send-to-xhibit/.*\\.xml";

    public static void stubExhibitFileUpload() {
        stubFor(put(urlPathMatching(XHIBIT_GATEWAY_SEND_TO_XHIBIT_PATH_REG_EX))
                .willReturn(aResponse()
                        .withStatus(OK.getStatusCode())
                        .withHeader("CPPID", UUID.randomUUID().toString())));

        waitForPutStubToBeReady("/xhibit-gateway/send-to-xhibit/waitForPutStubToBeReady.xml", APPLICATION_XML, OK);
    }

    public static String getSentXml() {
        final List<LoggedRequest> putRequests = findAll(putRequestedFor(urlPathMatching(XHIBIT_GATEWAY_SEND_TO_XHIBIT_PATH_REG_EX)));
        final LoggedRequest loggedRequest = putRequests.get(putRequests.size() - 1);

        return loggedRequest.getBodyAsString();
    }
}
