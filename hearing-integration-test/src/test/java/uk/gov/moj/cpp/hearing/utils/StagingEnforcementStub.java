package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.jayway.restassured.path.json.JsonPath.from;
import static java.util.UUID.randomUUID;
import static org.apache.http.HttpStatus.SC_ACCEPTED;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;

public class StagingEnforcementStub {

    private static final String SERVICE_NAME = "stagingenforcement-service";
    private static final String ENFORCE_FINANCIAL_IMPOSITION_PATH = "/" + SERVICE_NAME + "/command/api/rest/stagingenforcement/enforce-financial-imposition";


    public static void stubEnforceFinancialImposition() {


        stubFor(post(urlPathEqualTo(ENFORCE_FINANCIAL_IMPOSITION_PATH))
                .willReturn(aResponse().withStatus(SC_ACCEPTED)
                        .withHeader("CPPID", randomUUID().toString())
                ));
    }

    public static boolean requestIssuedForStagingEnforcementForNowsId(final List<String> requestIds) {
        final List<LoggedRequest> all = findAll(
                postRequestedFor(urlPathMatching(ENFORCE_FINANCIAL_IMPOSITION_PATH))
        );
        final List<String> actualRequestIds = all.stream()
                .map(request -> from(request.getBodyAsString()).getString("requestId"))
                .collect(Collectors.toList()
                );

        return (actualRequestIds.containsAll(requestIds));
    }
}
