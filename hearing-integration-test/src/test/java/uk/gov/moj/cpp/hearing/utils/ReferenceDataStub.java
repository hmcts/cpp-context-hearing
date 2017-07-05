package uk.gov.moj.cpp.hearing.utils;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady ;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

import java.util.UUID;

public class ReferenceDataStub {

    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL = "/referencedata-query-api/query/api/rest/referencedata/result/definitions";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL = "/referencedata-query-api/query/api/rest/referencedata/result/definition-keyword-synonyms";
    private static final String REFERENCE_DATA_RESULT_PROMPTS_QUERY_URL = "/referencedata-query-api/query/api/rest/referencedata/result/prompts";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL = "/referencedata-query-api/query/api/rest/referencedata/result/prompt-fixedlists";
    private static final String REFERENCE_DATA_RESULT_PROMPTS_KEYWORDS_QUERY_URL = "/referencedata-query-api/query/api/rest/referencedata/result/prompt-keyword-synonyms";


    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE = "application/vnd.referencedata.result.get-all-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_MEDIA_TYPE = "application/vnd.referencedata.result.get-all-definition-keyword-synonyms";
    private static final String REFERENCE_DATA_RESULT_PROMPTS_MEDIA_TYPE = "application/vnd.referencedata.result.get-all-prompts+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE = "application/vnd.referencedata.result.get-all-prompt-fixedlists+json";
    private static final String REFERENCE_DATA_RESULT_PROMPTS_KEYWORDS_MEDIA_TYPE = "application/vnd.referencedata.result.get-all-prompt-keyword-synonyms+json";


    public static void stubForReferenceDataResults(){
        stubGetReferenceDataResultDefinitions();
        stubGetReferenceDataResultDefinitionsKeywords();
        stubGetReferenceDataResultPrompts();
        stubGetReferenceDataResultPromptsKeywords();
        stubGetReferenceDataResultPromptFixedLists();
    }


    private static void stubGetReferenceDataResultDefinitions() {
        InternalEndpointMockUtils.stubPingFor("referencedata-query-api");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result.definitions.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultDefinitionsKeywords() {
        InternalEndpointMockUtils.stubPingFor("referencedata-query-api");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result.definition-keyword-synonyms.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPrompts() {
        InternalEndpointMockUtils.stubPingFor("referencedata-query-api");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_PROMPTS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPTS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result.prompts.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_PROMPTS_QUERY_URL, REFERENCE_DATA_RESULT_PROMPTS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptFixedLists() {
        InternalEndpointMockUtils.stubPingFor("referencedata-query-api");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result.prompt-fixedlists.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL, REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptsKeywords() {
        InternalEndpointMockUtils.stubPingFor("referencedata-query-api");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_PROMPTS_KEYWORDS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPTS_KEYWORDS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result.prompt-keyword-synonyms.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_PROMPTS_KEYWORDS_QUERY_URL, REFERENCE_DATA_RESULT_PROMPTS_KEYWORDS_MEDIA_TYPE);
    }
}
