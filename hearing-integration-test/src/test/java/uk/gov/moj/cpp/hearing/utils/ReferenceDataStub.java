package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.text.MessageFormat.format;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.it.TestUtilities;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.UUID;

public class ReferenceDataStub {

    private static final String REFERENCE_DATA_SERVICE_NAME = "referencedata-service";

    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/withdrawn";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/next-hearing";
    private static final String REFERENCE_DATA_RESULT_WORD_SYNONYMS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-word-synonyms";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/fixed-list";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-prompt-word-synonyms";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/nows-metadata";


    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-withdrawn+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-next-hearing+json";
    private static final String REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE = "application/vnd.referencedata.get-all-fixed-list+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-prompt-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE = "application/vnd.referencedata.get-all-now-metadata+json";


    public static void stubForReferenceDataResults() {
        stubGetReferenceDataResultDefinitions();
        stubGetReferenceDataResultWordSynonyms();
        stubGetReferenceDataResultPromptWordSynonyms();
        stubGetReferenceDataResultPromptFixedLists();
    }

    public static void stubRelistReferenceDataResults() {
        stubGetReferenceDataResultDefinitionsWithdrawn();
        stubGetReferenceDataResultDefinitionsNextHearing();
    }

    private static void stubGetReferenceDataResultDefinitionsWithdrawn() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result-definitions-withdrawn.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultDefinitionsNextHearing() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result-definitions-next-hearing.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE);
    }

    public static void stubGetAllNowsMetaData(LocalDate referenceDate, AllNows allNows) {
        stub(allNows, REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL, REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE, referenceDate);
    }

    public static void stubGetAllResultDefinitions(LocalDate referenceDate, AllResultDefinitions allResultDefinitions) {
        stub(allResultDefinitions, REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE, referenceDate);
    }

    private static void stub(Object result, String queryUrl, String mediaType, LocalDate referenceDate) {
        String strPayload;
        try {
            strPayload = TestUtilities.JsonUtil.toJsonString(result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        stubFor(get(urlPathEqualTo(queryUrl))
                .withQueryParam("on", equalTo(referenceDate.toString()))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", mediaType)
                        .withBody(strPayload)));

        poll(requestParams(format("{0}/{1}?on={2}", getBaseUri(), queryUrl, referenceDate.toString()), mediaType).build())
                .until(status().is(Response.Status.OK));
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

    private static void stubGetReferenceDataResultWordSynonyms() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_WORD_SYNONYMS_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result-word-synonyms.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_WORD_SYNONYMS_QUERY_URL, REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE);
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
