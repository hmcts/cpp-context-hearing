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
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.waitForStubToBeReady;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreasResult;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.referencedata.PIEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.PIEventMappingsList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AllAlcoholLevelMethods;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.verdicttype.AllVerdictTypes;
import uk.gov.moj.cpp.hearing.it.Utilities;

import java.io.StringReader;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import org.apache.http.HttpHeaders;

public class ReferenceDataStub {

    public static final String VERDICT_TYPE_GUILTY_ID = "c4ca4238-a0b9-3382-8dcc-509a6f75849b";
    public static final String VERDICT_TYPE_GUILTY_CODE = "VC01";
    /*todo These 2 data is for same stub, but different tests are trying to add different values, so we put static list to not loose data for other tests.
     * And these data prevent running tests on multiple JVM forks, Currently we support one JVM/MultipleThreads. see  hearing-integration-test/pom.xml
     */
    public static final String ALCOHOL_LEVEL_METHOD_B_CODE = "B";
    public static final String ALCOHOL_LEVEL_METHOD_B_DESCRIPTION = "Breath";
    public static final UUID INEFFECTIVE_TRIAL_TYPE_ID = fromString("9b738c6e-04fc-4e18-bc49-b599973af7e7");
    public static final UUID VACATED_TRIAL_TYPE_ID = fromString("9c738c6e-04fc-4e18-bc49-b599973af7b8");
    public static final UUID CRACKED_TRIAL_TYPE_ID = fromString("9d738c6e-04fc-4e18-bc49-b599973af7c6");
    public static final CrackedIneffectiveVacatedTrialType INEFFECTIVE_TRIAL_TYPE = new CrackedIneffectiveVacatedTrialType(INEFFECTIVE_TRIAL_TYPE_ID, "code", "InEffective", "Prosecution witness absent: police", "fullDescription", null);
    public static final CrackedIneffectiveVacatedTrialType VACATED_TRIAL_TYPE = new CrackedIneffectiveVacatedTrialType(VACATED_TRIAL_TYPE_ID, "code", "Vacated", "Prosecution failed to disclose unused material", "fullDescription", null);
    public static final CrackedIneffectiveVacatedTrialType CRACKED_TRIAL_TYPE = new CrackedIneffectiveVacatedTrialType(CRACKED_TRIAL_TYPE_ID, "code", "Cracked", "Prosecution not ready: specify in comments", "fullDescription", null);
    private static final String REFERENCE_DATA_SERVICE_NAME = "referencedata-service";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions?on=%s";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITHOUT_DATE = "/referencedata-service/query/api/rest/referencedata/result-definitions";
    private static final String REFERENCE_DATA_VERDICT_TYPES_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/verdict-types";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_QUERY_URL_WITH_CODE = "/referencedata-service/query/api/rest/referencedata/result-definitions?shortCode={0}";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_KEYWORDS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-word-synonyms?on=%s";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/fixed-list?on=%s";
    private static final String REFERENCE_DATA_RESULT_PROMPT_PRISON_NAMEADDRESS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/prisons";
    private static final String REFERENCE_DATA_RESULT_ORG_UNIT_CROWN_COURT = "/referencedata-service/query/api/rest/referencedata/organisationunits?oucodeL1Code=C";
    private static final String REFERENCE_DATA_RESULT_ORG_UNIT_MAGS_COURT = "/referencedata-service/query/api/rest/referencedata/organisationunits?oucodeL1Code=B";
    private static final String REFERENCE_DATA_RESULT_REGIONAL_ORG = "/referencedata-service/query/api/rest/referencedata/regional-organisations";
    private static final String REFERENCE_DATA_RESULT_ATTC_ORGANISATION = "/referencedata-service/query/api/rest/referencedata/organisation?orgType=ATTC";
    private static final String REFERENCE_DATA_RESULT_BASS_ORGANISATION = "/referencedata-service/query/api/rest/referencedata/organisation?orgType=BASS";
    private static final String REFERENCE_DATA_RESULT_EMC_ORGANISATION = "/referencedata-service/query/api/rest/referencedata/organisation?orgType=EMC";
    private static final String REFERENCE_DATA_RESULT_LOCAL_AUTHORITY_ORGANISATION = "/referencedata-service/query/api/rest/referencedata/organisation?orgType=DESLA";
    private static final String REFERENCE_DATA_RESULT_NCES_ORGANISATION = "/referencedata-service/query/api/rest/referencedata/organisation?orgType=NCESCOST";
    private static final String REFERENCE_DATA_RESULT_PROBATION_ORGANISATION = "/referencedata-service/query/api/rest/referencedata/organisation?orgType=NPS";
    private static final String REFERENCE_DATA_RESULT_YOTS_ORGANISATION = "/referencedata-service/query/api/rest/referencedata/youth-offending-teams";
    private static final String REFERENCE_DATA_RESULT_SCOTTISH_NI_COURT_ADDRESS = "/referencedata-service/query/api/rest/referencedata/scottish-ni-courts";
    private static final String REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREA_ADDRESS = "/referencedata-service/query/api/rest/referencedata/local-justice-areas";
    private static final String REFERENCE_DATA_RESULT_COUNTRIES = "/referencedata-service/query/api/rest/referencedata/country-nationality";
    private static final String REFERENCE_DATA_RESULT_LANGUAGES = "/referencedata-service/query/api/rest/referencedata/languages";
    private static final String REFERENCE_DATA_RESULT_DRINK_DRIVING_COURSE_PROVIDERS = "/referencedata-service/query/api/rest/referencedata/organisation?orgType=DDRP";
    private static final String REFERENCE_DATA_RESULT_YOUTH_COURT_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/youth-courts";
    private static final String REFERENCE_DATA_ALCOHOL_LEVEL_METHODS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/alcohol-level-methods";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-prompt-word-synonyms?on=%s";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_RULE_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definition-rule?on=%s";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/withdrawn";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/result-definitions/next-hearing";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/nows-metadata";
    private static final String REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/organisation-units";
    private static final String REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/enforcement-area";
    private static final String REFERENCE_DATA_YOUTH_COURT_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/youth-courts";
    private static final String REFERENCE_DATA_RESULT_BAIL_STATUSES_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/bail-statuses";
    private static final String REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/cracked-ineffective-vacated-trial-types";
    private static final String REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/local-justice-areas";
    private static final String REFERENCE_DATA_RESULT_PROSECUTORS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/prosecutors";
    private static final String REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_MEDIA_TYPE = "application/vnd.referencedata.query.local-justice-areas+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_BY_CODE_MEDIA_TYPE = "application/vnd.referencedata.query-result-definitions+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_WITHDRAWN_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-withdrawn+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITIONS_NEXT_HEARING_MEDIA_TYPE = "application/vnd.referencedata.get-result-definition-next-hearing+json";
    private static final String REFERENCE_DATA_RESULT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_FIXED_LISTS_MEDIA_TYPE = "application/vnd.referencedata.get-all-fixed-list+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_PRISON_MEDIA_TYPE = "application/vnd.referencedata.query.prisons+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_ORG_UNIT_MEDIA_TYPE = "application/vnd.referencedata.query.organisationunits+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_REGIONAL_ORG_MEDIA_TYPE = "application/vnd.referencedata.query.regional-organisations+json";
    private static final String REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE = "application/vnd.referencedata.query.organisation.byOrgType+json";
    private static final String REFERENCE_DATA_RESULT_PROMP_YOTS_MEDIA_TYPE = "application/vnd.referencedata.query.youth-offending-teams+json";
    private static final String REFERENCE_DATA_RESULT_PROMP_SCOTTISH_NI_COURT_MEDIA_TYPE = "application/vnd.referencedata.query.scottish-ni-courts+json";
    private static final String REFERENCE_DATA_RESULT_PROMP_LOCAL_JUSTICE_AREA_MEDIA_TYPE = "application/vnd.referencedata.query.local-justice-areas+json";
    private static final String REFERENCE_DATA_RESULT_PROMP_COUNTRIES_MEDIA_TYPE = "application/vnd.referencedata.query.country-nationality+json";
    private static final String REFERENCE_DATA_RESULT_PROMP_LANGUAGES_MEDIA_TYPE = "application/vnd.referencedata.query.languages+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_YOUTH_COURTS_MEDIA_TYPE = "application/vnd.referencedata.query.youth-courts+json";
    private static final String REFERENCE_DATA_RESULT_PROMPT_WORD_SYNONYMS_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-prompt-word-synonyms+json";
    private static final String REFERENCE_DATA_RESULT_DEFINITION_RULE_MEDIA_TYPE = "application/vnd.referencedata.get-all-result-definition-rules+json";
    private static final String REFERENCE_DATA_RESULT_NOWS_METADATA_MEDIA_TYPE = "application/vnd.referencedata.get-all-now-metadata+json";
    private static final String REFERENCE_DATA_RESULT_ORGANISATION_UNIT_MEDIA_TYPE = "application/vnd.referencedata.query.organisation-unit.v2+json";
    private static final String REFERENCE_DATA_RESULT_ORGANISATION_UNIT_V1_MEDIA_TYPE = "application/vnd.referencedata.query.organisation-unit+json";
    private static final String REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_MEDIA_TYPE = "application/vnd.referencedata.query.enforcement-area+json";
    private static final String REFERENCE_DATA_QUERY_YOUTH_COURT_MEDIA_TYPE = "application/vnd.referencedata.query.youth-courts-by-mag-uuid+json";
    private static final String REFERENCE_DATA_RESULT_BAIL_STATUSES_MEDIA_TYPE = "application/vnd.referencedata.bail-statuses+json";
    private static final String REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_MEDIA_TYPE = "application/vnd.referencedata.cracked-ineffective-vacated-trial-types+json";
    private static final String REFERENCE_DATA_COURTROOM_MAPPINGS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/cp-xhibit-courtroom-mappings";
    private static final String REFERENCE_DATA_COURTROOM_MAPPINGS_MEDIA_TYPE = "application/vnd.referencedata.query.cp-xhibit-courtroom-mappings+json";
    private static final String REFERENCE_DATA_RESULT_PROSECUTOR_MEDIA_TYPE = "application/vnd.referencedata.query.get-prosecutor+json";
    private static final String REFERENCE_DATA_VERDICT_TYPES_MEDIA_TYPE = "application/vnd.reference-data.verdict-types+json";
    private static final String REFERENCE_DATA_ALCOHOL_LEVEL_METHODS_MEDIA_TYPE = "application/vnd.referencedata.alcohol-level-methods+json";
    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNITS_URL = "/referencedata-service/query/api/rest/referencedata/organisationunits";
    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNITS_MEDIA_TYPE = "application/vnd.referencedata.query.organisationunits";
    private static final String REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS_URL = "/referencedata-service/query/api/rest/referencedata/cp-xhibit-court-mappings";
    private static final String REFERENCEDATA_QUERY_XHIBIT_COURT_MAPPINGS_MEDIA_TYPE = "application/vnd.referencedata.query.cp-xhibit-court-mappings";
    private static final String COURT_ROOM_MEDIA_TYPE = "application/vnd.referencedata.ou-courtrooms+json";
    private static final String COURT_ROOM_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/courtrooms";
    private static final String REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/cp-xhibit-hearing-event-mappings";
    //CCT877 - TBD - End
    private static final String REFERENCE_DATA_XHIBIT_EVENT_MAPPINGS_MEDIA_TYPE = "application/vnd.referencedata.query.cp-xhibit-hearing-event-mappings+json";
    private static final String REFERENCE_DATA_JUDICIARIES_MEDIA_TYPE = "application/vnd.reference-data.judiciaries+json";
    private static final String REFERENCE_DATA_JUDICIARIES_URL = "/referencedata-service/query/api/rest/referencedata/judiciaries";
    private static final String REFERENCE_DATA_PLEA_TYPES_MEDIA_TYPE = "application/vnd.referencedata.plea-types+json";
    private static final String REFERENCE_DATA_PLEA_TYPES_URL = "/referencedata-service/query/api/rest/referencedata/plea-types";
    //CCT877 - TBD - Start
    private static final String PI_EVENT_MAPPINGS = "referencedata.query.cp-pi-hearing-event-mappings";
    private static final String REFERENCE_DATA_PI_EVENT_QUERY_URL = "/referencedata-service/query/api/rest/referencedata/pi-events";
    private static final String REFERENCE_DATA_QUERY_PI_EVENT_MEDIA_TYPE = "application/vnd.referencedata.query.pi-events+json";
    private static final List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = new ArrayList<>();

