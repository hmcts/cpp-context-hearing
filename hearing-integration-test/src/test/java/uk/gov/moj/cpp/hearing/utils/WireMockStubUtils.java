package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static com.jayway.awaitility.Duration.TEN_SECONDS;
import static java.text.MessageFormat.format;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils.stubPingFor;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.rest.ResteasyClientBuilderFactory.clientBuilder;
import static uk.gov.moj.cpp.hearing.utils.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.external.domain.referencedata.PIEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.PIEventMappingsList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for setting stubs.
 */
public class WireMockStubUtils {

    public static final String MATERIAL_STATUS_UPLOAD_COMMAND =
            "/results-service/command/api/rest/results/hearings/.*/nowsmaterial/.*";

    public static final String MATERIAL_UPLOAD_COMMAND =
            "/material-service/command/api/rest/material/material";
    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
    private static final String CONTENT_TYPE_QUERY_GROUPS = "application/vnd.usersgroups.groups+json";
    private static final String CONTENT_TYPE_QUERY_PROSECTIONCASE = "application/vnd.hearing.prosecution-case-by-hearingid+json";

    private static final String BASE_URI = "http://" + HOST + ":8080";

    private final static String QUERY_GET_LOGGED_IN_USER_PERMISSIONS = "/usersgroups-service/query/api/rest/usersgroups/users/logged-in-user/permissions";
    private final static String CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS = "application/vnd.usersgroups.get-logged-in-user-permissions+json";

    private static final String QUERY_ROLES_FOR_USER = "/usersgroups-service/query/api/rest/usersgroups/users/{0}/roles";
    private static final String CONTENT_TYPE_QUERY_ROLES_FOR_USER = "application/vnd.usersgroups.roles+json";

    private static final Logger LOGGER = LoggerFactory.getLogger(WireMockStubUtils.class);

    static {
        LOGGER.info("Configuring and reseting wiremock");
        configureFor(HOST, 8080);
    }

    public static void setupAsAuthorisedUserByGivenGroup(final UUID userId, final String group) {

        final String response = getPayload("stub-data/usersgroups.get-groups-by-user-and-group.json").replaceAll("%GROUP_NAME%", group);

        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/8ac63b71-c154-4469-af51-db3528c4d0d3/groups", userId), CONTENT_TYPE_QUERY_GROUPS);
    }


