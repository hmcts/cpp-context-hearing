package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.json.schemas.staging.HearingLanguage.WELSH;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreasResult;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.it.Utilities;

import java.io.StringReader;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.mockito.Spy;

public class ReferenceDataStub {

    private static final String REFERENCE_DATA_SERVICE_NAME = "referencedata-service";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions?on=%s";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITHOUT_DATE = "/referencedata-service/query/api/rest/referencedata/result-definitions";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITH_CODE = "/referencedata-service/query/api/rest/referencedata/result-definitions?shortCode={0}";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-word-synonyms?on=%s";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/fixed-list?on=%s";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-prompt-word-synonyms?on=%s";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_RULE_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definition-rule?on=%s";

    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/withdrawn";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/next-hearing";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/nows-metadata";
    private static final String REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/organisation-units";
    private static final String REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/enforcement-area";
    private static final String REFERENCE_DATA_RESULT_BAIL_STATUSES_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/bail-statuses";
    private static final String REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/cracked-ineffective-vacated-trial-types";
    private static final String REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/local-justice-areas";


    private static final String REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_MEDIA_TYPE = "application/vnd.referencedata.query.local-justice-areas+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_BY_CODE_MEDIA_TYPE = "application/vnd.referencedata.query-result-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-withdrawn+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-next-hearing+json";
    private static final String REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE = "application/vnd.referencedata.get-all-fixed-list+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-prompt-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITION_RULE_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-definition-rules+json";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE = "application/vnd.referencedata.get-all-now-metadata+json";
    private static final String REFERENCE_DATA_RESULT_ORGANISATION_UNIT_MEDIA_TYPE = "application/vnd.referencedata.query.organisation-unit.v2+json";
    private static final String REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_MEDIA_TYPE = "application/vnd.referencedata.query.enforcement-area+json";
    private static final String REFERENCE_DATA_RESULT_BAIL_STATUSES_MEDIA_TYPE = "application/vnd.referencedata.bail-statuses+json";
    private static final String REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_MEDIA_TYPE = "application/vnd.referencedata.cracked-ineffective-vacated-trial-types+json";
    private static final String REFERENCE_DATA_COURTROOM_MAPPINGS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/cp-xhibit-courtroom-mappings";
    private static final String REFERENCE_DATA_COURTROOM_MAPPINGS_MEDIA_TYPE = "application/vnd.referencedata.query.cp-xhibit-courtroom-mappings+json";

    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNITS_URL = "/referencedata-service/query/api/rest/referencedata/organisationunits";
    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNITS_MEDIA_TYPE = "application/vnd.referencedata.query.organisationunits";

    private static final String REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS_URL = "/referencedata-service/query/api/rest/referencedata/cp-xhibit-court-mappings";
    private static final String REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS_MEDIA_TYPE = "application/vnd.referencedata.query.cp-xhibit-court-mappings";

    private static final String REFERENCEDATA_QUERY_XHIBIT_HEARING_TYPES_URL = "/referencedata-service/query/api/rest/referencedata/hearing-types";
    private static final String REFERENCEDATA_QUERY_XHIBIT_HEARING_TYPES_MEDIA_TYPE = "application/vnd.referencedata.query.hearing-types+json";

    private static final String COURT_ROOM_MEDIA_TYPE = "application/vnd.referencedata.ou-courtrooms+json";
    private static final String COURT_ROOM_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/courtrooms";

    private static final String REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/cp-xhibit-hearing-event-mappings";
    private static final String REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_MEDIA_TYPE = "application/vnd.referencedata.query.cp-xhibit-hearing-event-mappings+json";

    private static final String REFERENCE_DATA_JUDICIARIES_MEDIA_TYPE = "application/vnd.reference-data.judiciaries+json";
    private static final String REFERENCE_DATA_JUDICIARIES_URL = "/referencedata-service/query/api/rest/referencedata/judiciaries";
    /*todo These 2 data is for same stub, but different tests are trying to add different values, so we put static list to not loose data for other tests.
     * And these data prevent running tests on multiple JVM forks, Currently we support one JVM/MultipleThreads. see  hearing-integration-test/pom.xml
     */
    private static final List<JsonValue> organisationunits = createCourtRoomFixture();
    private static final List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = new ArrayList<>();

    @Spy
    private ObjectMapper objectMapper;


    private static List<JsonValue> createCourtRoomFixture() {
        String body = getPayload("referencedata.dyna.fixedlists.court.centre.json");
        JsonObject jsonObject = createReader(new StringReader(body)).readObject();
        return new ArrayList<>(jsonObject.getJsonArray("organisationunits"));
    }