    private static final String REFERENCE_DATA_PUBLIC_HOLIDAYS_MEDIA_TYPE = "application/vnd.referencedata.query.public-holidays+json";
    private static final String REFERENCE_DATA_PUBLIC_HOLIDAYS_URL = "/referencedata-service/query/api/rest/referencedata/public-holidays";

    private static List<JsonValue> createCourtRoomFixture() {
        String body = getPayload("referencedata.dyna.fixedlists.court.centre.json");
        JsonObject jsonObject = createReader(new StringReader(body)).readObject();
        return new ArrayList<>(jsonObject.getJsonArray("organisationunits"));
    }

    public static void stubForReferenceDataResults() {
        stubTrialTypes();
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
        changeCourtRoomsStubWithAdding();
        stubDynamicPromptFixedList();
        //stubCrackedIneffectiveVacatedTrialTypes();
        stubReferenceDataResultListForNameAddress();
        stubProsecutorByMajorCreditorFlag();

        stubGetAllVerdictTypes();
        stubGetAllAlcoholLevelMethods();
        stubGetReferenceDataResultDefinitionsDDCH();
        stubPleaTypeGuiltyFlags();
    }

    public static void stubGetReferenceDataResultDefinitionsWithDefaultValues(final LocalDate orderedDate) {
        stubGetReferenceDataResultDefinitions(LocalDate.now(), "stub-data/result-definitions.json");
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

    public static void stubReferenceDataResultListForNameAddress() {
        stubGetReferenceDataResultPromptForPrisonNameAdress();
        stubGetReferenceDataResultPromptForCrownCourtNameAddress();
        stubGetReferenceDataResultPromptForMagsCourtNameAddress();
        stubGetReferenceDataResultPromptForRegionalOrganisationNameAddress();
        setGetReferenceDataResultPromptForAttentionCenterNameAddress();
        setGetReferenceDataResultPromptForBassProviderNameAddress();
        setGetReferenceDataResultPromptForEMCNameAddress();
        setGetReferenceDataResultPromptForLocalAuthorityNameAddress();
        setGetReferenceDataResultPromptForNCESNameAddress();
        setGetReferenceDataResultPromptForProbationNameAddress();
        setGetReferenceDataResultPromptForYOTSAddress();
        setGetReferenceDataResultPromptForScottishNICourtAddress();
        setGetReferenceDataResultPromptForLocalJusticeAreaAddress();
        setGetReferenceDataResultPromptForCountries();
        setGetReferenceDataResultPromptForLanguages();
        setGetReferenceDataResultPromptForDrinkDrivingCourseProviders();
        setGetReferenceDataResultPromptForYouthCourtAddress();
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

    public static void stubGetAllVerdictTypes() {
        final AllVerdictTypes allVerdictTypes = new AllVerdictTypes();

        List<VerdictType> verdictTypes = new ArrayList<>();
        verdictTypes.add(VerdictType.verdictType()
                .withVerdictCode(VERDICT_TYPE_GUILTY_CODE)
                .withCategory("Guilty")
                .withCategoryType("GUILTY_BY_JURY_CONVICTED")
                .withDescription("Guilty")
                .withId(fromString(VERDICT_TYPE_GUILTY_ID))
                .withSequence(1)
                .withCjsVerdictCode("G")
                .build());
        verdictTypes.add(
                VerdictType.verdictType()
                        .withVerdictCode("VC13")
                        .withCategory("Not Guilty")
                        .withCategoryType("NOT_GUILTY")
                        .withDescription("Not guilty by reason of insanity")
                        .withId(fromString("c51ce410-c124-310e-8db5-e4b97fc2af39"))
                        .withCjsVerdictCode("N")
                        .withSequence(13)
                        .build());

        allVerdictTypes.setVerdictTypes(verdictTypes);

        stubGetAllVerdictTypes(allVerdictTypes);
    }


    public static void stubGetAllAlcoholLevelMethods() {
        final AllAlcoholLevelMethods allAlcoholLevelMethods = new AllAlcoholLevelMethods();

        allAlcoholLevelMethods.setAlcoholLevelMethods(Arrays.asList(
                new AlcoholLevelMethod(randomUUID(), 1, "A", "Blood"),
                new AlcoholLevelMethod(randomUUID(), 2, ALCOHOL_LEVEL_METHOD_B_CODE, ALCOHOL_LEVEL_METHOD_B_DESCRIPTION)));

        stubGetAllAlcoholLevelMethods(allAlcoholLevelMethods);
    }

    public static void stubGetAllVerdictTypes(final AllVerdictTypes allVerdictTypes) {
        stub(allVerdictTypes, REFERENCE_DATA_VERDICT_TYPES_QUERY_URL, REFERENCE_DATA_VERDICT_TYPES_MEDIA_TYPE);
    }

    public static void stubGetAllAlcoholLevelMethods(final AllAlcoholLevelMethods allAlcoholLevelMethods) {
        stub(allAlcoholLevelMethods, REFERENCE_DATA_ALCOHOL_LEVEL_METHODS_QUERY_URL, REFERENCE_DATA_ALCOHOL_LEVEL_METHODS_MEDIA_TYPE);
    }

    public static void stub(final OrganisationalUnit organisationalUnit) {
        stub(organisationalUnit, REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL + "/" + organisationalUnit.getId(),
                REFERENCE_DATA_RESULT_ORGANISATION_UNIT_MEDIA_TYPE);
    }

    public static void stubOrganisationUnit(final OrganisationalUnit organisationalUnit) {
        stub(organisationalUnit, REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL + "/" + organisationalUnit.getId(),
                REFERENCE_DATA_RESULT_ORGANISATION_UNIT_V1_MEDIA_TYPE);
    }

    public static void stub(final Prosecutor prosecutor) {
        stub(prosecutor, REFERENCE_DATA_RESULT_PROSECUTORS_QUERY_URL + "/" + prosecutor.getId(),
                REFERENCE_DATA_RESULT_PROSECUTOR_MEDIA_TYPE);
    }

    public static void stub(final EnforcementArea enforcementArea, String ouCode) {
        stub(enforcementArea, REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_QUERY_URL, REFERENCE_DATA_RESULT_ENFORCEMENT_AREA_MEDIA_TYPE, "localJusticeAreaNationalCourtCode", ouCode);
    }

    public static void stub(final LocalJusticeAreasResult enforcementArea, String ouCode) {
        stub(enforcementArea, REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_QUERY_URL, REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREAS_MEDIA_TYPE, "nationalCourtCode", ouCode);
    }

    public static void stubCrackedInEffectiveTrialTypes(List<CrackedIneffectiveVacatedTrialType> newCrackedIneffectiveVacatedTrialTypes) {
        crackedIneffectiveVacatedTrialTypes.addAll(newCrackedIneffectiveVacatedTrialTypes);
        CrackedIneffectiveVacatedTrialTypes result = new CrackedIneffectiveVacatedTrialTypes();
        result.setCrackedIneffectiveVacatedTrialTypes(crackedIneffectiveVacatedTrialTypes);

        stub(result, REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_QUERY_URL,
                REFERENCE_DATA_RESULT_CRACKED_INEFFECTIVE_TRIAL_TYPES_MEDIA_TYPE);
    }

    public static void stubTrialTypes() {
        CrackedIneffectiveVacatedTrialTypes result = new CrackedIneffectiveVacatedTrialTypes();
        result.setCrackedIneffectiveVacatedTrialTypes(Lists.newArrayList(INEFFECTIVE_TRIAL_TYPE, VACATED_TRIAL_TYPE, CRACKED_TRIAL_TYPE));

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

        stubFor(get(urlPathEqualTo(queryUrl)).withHeader("Accept", equalTo(mediaType))
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

    private static void stubGetReferenceDataResultPromptForPrisonNameAdress() {
        stubGetReferenceDataResultPromptPrisonNameAddress(LocalDate.now(), "referencedata.prison-name-address.json");
    }

    private static void stubGetReferenceDataResultPromptForCrownCourtNameAddress() {
        stubGetReferenceDataResultPromptCrownCourtAddress("referencedata.crown-court-name-address.json");
    }

    private static void stubGetReferenceDataResultPromptForMagsCourtNameAddress() {
        stubGetReferenceDataResultPromptMagsCourtAddress("referencedata.mags-court-name-address.json");
    }

    private static void stubGetReferenceDataResultPromptForRegionalOrganisationNameAddress() {
        stubGetReferenceDataResultPromptRegionalOrganisationAddress("referencedata.regional-address.json");
    }

    private static void setGetReferenceDataResultPromptForAttentionCenterNameAddress() {
        stubGetReferenceDataResultPromptAttentionCenterNameAddress("referencedata.attention-center-address.json");
    }

    private static void setGetReferenceDataResultPromptForBassProviderNameAddress() {
        stubGetReferenceDataResultPromptBassProviderNameAddress("referencedata.bass-provider-address.json");
    }

    private static void setGetReferenceDataResultPromptForEMCNameAddress() {
        stubGetReferenceDataResultPromptEMCNameAddress("referencedata.EMC-address.json");
    }

    private static void setGetReferenceDataResultPromptForLocalAuthorityNameAddress() {
        setGetReferenceDataResultPromptForLocalAuthorityNameAddress("referencedata.local-authority-address.json");
    }


    private static void setGetReferenceDataResultPromptForNCESNameAddress() {
        setGetReferenceDataResultPromptNCESNameAddress("referencedata.NCES-address.json");
    }

    private static void setGetReferenceDataResultPromptForProbationNameAddress() {
        setGetReferenceDataResultPromptProbationNameAddress("referencedata.probation-address.json");
    }

    private static void setGetReferenceDataResultPromptForYOTSAddress() {
        setGetReferenceDataResultPromptYOTSAddress("referencedata.youth-offending-teams-address.json");
    }

    private static void setGetReferenceDataResultPromptForScottishNICourtAddress() {
        setGetReferenceDataResultPromptScottishNICourtddress("referencedata.scottish-ni-court-address.json");
    }

    private static void setGetReferenceDataResultPromptForLocalJusticeAreaAddress() {
        setGetReferenceDataResultPromptLocalJusticeAreaAddress("referencedata.local-justice-area-address.json");
    }

    private static void setGetReferenceDataResultPromptForCountries() {
        setGetReferenceDataResultPromptCountries("referencedata.countries-name.json");
    }

    private static void setGetReferenceDataResultPromptForLanguages() {
        setGetReferenceDataResultPromptLanguages("referencedata.languages.json");
    }

    private static void setGetReferenceDataResultPromptForDrinkDrivingCourseProviders() {
        setGetReferenceDataResultPromptDrinkDrivingCourseProviders("referencedata.drink-driving-course-providers.json");
    }

    private static void setGetReferenceDataResultPromptForYouthCourtAddress() {
        setGetReferenceDataResultPromptYouthCourtAddress("referencedata.youth-court-address.json");
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

    private static void setGetReferenceDataResultPromptYouthCourtAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_YOUTH_COURT_QUERY_URL;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_YOUTH_COURTS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_YOUTH_COURTS_MEDIA_TYPE);
    }

    private static void setGetReferenceDataResultPromptDrinkDrivingCourseProviders(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_DRINK_DRIVING_COURSE_PROVIDERS;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE);
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

    private static void stubGetReferenceDataResultPromptPrisonNameAddress(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = format(REFERENCE_DATA_RESULT_PROMPT_PRISON_NAMEADDRESS_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_PRISON_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_PRISON_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptCrownCourtAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_ORG_UNIT_CROWN_COURT;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_ORG_UNIT_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_ORG_UNIT_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptMagsCourtAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_ORG_UNIT_MAGS_COURT;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_ORG_UNIT_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_ORG_UNIT_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptRegionalOrganisationAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_REGIONAL_ORG;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMPT_REGIONAL_ORG_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMPT_REGIONAL_ORG_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptAttentionCenterNameAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_ATTC_ORGANISATION;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultPromptBassProviderNameAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_BASS_ORGANISATION;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE);
    }

    public static void stubGetReferenceDataResultPromptEMCNameAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_EMC_ORGANISATION;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE);
    }

    public static void setGetReferenceDataResultPromptForLocalAuthorityNameAddress(final String responsePath) {

        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_LOCAL_AUTHORITY_ORGANISATION;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE);
    }

