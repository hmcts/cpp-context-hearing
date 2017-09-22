package uk.gov.moj.cpp.hearing.utils;


import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

public class StructureStub {

    private static final String STRUCTURE_GET_CASE_QUERY_URL = "/structure-query-api/query/api/rest/structure/cases/.*";
    private static final String STRUCTURE_GET_CASE_MEDIA_TYPE = "application/vnd.structure.query.case+json";


    public static void stubForCaseDetails() {
        InternalEndpointMockUtils.stubPingFor("structure-query-api");

        stubFor(get(urlMatching(STRUCTURE_GET_CASE_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(ID, UUID.randomUUID().toString())
                        .withHeader(CONTENT_TYPE, STRUCTURE_GET_CASE_MEDIA_TYPE)
                        .withBody(getPayload("structure.query.case.json"))));

        waitForStubToBeReady(STRUCTURE_GET_CASE_QUERY_URL, STRUCTURE_GET_CASE_MEDIA_TYPE);
    }

}