    public static void stubUsersAndGroupsGetLoggedInPermissionsWithoutCases() {
        final String response = getPayload("stub-data/usersgroups.permissions.json");

        stubFor(get(urlPathEqualTo(QUERY_GET_LOGGED_IN_USER_PERMISSIONS))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));
        waitForStubToBeReady(QUERY_GET_LOGGED_IN_USER_PERMISSIONS, CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS);

    }

    public static void stubUsersAndGroupsGetLoggedInPermissionsWithCases(final UUID case1, final UUID case2, final UUID case3, final UUID userId) {
        final String response = getPayload("stub-data/usersgroups.permissions-for-cases.json")
                .replaceAll("%CASE_1%", case1.toString())
                .replaceAll("%CASE_2%", case2.toString())
                .replaceAll("%CASE_3%", case3.toString())
                .replaceAll("%USER_ID%", userId.toString());

        stubFor(get(urlPathEqualTo(QUERY_GET_LOGGED_IN_USER_PERMISSIONS))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));
        waitForStubToBeReady(QUERY_GET_LOGGED_IN_USER_PERMISSIONS, CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS);

    }

    public static void stubUsersAndGroupsGetLoggedInPermissionsWithCasesForRecorder(final UUID case1, final UUID case2, final UUID case3, final UUID userId) {
        final String response = getPayload("stub-data/usersgroups.permissions-for-cases-recorder.json")
                .replaceAll("%CASE_1%", case1.toString())
                .replaceAll("%CASE_2%", case2.toString())
                .replaceAll("%CASE_3%", case3.toString())
                .replaceAll("%USER_ID%", userId.toString());

        stubFor(get(urlPathEqualTo(QUERY_GET_LOGGED_IN_USER_PERMISSIONS))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));
        waitForStubToBeReady(QUERY_GET_LOGGED_IN_USER_PERMISSIONS, CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS);

    }

    public static void stubUsersAndGroupsGetLoggedInPermissionsWithFilteredCases(final UUID case1, final UUID userId) {
        final String response = getPayload("stub-data/usersgroups.permissions-for-filtered-cases-ddj.json")
                .replaceAll("%CASE_1%", case1.toString())
                .replaceAll("%USER_ID%", userId.toString());

        stubFor(get(urlPathEqualTo(QUERY_GET_LOGGED_IN_USER_PERMISSIONS))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));
        waitForStubToBeReady(QUERY_GET_LOGGED_IN_USER_PERMISSIONS, CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS);

    }

    public static void stubUsersAndGroupsGetLoggedInPermissionsWithFilteredCasesForRecorder(final UUID case1, final UUID userId) {
        final String response = getPayload("stub-data/usersgroups.permissions-for-filtered-cases-recorder.json")
                .replaceAll("%CASE_1%", case1.toString())
                .replaceAll("%USER_ID%", userId.toString());

        stubFor(get(urlPathEqualTo(QUERY_GET_LOGGED_IN_USER_PERMISSIONS))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));
        waitForStubToBeReady(QUERY_GET_LOGGED_IN_USER_PERMISSIONS, CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS);

    }

    public static void stubUsersAndGroupsUserRoles(final UUID userId) {
        final String response = getPayload("stub-data/usergroups.get-roles-for-user.json");
        final String url = format(QUERY_ROLES_FOR_USER, userId.toString());

        final ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(SC_OK)
                .withHeader(ID, randomUUID().toString())
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .withBody(response);
        stubFor(get(urlPathEqualTo(url))
                .willReturn(responseDefBuilder));

        waitForStubToBeReady(url, CONTENT_TYPE_QUERY_ROLES_FOR_USER);
    }

    public static void stubUsersAndGroupsUserRolesForDDJ(final UUID userId) {
        final String response = getPayload("stub-data/usergroups.get-roles-for-user-ddj.json");
        final String url = format(QUERY_ROLES_FOR_USER, userId.toString());

        final ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(SC_OK)
                .withHeader(ID, randomUUID().toString())
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .withBody(response);
        stubFor(get(urlPathEqualTo(url))
                .willReturn(responseDefBuilder));

        waitForStubToBeReady(url, CONTENT_TYPE_QUERY_ROLES_FOR_USER);
    }


    public static void stubUsersAndGroupsUserRolesForRecorder(final UUID userId) {
        final String response = getPayload("stub-data/usergroups.get-roles-for-user-recorder.json");
        final String url = format(QUERY_ROLES_FOR_USER, userId.toString());

        final ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(SC_OK)
                .withHeader(ID, randomUUID().toString())
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .withBody(response);
        stubFor(get(urlPathEqualTo(url))
                .willReturn(responseDefBuilder));

        waitForStubToBeReady(url, CONTENT_TYPE_QUERY_ROLES_FOR_USER);
    }


    public static void stubUsersAndGroupsGetLoggedInPermissionsWithoutCasesForDDJ() {
        final String response = getPayload("stub-data/usersgroups.permissions-for-DDJ.json");

        stubFor(get(urlPathEqualTo(QUERY_GET_LOGGED_IN_USER_PERMISSIONS))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));
        waitForStubToBeReady(QUERY_GET_LOGGED_IN_USER_PERMISSIONS, CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS);

    }

    public static void stubUsersAndGroupsGetLoggedInPermissionsWithoutCasesForRecorder() {
        final String response = getPayload("stub-data/usersgroups.permissions-for-recorder.json");

        stubFor(get(urlPathEqualTo(QUERY_GET_LOGGED_IN_USER_PERMISSIONS))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(response)));
        waitForStubToBeReady(QUERY_GET_LOGGED_IN_USER_PERMISSIONS, CONTENT_TYPE_QUERY_GET_LOGGED_IN_USER_PERMISSIONS);

    }

    public static void setupAsAuthorisedUser(final UUID userId) {

        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("stub-data/usersgroups.get-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId), CONTENT_TYPE_QUERY_GROUPS);
    }

    public static void setupAsSystemUser(final UUID userId) {

        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("stub-data/usersgroups.get-systemuser-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId), CONTENT_TYPE_QUERY_GROUPS);
    }

    public static void setupAsWildcardUserBelongingToAllGroups() {

        stubFor(get(urlMatching("/usersgroups-service/query/api/rest/usersgroups/users/.*/groups"))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("stub-data/usersgroups.get-all-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", randomUUID()), CONTENT_TYPE_QUERY_GROUPS);
    }

    public static void setupAsAuthorizedAndSystemUser(final UUID userId) {
        stubPingFor("usersgroups-service");

        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("stub-data/usersgroups.get-system-and-authorized-user-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId), CONTENT_TYPE_QUERY_GROUPS);
    }

    public static void setupAsAuthorizedUserToQueryCaseByDefendant(final UUID userId) {
        stubPingFor("usersgroups-service");

        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("stub-data/usergroups-get-for-cases-by-defendant.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId), CONTENT_TYPE_QUERY_GROUPS);
    }



    public static void stubStagingEnforcementOutstandingFines() {

        stubPingFor("stagingenforcement-query-api");

        final String urlPath = "/stagingenforcement-service/query/api/rest/stagingenforcement/defendant/outstanding-fines";
        stubFor(get(urlPathEqualTo(urlPath))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader(HeaderConstants.ID, randomUUID().toString())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.stagingenforcement.defendant.outstanding-fines+json")
                        .withBody(getPayload("stub-data/stagingenforcement.defendant.outstanding-fines.json"))));
        waitForStubToBeReady(
                String.format("/stagingenforcement-service/query/api/rest/stagingenforcement/defendant/outstanding-fines"),
                "application/vnd.stagingenforcement.defendant.outstanding-fines+json");

    }

    public static void mockProgressionCaseDetails(final UUID caseId, final String caseUrn) {

        stubFor(get(urlMatching("/usersgroups-service/query/api/rest/usersgroups/users/.*/groups"))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("stub-data/usersgroups.get-all-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", randomUUID()), CONTENT_TYPE_QUERY_GROUPS);
    }

    public static void setupAsMagistrateUser(final UUID userId) {
        stubFor(get(urlPathEqualTo(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId)))
                .willReturn(aResponse().withStatus(OK.getStatusCode())
                        .withHeader(ID, randomUUID().toString())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(getPayload("stub-data/usersgroups.get-magistrateuser-groups-by-user.json"))));

        waitForStubToBeReady(format("/usersgroups-service/query/api/rest/usersgroups/users/{0}/groups", userId), CONTENT_TYPE_QUERY_GROUPS);
    }

    public static void stubStagingenforcementCourtRoomsOutstandingFines() {
        InternalEndpointMockUtils.stubPingFor("stagingenforcement-service");

        stubFor(post(urlPathEqualTo("/stagingenforcement-service/command/api/rest/stagingenforcement/court/rooms/outstanding-fines"))
                .withHeader(CONTENT_TYPE, equalTo("application/vnd.stagingenforcement.court.rooms.outstanding-fines+json"))
                .willReturn(aResponse().withStatus(SC_ACCEPTED)));

    }

    public static void stubRequestApproval() {
        InternalEndpointMockUtils.stubPingFor("hearing-service");

        stubFor(post(urlPathEqualTo("/hearing-service/command/api/rest/hearing/request-approval"))
                .withHeader(CONTENT_TYPE, equalTo("application/vnd.hearing.request-approval+json"))
                .willReturn(aResponse().withStatus(SC_ACCEPTED)));

    }

    public static void stubStagingenforcementOutstandingFines() {
        InternalEndpointMockUtils.stubPingFor("stagingenforcement-service");

        stubFor(post(urlPathEqualTo("/stagingenforcement-service/command/api/rest/stagingenforcement/outstanding-fines"))
                .withHeader(CONTENT_TYPE, equalTo("application/vnd.stagingenforcement.request-outstanding-fine+json"))
                .willReturn(aResponse().withStatus(SC_ACCEPTED)));

    }

    public static void stubAzure() {
        stubFor(get(urlPathMatching("/azure/featuremanager/*"))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withBody("{\"items\" : [{ \"content_type\" : \"temp\" , \"key\" : \"reuseOfInformation\", \"value\" : \"{\\\"enabled\\\": true}\"}]}")
                ));

        waitForStubToBeReady("/azure/featuremanager/reuseOfInformation", "");
    }

    public static final void mockMaterialUpload() {

        stubFor(post(urlMatching(MATERIAL_UPLOAD_COMMAND))
                .willReturn(aResponse().withStatus(SC_ACCEPTED)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("")));
    }

    public static final void mockUpdateHmpsMaterialStatus() {
        stubFor(post(urlMatching(
                MATERIAL_STATUS_UPLOAD_COMMAND))
                .willReturn(aResponse().withStatus(SC_ACCEPTED)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("")));
    }

    static void waitForStubToBeReady(final String resource, final String mediaType) {
        waitForStubToBeReady(resource, mediaType, Status.OK);
    }

    static void waitForStubToBeReady(final String resource, final String mediaType, final Status expectedStatus) {
        poll(requestParams(format("{0}/{1}", getBaseUri(), resource), mediaType).build())
                .until(status().is(expectedStatus));
    }

    public static void waitForPutStubToBeReady(final String resource, final String contentType, final Response.Status expectedStatus) {
        waitAtMost(TEN_SECONDS)
                .until(() -> put(BASE_URI + resource, contentType)
                        .getStatus() == expectedStatus.getStatusCode());
    }

    private static Response put(final String url, final String contentType) {
        return clientBuilder().build()
                .target(url)
                .request()
                .put(entity("", MediaType.valueOf(contentType)));
    }

    private static JsonObject getProgressionCaseJson(final UUID caseId, final String caseUrn) {
        return createObjectBuilder()
                .add("caseId", caseId.toString())
                .add("caseUrn", caseUrn)
                .build();
    }

    public static void setupProsecutionCaseByHearingId(final UUID hearingId, final UUID hearingEventDefinitionId) {

        Set<UUID> refHearingEventId = stubPIEventMapperCache(); // TBD

        if (refHearingEventId.contains(hearingEventDefinitionId)) { //TBD
            stubPingFor("hearing-query-api");

            stubFor(get(urlPathEqualTo(format("/query/api/rest/hearing/hearings/{0}/{1}/prosecution-case", hearingId, hearingEventDefinitionId)))
                    .willReturn(aResponse().withStatus(SC_OK)
                            .withHeader("Accept", "application/vnd.hearing.prosecution-case-by-hearingid+json")
                            .withBody(getPayload("stub-data/hearing.get-prosecutioncase-result.json"))));

            waitForStubToBeReady(format("/query/api/rest/hearing/hearings/{0}/{1}/prosecution-case", hearingId, hearingEventDefinitionId), CONTENT_TYPE_QUERY_PROSECTIONCASE);
        }
    }

    public static void setupNoProsecutionCaseByHearingId(final UUID hearingId, final UUID hearingEventDefinitionId) {

        Set<UUID> refHearingEventId = stubPIEventMapperCache(); // TBD

        if (!refHearingEventId.contains(hearingEventDefinitionId)) { //TBD

            stubFor(get(urlPathEqualTo(format("/query/api/rest/hearing/hearings/{0}/{1}/prosecution-case", hearingId, hearingEventDefinitionId)))
                    .willReturn(aResponse().withStatus(SC_OK)
                            .withHeader("Accept", "application/vnd.hearing.prosecution-case-by-hearingid+json")));
            waitForStubToBeReady(format("/query/api/rest/hearing/hearings/{0}/{1}/prosecution-case", hearingId, hearingEventDefinitionId), CONTENT_TYPE_QUERY_PROSECTIONCASE);

        }
    }

    public static Set<UUID> stubPIEventMapperCache() {

        List<PIEventMapping> cpPIHearingEventMappings = new ArrayList<PIEventMapping>();
        Map<UUID, PIEventMapping> eventMapperCache = new HashMap<>();

        final PIEventMapping piEventMapping1 = new PIEventMapping(fromString("b71e7d2a-d3b3-4a55-a393-6d451767fc05"), "100", "Case Started");
        final PIEventMapping piEventMapping2 = new PIEventMapping(fromString("50fb4a64-943d-4a2a-afe6-4b5c9e99e043"), "200", "Appellant case open");
        final PIEventMapping piEventMapping3 = new PIEventMapping(fromString("2ae725a8-b605-4427-aa1d-b587d5c2d71"), "300", "Appellant submissions");
        final PIEventMapping piEventMapping4 = new PIEventMapping(fromString("aebfe8d9-74b4-442b-885f-702c818bf6b5"), "500", "Appellant case closed");

        cpPIHearingEventMappings.add(piEventMapping1);
        cpPIHearingEventMappings.add(piEventMapping2);
        cpPIHearingEventMappings.add(piEventMapping3);
        cpPIHearingEventMappings.add(piEventMapping4);

        PIEventMappingsList piEventMappingsList = new PIEventMappingsList(cpPIHearingEventMappings);

        piEventMappingsList.getCpPIHearingEventMappings().forEach(event -> eventMapperCache.put(event.getCpHearingEventId(), event));

        return eventMapperCache.keySet();
    }


}
