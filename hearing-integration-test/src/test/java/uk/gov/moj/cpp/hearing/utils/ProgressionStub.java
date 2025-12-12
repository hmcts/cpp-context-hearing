package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.text.MessageFormat.format;
import static java.util.UUID.randomUUID;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;

import java.text.MessageFormat;
import java.util.UUID;

public class ProgressionStub {

    private static final String PROGRESSION_PROSECUTION_CASE_QUERY_URL = "/progression-service/query/api/rest/progression/prosecutioncases/{0}";
    private static final String PROGRESSION_PROSECUTION_CASE_MEDIA_TYPE = "application/vnd.progression.query.prosecutioncase+json";

    public static void stubGetProgressionProsecutionCases(final UUID caseId) {
        final String stringUrl = format(PROGRESSION_PROSECUTION_CASE_QUERY_URL, caseId);
        final String payload = getPayload("stub-data/progression.query.prosecutioncase.json");
        stubFor(get(urlPathEqualTo(stringUrl))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", PROGRESSION_PROSECUTION_CASE_MEDIA_TYPE)
                        .withBody(payload)));
    }

    public static void stubApplicationsByParentId(final UUID id) {
        final String stringUrl = MessageFormat.format("/progression-service/query/api/rest/progression/applications/{0}", id);
        final String payload = getPayload("stub-data/progression.query.application.summary.json");
        stubFor(get(urlPathEqualTo(stringUrl))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", "application/vnd.progression.query.application.summary+json")
                        .withBody(payload)));

    }
}