    public static void stubForReferenceDataResults() {
        stubGetReferenceDataResultDefinitionRules();
        stubGetReferenceDataResultDefinitionsForFirstDay();
        stubGetReferenceDataResultDefinitionsKeywordsForFirstDay();
        stubGetReferenceDataResultPromptWordSynonymsForFirstDay();
        stubGetReferenceDataResultPromptFixedListsForFirstDay();

        stubGetReferenceDataResultDefinitionsForSecondDay();
        stubGetReferenceDataResultDefinitionsKeywordsForSecondDay();
        stubGetReferenceDataResultPromptWordSynonymsForSecondDay();
        stubGetReferenceDataResultPromptFixedListsForSecondDay();
        stubGetReferenceDataResultBailStatuses();
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
        stubGetReferenceDataResultBailStatuses();
    }

    private static void stubGetReferenceDataResultDefinitionsWithdrawn() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result-definitions-withdrawn.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultDefinitionsNextHearing() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        stubFor(get(urlPathEqualTo(REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE)
                        .withBody(getPayload("referencedata.result-definitions-next-hearing.json"))));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL, REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE);
    }

    public static void stubGetAllNowsMetaData(final LocalDate referenceDate, final AllNows allNows) {
        stub(allNows, REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL, REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE, "on", referenceDate.toString());
    }

    public static void stubGetAllResultDefinitions(final LocalDate referenceDate, final AllResultDefinitions allResultDefinitions) {
        stub(allResultDefinitions, REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITHOUT_DATE, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE, referenceDate);
    }

    public static void stubGetReferenceDataResultDefinitionsDDCH() {
        final String urlPath = MessageFormat.format(REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITH_CODE, "DDCH");

        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("referencedata.ddch.result-definitions.json"))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_DEFINITIONS_BY_CODE_MEDIA_TYPE);
    }

    public static void stubGetAllResultDefinitions(final AllResultDefinitions allResultDefinitions) {
        stub(allResultDefinitions, REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITHOUT_DATE, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE);
    }

    public static void stub(final OrganisationalUnit organisationalUnit) {

        stub(organisationalUnit, REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL + "/" + organisationalUnit.getId(),
                REFERENCE_DATA_RESULT_ORGANISATION_UNIT_MEDIA_TYPE);
    }

    public static void stub(final EnforcementArea enforcementArea, String ouCode) {
        stub(enforcementArea, REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_QUERY_URL, REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_MEDIA_TYPE, "localJusticeAreaNationalCourtCode", ouCode);
    }

    public static void stub(final LocalJusticeAreasResult enforcementArea, String ouCode) {
        stub(enforcementArea, REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_QUERY_URL, REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_MEDIA_TYPE, "nationalCourtCode", ouCode);
    }

    public static void stubCrackedIOnEffectiveTrialTypes(List<CrackedIneffectiveVacatedTrialType> newCrackedIneffectiveVacatedTrialTypes) {
        crackedIneffectiveVacatedTrialTypes.addAll(newCrackedIneffectiveVacatedTrialTypes);
        CrackedIneffectiveVacatedTrialTypes result = new CrackedIneffectiveVacatedTrialTypes();
        result.setCrackedIneffectiveVacatedTrialTypes(crackedIneffectiveVacatedTrialTypes);

        stub(result, REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_QUERY_URL,
                REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_MEDIA_TYPE);
    }

    private static void stub(final Object result, final String queryUrl, final String mediaType) {
        final String strPayload;
        try {
            strPayload = Utilities.JsonUtil.toJsonString(result);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

        stubFor(get(urlPathEqualTo(queryUrl))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", mediaType)
                        .withBody(strPayload)));

        poll(requestParams(MessageFormat.format("{0}/{1}", getBaseUri(), queryUrl), mediaType).build())
                .until(status().is(Response.Status.OK));
    }

    private static void stub(final Object result, final String queryUrl, final String mediaType, final LocalDate referenceDate) {
        final String strValue = referenceDate.toString();
        final String strKey = "on";
        stub(result, queryUrl, mediaType, strKey, strValue);
    }

    private static void stub(final Object result, final String queryUrl, final String mediaType, final String strKey, final String strValue) {
        final String strPayload;
        try {
            strPayload = Utilities.JsonUtil.toJsonString(result);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

        stubFor(get(urlPathEqualTo(queryUrl))
                .withQueryParam(strKey, equalTo(strValue))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", mediaType)
                        .withBody(strPayload)));
        final String url = MessageFormat.format("{0}/{1}?{2}={3}", getBaseUri(), queryUrl, strKey, strValue);
        poll(requestParams(url, mediaType).build())
                .until(status().is(Response.Status.OK));
    }

    public static void stubGetReferenceDataResultBailStatuses() {
        stubGetReferenceDataResultBailStatuses(LocalDate.now(), "referencedata.bail-statuses.json");
    }

    private static void stubGetReferenceDataResultPromptWordSynonymsForFirstDay() {
        stubGetReferenceDataResultPromptWordSynonyms(LocalDate.now(), "referencedata.result-prompt-word-synonyms.json");
    }

    private static void stubGetReferenceDataResultDefinitionRules() {
        stubGetReferenceDataResultDefinitionRules(LocalDate.now(), "referencedata.result-definition-rules.json");
    }

    public static void stubGetReferenceDataResultDefinitionRules(final LocalDate referenceDate) {
        stubGetReferenceDataResultDefinitionRules(referenceDate, "referencedata.result-definition-rules.json");
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
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = format(REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultDefinitionsKeywords(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = format(REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptFixedLists(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = format(REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptWordSynonyms(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = format(REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE);
    }


    public static void stubCourtRoomsForWelshValues(UUID courtRoomID) {
        String body = getPayload("referencedata.court.rooms.welsh.json").replaceAll("%COURT_ROOM_ID%", courtRoomID.toString());
        JsonObject jsonObject = createReader(new StringReader(body)).readObject();
        changeCourtRoomsStubWithAdding(jsonObject);

    }

    public static void stubFixedListForWelshValues() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String fixedListPath = "/referencedata-service/query/api/rest/referencedata/fixed-list";
        final String fixedListCT = "application/vnd.referencedata.get-all-fixed-list+json";
        stubFor(get(urlPathEqualTo(fixedListPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", fixedListCT)
                        .withBody(getPayload("referencedata.fixed-list-collection-with-welsh.json")))
        );

        waitForStubToBeReady(fixedListPath, fixedListCT);
    }

    private static void stubGetReferenceDataResultDefinitionRules(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String urlPath = format(REFERENCE_DATA_RESULT_DEFINITIONS_RULE_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITION_RULE_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_DEFINITION_RULE_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultBailStatuses(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String urlPath = format(REFERENCE_DATA_RESULT_BAIL_STATUSES_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_BAIL_STATUSES_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_BAIL_STATUSES_MEDIA_TYPE);
    }

    private static void stubDynamicPromptFixedList() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String hearingTypePath = "/referencedata-service/query/api/rest/referencedata/hearing-types";
        final String hearingTypePathCT = "application/vnd.referencedata.query.hearing-types+json";
        stubFor(get(urlPathEqualTo(hearingTypePath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", hearingTypePathCT)
                        .withBody(getPayload("stub-data/referencedata.query.hearing-types.json"))));

        waitForStubToBeReady(hearingTypePath, hearingTypePathCT);
    }

    public synchronized static void stubGetReferenceDataCourtRooms(final CourtCentre courtCentre,
                                                                   final HearingLanguage hearingLanguage,
                                                                   final String courtIdForWelsh,
                                                                   final String courtIdForEnglish) {
        UUID welshCourtId = fromString(courtIdForWelsh);
        UUID englishCourtId = fromString(courtIdForEnglish);

        if (WELSH.equals(hearingLanguage)) {
            welshCourtId = courtCentre.getId();
        } else {
            englishCourtId = courtCentre.getId();
        }
        changeCourtRoomsStubWithAdding(
                createObjectBuilder()
                        .add("id", englishCourtId.toString())
                        .add("oucodeL3Name", courtCentre.getName())
                        .add("isWelsh", false)
                        .add("courtrooms", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", courtCentre.getRoomId().toString())
                                        .add("venueName", courtCentre.getName())
                                        .add("courtroomName", courtCentre.getRoomName())
                                        .build())
                                .build())
                        .build(),
                createObjectBuilder()
                        .add("id", courtCentre.getId().toString())
                        .add("oucode", "B01BE01")
                        .add("oucodeL1Code", "C")
                        .add("oucodeL3Name", "Wimbledon")
                        .add("address1", "4 Belmarsh Road")
                        .add("address2", "London")
                        .add("postcode", "SE28 0HA")
                        .add("defaultStartTime", "10:00")
                        .add("defaultDurationHrs", "7:00")
                        .add("isWelsh", false)
                        .add("courtrooms", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", "f703dc83-d0e4-42c8-8d44-0352d46e5194")
                                        .add("venueName", "WIMBLEDON MAGISTRATES' COURT")
                                        .add("courtroomId", 789)
                                        .add("courtroomName", "Room A")
                                        .add("oucodeL3Name", "Wimbledon")
                                        .build())
                                .add(createObjectBuilder()
                                        .add("id", "2bd3e322-f603-411d-a5ab-2e42ff4b6e00")
                                        .add("venueName", "WIMBLEDON MAGISTRATES' COURT")
                                        .add("courtroomId", 790)
                                        .add("courtroomName", "Room B")
                                        .add("oucodeL3Name", "Wimbledon")
                                        .build())
                                .build())
                        .build()

                ,
                createObjectBuilder()
                        .add("id", welshCourtId.toString())
                        .add("oucode", "C55BN00")
                        .add("lja", 3522)
                        .add("oucodeL1Code", "C")
                        .add("oucodeL3Name", "Welsh Name")
                        .add("isWelsh", true)
                        .add("courtrooms", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", courtCentre.getRoomId().toString())
                                        .add("venueName", "welshCourtRoom")
                                        .add("welshVenueName", courtCentre.getWelshName())
                                        .add("courtroomName", "welshCourtRoom")
                                        .build())
                                .build())
                        .build());


    }

    public static void changeCourtRoomsStubWithAdding(JsonObject... courtRooms) {
        Collections.addAll(organisationunits, courtRooms);
        JsonArrayBuilder arrayBuilder = createArrayBuilder();
        organisationunits.forEach(arrayBuilder::add);
        final JsonObject responsePayload = createObjectBuilder()
                .add("organisationunits", arrayBuilder)
                .build();

        stubFor(get(urlPathEqualTo(COURT_ROOM_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", COURT_ROOM_MEDIA_TYPE)
                        .withBody(responsePayload.toString())));

        waitForStubToBeReady(COURT_ROOM_QUERY_URL, COURT_ROOM_MEDIA_TYPE);
    }

    public static void stubGetReferenceDataCourtRoomMappings(final String courtRoom1Id, final String courtRoom2Id) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        String payload = getPayload("stub-data/referencedata.query.cp-xhibit-courtroom-mappings.json").replace("COURT_ROOM1_ID", courtRoom1Id).replace("COURT_ROOM2_ID", courtRoom2Id);

        stubFor(get(urlPathMatching(REFERENCE_DATA_COURTROOM_MAPPINGS_QUERY_URL))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_COURTROOM_MAPPINGS_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(REFERENCE_DATA_COURTROOM_MAPPINGS_QUERY_URL, REFERENCE_DATA_COURTROOM_MAPPINGS_MEDIA_TYPE);
    }

    public static void stubGetReferenceDataCourtXhibitCourtMappings() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        String payload = getPayload("stub-data/referencedata.query.cp-xhibit-court-mappings.json");

        stubFor(get(urlPathMatching(REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS_URL))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_COURTROOM_MAPPINGS_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS_URL, REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS_MEDIA_TYPE);
    }

    public static void stubGetReferenceDataXhibitHearingTypes() {
        stubDynamicPromptFixedList();
    }

    public static void stubOrganisationUnit(final String... ouIds) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final JsonArrayBuilder orgUnits = createArrayBuilder();
        for (String ouId : ouIds) {
            String payloadAsString = getPayload("stub-data/referencedata.query.organisationunits.json")
                    .replace("OU_ID", ouId);
            final JsonObject payloadJson = (new StringToJsonObjectConverter().convert(payloadAsString)).getJsonArray("organisationunits").getJsonObject(0);
            orgUnits.add(payloadJson);
        }
        final JsonObject organisationunits = createObjectBuilder().add("organisationunits", orgUnits).build();

        stubFor(get(urlPathMatching(REFERENCEDATA_QUERY_ORGANISATION_UNITS_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", REFERENCEDATA_QUERY_ORGANISATION_UNITS_MEDIA_TYPE)
                        .withBody(organisationunits.toString())));

        waitForStubToBeReady(REFERENCEDATA_QUERY_ORGANISATION_UNITS_URL, REFERENCEDATA_QUERY_ORGANISATION_UNITS_MEDIA_TYPE);
    }


    public static void stubGetReferenceDataEventMappings() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String payload = getPayload("stub-data/referencedata.query.cp-xhibit-hearing-event-mappings.json");
        stubFor(get(urlPathMatching(REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_QUERY_URL))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_QUERY_URL, REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_MEDIA_TYPE);
    }

    public static void stubGetReferenceDataJudiciaries() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        String payload = getPayload("stub-data/referencedata.judiciaries.json");

        stubFor(get(urlPathMatching(REFERENCE_DATA_JUDICIARIES_URL))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_JUDICIARIES_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(REFERENCE_DATA_JUDICIARIES_URL, REFERENCE_DATA_JUDICIARIES_MEDIA_TYPE);
    }
}
