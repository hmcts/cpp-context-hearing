package uk.gov.moj.cpp.hearing.utils;


import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.it.TestUtilities;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

public class ReferenceDataStub {

    private static final String REFERENCE_DATA_SERVICE_NAME = "referencedata-service";


    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result/definition-keyword-synonyms";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/fixed-list";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-prompt-word-synonyms";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/nows-metadata";


    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_MEDIA_TYPE = "application/vnd.referencedata.result.get-all-definition-keyword-synonyms";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE = "application/vnd.referencedata.get-all-fixed-list+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-prompt-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE = "application/vnd.referencedata.get-all-now-metadata+json";


    public static void stubForReferenceDataResults() {
        stubGetReferenceDataResultDefinitions();
        stubGetReferenceDataResultDefinitionsKeywords();
        stubGetReferenceDataResultPromptWordSynonyms();
        stubGetReferenceDataResultPromptFixedLists();
    }

    public static void stubGetAllNowsMetaData(AllNows allNows) {
        stub(allNows, REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL, REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE);
    }

    public static void stubGetAllResultDefinitions(AllResultDefinitions allResultDefinitions) {
        stub(allResultDefinitions, REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE);
    }


    public static void stub(Object result, String queryUrl, String mediaType) {
        String strPayload;
        try {
            strPayload = TestUtilities.JsonUtil.toJsonString(result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        stubFor(get(urlPathEqualTo(queryUrl))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", mediaType)
                        .withBody(strPayload)));

        waitForStubToBeReady(queryUrl, mediaType);
    }


    private static void stubGetReferenceDataResultDefinitions() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result-definitions.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultDefinitionsKeywords() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result.definition-keyword-synonyms.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptFixedLists() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.fixed-list-collection.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL, REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptWordSynonyms() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result-prompt-word-synonyms.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL, REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE);
    }
}
