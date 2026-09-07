package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.path.json.JsonPath.from;
import static org.apache.http.HttpStatus.SC_OK;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.restassured.path.json.JsonPath;

public class ResultsValidatorStub {

    private static final String VALIDATE_PATH = "/results-validator/api/validation/validate";

    public static void stubResultsValidatorValidate() {
        stubFor(post(urlPathEqualTo(VALIDATE_PATH))
                .withHeader("CJSCPPUID", matching(".+"))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" +
                                "\"validationId\":\"11111111-1111-1111-1111-111111111111\"," +
                                "\"timestamp\":\"2026-01-01T00:00:00Z\"," +
                                "\"mode\":\"advisory\"," +
                                "\"rulesEvaluated\":[]," +
                                "\"isValid\":true," +
                                "\"errors\":{\"errorMessages\":[],\"validationIssues\":[]}," +
                                "\"warnings\":[]," +
                                "\"processingTimeMs\":0}")));
    }

    public static void stubResultsValidatorValidateWithErrors() {
        stubFor(post(urlPathEqualTo(VALIDATE_PATH))
                .withHeader("CJSCPPUID", matching(".+"))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" +
                                "\"validationId\":\"22222222-2222-2222-2222-222222222222\"," +
                                "\"timestamp\":\"2026-01-01T00:00:00Z\"," +
                                "\"mode\":\"advisory\"," +
                                "\"rulesEvaluated\":[\"DR-SENT-001\"]," +
                                "\"isValid\":false," +
                                "\"errors\":{" +
                                    "\"errorMessages\":[\"Sentence result is invalid for offence\"]," +
                                    "\"validationIssues\":[{" +
                                        "\"ruleId\":\"DR-SENT-001\"," +
                                        "\"severity\":\"ERROR\"," +
                                        "\"affectedResultCodes\":[]," +
                                        "\"affectedOffences\":[{" +
                                            "\"offenceId\":\"11111111-1111-1111-1111-111111111111\"," +
                                            "\"offenceTitle\":\"Theft\"," +
                                            "\"message\":\"Sentence result is invalid for offence\"}]," +
                                        "\"affectedDefendants\":[]," +
                                        "\"validationLevel\":\"OFFENCE\"}]}," +
                                "\"warnings\":[]," +
                                "\"processingTimeMs\":0}")));
    }

    public static List<JsonPath> capturedValidationRequests() {
        final List<LoggedRequest> requests = findAll(postRequestedFor(urlPathEqualTo(VALIDATE_PATH)));
        return requests.stream()
                .map(request -> from(request.getBodyAsString()))
                .collect(Collectors.toList());
    }
}
