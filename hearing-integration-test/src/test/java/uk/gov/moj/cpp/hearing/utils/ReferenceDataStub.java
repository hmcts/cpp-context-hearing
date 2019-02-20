package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.OuCourtRoomsResult;
import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.it.Utilities;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.UUID;

import javax.ws.rs.core.Response;

public class ReferenceDataStub {

    private static final String REFERENCE_DATA_SERVICE_NAME = "referencedata-service";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions?on=%s";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITHOUT_DATE = "/referencedata-service/query/api/rest/referencedata/result-definitions";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-word-synonyms?on=%s";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/fixed-list?on=%s";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-prompt-word-synonyms?on=%s";

    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/withdrawn";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/next-hearing";
    private static final String REFERENCE_DATA_RESULT_WORD_SYNONYMS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-word-synonyms";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/nows-metadata";
    private static final String REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/organisation-units";
    private static final String REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/enforcement-area";


    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-withdrawn+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-next-hearing+json";
    private static final String REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE = "application/vnd.referencedata.get-all-fixed-list+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-prompt-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE = "application/vnd.referencedata.get-all-now-metadata+json";
    private static final String REFERENCE_DATA_RESULT_ORGANISATION_UNIT_MEDIA_TYPE = "application/vnd.referencedata.query.organisation-unit.v2+json";
    private static final String REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_MEDIA_TYPE = "application/vnd.referencedata.query.enforcement-area+json";


    public static void stubForReferenceDataResults() {
        stubGetReferenceDataResultDefinitionsForFirstDay();
        stubGetReferenceDataResultDefinitionsKeywordsForFirstDay();
        stubGetReferenceDataResultPromptWordSynonymsForFirstDay();
        stubGetReferenceDataResultPromptFixedListsForFirstDay();

        stubGetReferenceDataResultDefinitionsForSecondDay();
        stubGetReferenceDataResultDefinitionsKeywordsForSecondDay();
        stubGetReferenceDataResultPromptWordSynonymsForSecondDay();
        stubGetReferenceDataResultPromptFixedListsForSecondDay();

        stubDynamicPromptFixedList();
    }

    private static void stubGetReferenceDataResultDefinitionsForFirstDay() {
        stubGetReferenceDataResultDefinitions(LocalDate.now(), "referencedata.result-definitions.json");
    }

    private static void stubGetReferenceDataResultDefinitionsKeywordsForFirstDay() {
        stubGetReferenceDataResultDefinitionsKeywords(LocalDate.now(), "referencedata.result-word-synonyms.json");
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
        stub(allNows, REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL, REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE, "on", referenceDate.toString());
    }

    public static void stubGetAllResultDefinitions(LocalDate referenceDate, AllResultDefinitions allResultDefinitions) {
        stub(allResultDefinitions, REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITHOUT_DATE, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE, referenceDate);
    }

    public static void stub(final OrganisationalUnit organisationalUnit) {

        stub(organisationalUnit, REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL + "/" + organisationalUnit.getId(),
                REFERENCE_DATA_RESULT_ORGANISATION_UNIT_MEDIA_TYPE);
    }

    public static void stub(final EnforcementArea enforcementArea, String ouCode) {
        stub(enforcementArea, REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_QUERY_URL, REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_MEDIA_TYPE, "localJusticeAreaNationalCourtCode", ouCode);
    }

    private static void stub(Object result, String queryUrl, String mediaType) {
        String strPayload;
        try {
            strPayload = Utilities.JsonUtil.toJsonString(result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        stubFor(get(urlPathEqualTo(queryUrl))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", mediaType)
                        .withBody(strPayload)));

        poll(requestParams(MessageFormat.format("{0}/{1}", getBaseUri(), queryUrl), mediaType).build())
                .until(status().is(Response.Status.OK));
    }

    private static void stub(Object result, String queryUrl, String mediaType, LocalDate referenceDate) {
        final String strValue = referenceDate.toString();
        final String strKey = "on";
        stub(result, queryUrl, mediaType, strKey, strValue);
    }

    private static void stub(final Object result, final String queryUrl, final String mediaType, final String strKey, final String strValue) {
        String strPayload;
        try {
            strPayload = Utilities.JsonUtil.toJsonString(result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        stubFor(get(urlPathEqualTo(queryUrl))
                .withQueryParam(strKey, equalTo(strValue))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", mediaType)
                        .withBody(strPayload)));
        final String url = MessageFormat.format("{0}/{1}?{2}={3}", getBaseUri(), queryUrl, strKey, strValue);
        poll(requestParams(url, mediaType).build())
                .until(status().is(Response.Status.OK));
    }

    private static void stubGetReferenceDataResultPromptWordSynonymsForFirstDay() {
        stubGetReferenceDataResultPromptWordSynonyms(LocalDate.now(), "referencedata.result-prompt-word-synonyms.json");
    }

    private static void stubGetReferenceDataResultPromptFixedListsForFirstDay() {
        stubGetReferenceDataResultPromptFixedLists(LocalDate.now(), "referencedata.fixed-list-collection.json");
    }

    private static void stubGetReferenceDataResultDefinitionsForSecondDay() {
        stubGetReferenceDataResultDefinitions(LocalDate.now().plusDays(1), "referencedata.result-definitions-version2.json");
    }

    private static void stubGetReferenceDataResultDefinitionsKeywordsForSecondDay() {
        stubGetReferenceDataResultDefinitionsKeywords(LocalDate.now().plusDays(1), "referencedata.result-word-synonyms.json");
    }

    private static void stubGetReferenceDataResultPromptWordSynonymsForSecondDay() {
        stubGetReferenceDataResultPromptWordSynonyms(LocalDate.now().plusDays(1), "referencedata.result-prompt-word-synonyms.json");
    }

    private static void stubGetReferenceDataResultPromptFixedListsForSecondDay() {
        stubGetReferenceDataResultPromptFixedLists(LocalDate.now().plusDays(1), "referencedata.fixed-list-collection.json");
    }

    private static void stubGetReferenceDataResultDefinitions(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String urlPath = format(REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultDefinitionsKeywords(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String urlPath = format(REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptFixedLists(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String urlPath = format(REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptWordSynonyms(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String urlPath = format(REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE);
    }
    private static void stubDynamicPromptFixedList() {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String courtRoomPath = "/referencedata-service/query/api/rest/referencedata/courtrooms";
        final String courtRoomCT = "application/vnd.referencedata.ou-courtrooms+json";
        stubFor(get(urlPathEqualTo(courtRoomPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", courtRoomCT)
                        .withBody(getPayload("referencedata.dyna.fixedlists.court.centre.json"))));

        waitForStubToBeReady(courtRoomPath, courtRoomCT);

        final String hearingTypePath = "/referencedata-service/query/api/rest/referencedata/hearing-types";
        final String hearingTypePathCT = "application/vnd.referencedata.query.hearing-types+json";
        stubFor(get(urlPathEqualTo(hearingTypePath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", hearingTypePathCT)
                        .withBody(getPayload("referencedata.dyna.fixedlists.hearing.type.json"))));

        waitForStubToBeReady(hearingTypePath, hearingTypePathCT);
    }
}
