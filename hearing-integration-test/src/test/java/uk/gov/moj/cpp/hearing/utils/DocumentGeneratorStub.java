package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.List;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;

public class DocumentGeneratorStub {

    public static final String PATH = "/system-documentgenerator-api/rest/documentgenerator/*";

    public static void stubDocumentCreate(String documentText) {

        stubFor(post(urlPathMatching(PATH ))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withBody(documentText.getBytes())
                        .withHeader(CONTENT_TYPE, "application/vnd.system-documentgenerator.render+json")));
    }

    public static void verifyCreate(List<String> expectedValues) {

        RequestPatternBuilder requestPatternBuilder = postRequestedFor(urlPathMatching(PATH ));
        expectedValues.forEach(
                expectedValue -> requestPatternBuilder.withRequestBody(containing(expectedValue))
        );
        verify(requestPatternBuilder);
    }
}