    public static void setGetReferenceDataResultPromptNCESNameAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_NCES_ORGANISATION;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE);
    }

    public static void setGetReferenceDataResultPromptProbationNameAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_PROBATION_ORGANISATION;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_ORGANISATION_MEDIA_TYPE);
    }

    public static void setGetReferenceDataResultPromptYOTSAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_YOTS_ORGANISATION;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_YOTS_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_YOTS_MEDIA_TYPE);
    }

    public static void setGetReferenceDataResultPromptScottishNICourtddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_SCOTTISH_NI_COURT_ADDRESS;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_SCOTTISH_NI_COURT_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_SCOTTISH_NI_COURT_MEDIA_TYPE);
    }


    public static void setGetReferenceDataResultPromptLocalJusticeAreaAddress(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_LOCAL_JUSTICE_AREA_ADDRESS;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_LOCAL_JUSTICE_AREA_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_LOCAL_JUSTICE_AREA_MEDIA_TYPE);
    }

    public static void setGetReferenceDataResultPromptCountries(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_COUNTRIES;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_COUNTRIES_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_COUNTRIES_MEDIA_TYPE);
    }

    public static void setGetReferenceDataResultPromptLanguages(final String responsePath) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String urlPath = REFERENCE_DATA_RESULT_LANGUAGES;
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_PROMP_LANGUAGES_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_PROMP_LANGUAGES_MEDIA_TYPE);
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
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_DEFINITION_RULE_MEDIA_TYPE)
                        .withBody(getPayload(responsePath))));

        waitForStubToBeReady(urlPath, REFERENCE_DATA_RESULT_DEFINITION_RULE_MEDIA_TYPE);
    }

    private static void stubGetReferenceDataResultBailStatuses(final LocalDate orderedDate, final String responsePath) {
        InternalEndpointMockUtils.stubPingFor("referencedata-service");

        final String urlPath = format(REFERENCE_DATA_RESULT_BAIL_STATUSES_QUERY_URL, orderedDate.toString());
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
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

    private static void stubCrackedIneffectiveVacatedTrialTypes() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String hearingTypePath = "/referencedata-service/query/api/rest/referencedata//cracked-ineffective-vacated-trial-types";
        final String hearingTypePathCT = "application/vnd.referencedata.cracked-ineffective-vacated-trial-types+json";
        stubFor(get(urlPathEqualTo(hearingTypePath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", hearingTypePathCT)
                        .withBody(getPayload("stub-data/referencedata.fixedlists.cracked.ineffective.vacated.trial.types.json"))));

        waitForStubToBeReady(hearingTypePath, hearingTypePathCT);
    }

    private static void stubProsecutorByMajorCreditorFlag() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String prosecutorByMajorCreditorFlagTypePath = "/referencedata-service/query/api/rest/referencedata/prosecutors"; //Correct?
        final String prosecutorByMajorCreditorFlagPathCT = "application/vnd.referencedata.query.get.prosecutorMajorCreditor+json";
        stubFor(get(urlPathEqualTo(prosecutorByMajorCreditorFlagTypePath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", prosecutorByMajorCreditorFlagPathCT)
                        .withBody(getPayload("stub-data/referencedata.query.prosecutors-by-major-creditor-flag.json"))));

        waitForStubToBeReady(prosecutorByMajorCreditorFlagTypePath, prosecutorByMajorCreditorFlagPathCT);
    }

    public synchronized static void stubGetReferenceDataCourtRooms(final CourtCentre courtCentre,
                                                                   final HearingLanguage hearingLanguage,
                                                                   final String courtIdForWelsh,
                                                                   final String courtIdForEnglish) {
        UUID welshCourtId = fromString(courtIdForWelsh);
        UUID englishCourtId = fromString(courtIdForEnglish);

        if (HearingLanguage.WELSH.equals(hearingLanguage)) {
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
        final List<JsonValue> organisationunits = createCourtRoomFixture();
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

    public static void stubGetReferenceDataCourtRoomMappings(final String courtRoom1Id, final String courtRoom2Id, String courtRoom3Id, String courtRoom4Id, String courtRoom5Id) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        String payload = getPayload("stub-data/referencedata.query.cp-xhibit-courtroom-mappings.json").replace("COURT_ROOM1_ID", courtRoom1Id)
                .replace("COURT_ROOM2_ID", courtRoom2Id)
                .replace("COURT_ROOM3_ID", courtRoom3Id)
                .replace("COURT_ROOM4_ID", courtRoom4Id)
                .replace("COURT_ROOM5_ID", courtRoom5Id);

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

    public static void stubOrganisationUnit(final String ouId) {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String payloadAsString = getPayload("stub-data/referencedata.query.organisation-unit.json")
                .replace("OU_ID", ouId);
        final JsonObject payloadJson = new StringToJsonObjectConverter().convert(payloadAsString);


        stubFor(get(urlPathMatching(REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL + "/" + ouId)).withHeader("Accept", equalTo("application/vnd.referencedata.query.organisation-unit+json"))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_RESULT_ORGANISATION_UNIT_V1_MEDIA_TYPE)
                        .withBody(payloadJson.toString())));

        waitForStubToBeReady(REFERENCE_DATA_RESULT_ORGANISATION_UNIT_QUERY_URL + "/" + ouId, REFERENCE_DATA_RESULT_ORGANISATION_UNIT_V1_MEDIA_TYPE);
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
                        .withHeader("CPPID", randomUUID().toString())
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

    public static void stubPleaTypeGuiltyFlags() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);
        String payload = getPayload("stub-data/referencedata.query.plea-types.json");

        stubFor(get(urlPathMatching(REFERENCE_DATA_PLEA_TYPES_URL))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_PLEA_TYPES_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(REFERENCE_DATA_PLEA_TYPES_URL, REFERENCE_DATA_PLEA_TYPES_MEDIA_TYPE);
    }

    public static void stubPublicHolidays() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);
        String payload = getPayload("stub-data/referencedata.query.public-holidays.json");

        stubFor(get(urlPathMatching(REFERENCE_DATA_PUBLIC_HOLIDAYS_URL))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_PUBLIC_HOLIDAYS_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(REFERENCE_DATA_PUBLIC_HOLIDAYS_URL, REFERENCE_DATA_PUBLIC_HOLIDAYS_MEDIA_TYPE);
    }

    public static void stubForYouthCourtForMagUUID(final UUID magsUUID) {
        final JsonObjectBuilder builder = createObjectBuilder();
        builder.add("courtCode", "5410");
        builder.add("courtName", "courtName");
        builder.add("courtNameWelsh", "courtNameWelsh");
        builder.add("id", randomUUID().toString());

        final JsonArrayBuilder youthCourtsBuilder = Json.createArrayBuilder();
        youthCourtsBuilder.add(builder.build());
        final JsonObject payload =
                Json.createObjectBuilder().add("youthCourts", youthCourtsBuilder.build()).build();
        stub(payload, REFERENCE_DATA_YOUTH_COURT_QUERY_URL, REFERENCE_DATA_QUERY_YOUTH_COURT_MEDIA_TYPE, "magsUUID", magsUUID.toString());
    }

    //TBD - CCT-877
    public static void stubGetPIReferenceDataEventMappings() {
        InternalEndpointMockUtils.stubPingFor(REFERENCE_DATA_SERVICE_NAME);

        final String payload = getPayload("stub-data/referencedata.query.cp-pi-hearing-event-mappings.json");
        stubFor(get(urlPathMatching(REFERENCE_DATA_PI_EVENT_QUERY_URL))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withHeader("Content-Type", REFERENCE_DATA_QUERY_PI_EVENT_MEDIA_TYPE)
                        .withBody(payload)));

        waitForStubToBeReady(REFERENCE_DATA_PI_EVENT_QUERY_URL, REFERENCE_DATA_QUERY_PI_EVENT_MEDIA_TYPE);

    }


    public static void stubOrganisationalUnit(final UUID id, final String oucode) {
        final OrganisationalUnit organisationalUnit = OrganisationalUnit.organisationalUnit()
                .withOucode(oucode)
                .withId(id.toString())
                .withIsWelsh(true)
                .withWelshAddress1("Welsh address1")
                .withWelshAddress2("Welsh address2")
                .withWelshAddress3("Welsh address3")
                .withWelshAddress4("Welsh address4")
                .withWelshAddress4("Welsh address5")
                .withPostcode("Post Code")
                .build();

        ReferenceDataStub.stub(organisationalUnit);
    }
}
