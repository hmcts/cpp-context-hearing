package uk.gov.moj.cpp.hearing.it;

import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.io.Resources;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

/**
 * Todo need to re-factor all the tests in the class to follow BDD like pattern
 * E.g. have a look at test cases in {@link HearingEventsIT}
 */
public class HearingIT extends AbstractIT {

    private static final UUID USER_ID = randomUUID();
    private static final Header CPP_UID_HEADER = new Header("CJSCPPUID", USER_ID.toString());

    @Before
    public void setUp() {
        setupAsAuthorisedUser(USER_ID.toString());
    }

    @Test
    public void hearingTest() throws IOException, InterruptedException {
        final String caseId = randomUUID().toString();
        final String hearingId = randomUUID().toString();

        final String initiateHearing = Resources.toString(
                getResource("hearing.initiate-hearing.json"),
                defaultCharset());

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        final String initiateHearingBody = initiateHearing.replace("RANDOM_CASE_ID", caseId);

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.start+json")
                .body("{\n" +
                        "  \"localTime\": \"2016-06-01T10:00:00Z\"\n" +
                        "}").header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.end+json")
                .body("{\n" +
                        "  \"localTime\": \"2016-06-01T11:00:00Z\"\n" +
                        "}").header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));


        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.initiate-hearing+json")
                .body(initiateHearingBody).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));


        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-case+json")
                .body("{\n" +
                        "  \"caseId\": \"2a2d7e9e-0c60-11e6-a148-3e1d05defe78\"\n" +
                        "}").header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.book-room+json")
                .body("{\n" +
                        "  \"roomName\": \"Room1\"\n" +
                        "}").header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.allocate-court+json")
                .body("{\n" +
                        "  \"courtCentreName\": \"Bournemouth\"\n" +
                        "}").header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));


        TimeUnit.SECONDS.sleep(15);

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing"), hearingId);

        final Response readResponse = given().spec(requestSpec).and()
                .accept("application/vnd.hearing.get.hearing+json")
                .header(CPP_UID_HEADER).when().get(queryAPIEndPoint).then().extract()
                .response();

        assertThat(readResponse.getStatusCode(), is(200));


        /*
          Test Read store
         */
        assertThat("Case should associated with hearing", readResponse.jsonPath().getList("caseIds").contains(caseId), equalTo(true));
        assertThat("Case should associated with hearing", readResponse.jsonPath().getList("caseIds").contains("2a2d7e9e-0c60-11e6-a148-3e1d05defe78"), equalTo(true));
        assertThat("Hearing ID should match", readResponse.jsonPath().get("hearingId").equals(hearingId), equalTo(true));
        assertThat("HearingType should match", readResponse.jsonPath().get("hearingType").equals("TRIAL"), equalTo(true));
        assertThat("Court Centre name should match", readResponse.jsonPath().get("courtCentreName").equals("Bournemouth"), equalTo(true));
        assertThat("Room name should match", readResponse.jsonPath().get("roomName").equals("Room1"), equalTo(true));
        assertThat("Hearing start Date should match", readResponse.jsonPath().get("startDate").equals("2016-06-01"), equalTo(true));
        assertThat("Hearing Start time should match", readResponse.jsonPath().get("startTime").equals("10:00"), equalTo(true));
        assertThat("Hearing Started time should match", readResponse.jsonPath().get("startedAt").equals("2016-06-01T10:00:00Z"), equalTo(true));
        assertThat("Hearing ended time should match", readResponse.jsonPath().get("endedAt").equals("2016-06-01T11:00:00Z"), equalTo(true));


        final String getHearingsByDate = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearings-by-startDate"), (String) readResponse.jsonPath().get("startDate"));

        final Response readResponses = given().spec(requestSpec).and()
                .accept("application/vnd.hearing.get.hearings-by-startdate+json")
                .header(CPP_UID_HEADER).when().get(getHearingsByDate).then().extract()
                .response();

        assertThat(readResponses.getStatusCode(), is(200));
        assertThat("hearings list size should be greater or equal one", readResponses.jsonPath().getList("hearings").size() >= 1, equalTo(true));

    }

    @Test
    public void hearingEventDefinitionsTest() throws IOException, InterruptedException {
        final List<HearingEventDefinition> hearingEventDefinitions = createHearingEventDefinitions();
        final String hearingEventDefinitionsPayload = createHearingEventDefinitionsPayload(hearingEventDefinitions);

        final Integer hearingId = 0;
        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-command-api-hearings-event_definitions"), hearingId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.create-hearing-event-definitions+json")
                .body(hearingEventDefinitionsPayload).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-hearings-event_definitions"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.hearing-event-definitions+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.eventDefinitions", hasSize(2)),
                                withJsonPath("$.eventDefinitions[0].actionLabel", is(hearingEventDefinitions.get(0).getActionLabel())),
                                withJsonPath("$.eventDefinitions[0].recordedLabel", is(hearingEventDefinitions.get(0).getRecordedLabel())),
                                withJsonPath("$.eventDefinitions[1].actionLabel", is(hearingEventDefinitions.get(1).getActionLabel())),
                                withJsonPath("$.eventDefinitions[1].recordedLabel", is(hearingEventDefinitions.get(1).getRecordedLabel()))
                        )));
    }

    @Test
    public void hearingSaveDraftResultTest() throws IOException, InterruptedException {
        final String targetId = randomUUID().toString();
        final String hearingId = randomUUID().toString();
        final String draftResultCommandPayload = createDraftResultCommandPayload(targetId);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.save-draft-result+json")
                .body(draftResultCommandPayload).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-draft-result"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get-draft-result+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.targets", hasSize(1)),
                                withJsonPath("$.targets[0].targetId", is(targetId)),
                                withJsonPath("$.targets[0].draftResult", is("imp 2 yrs")),
                                withJsonPath("$.targets[0].defendantId", is(UUID.fromString("d06f6539-2a7c-4bc8-bca3-A1e5a225471a").toString())),
                                withJsonPath("$.targets[0].offenceId", is(UUID.fromString("4daefec6-5f78-4109-82d9-1e60544a6c02").toString()))
                        )));
    }

    @Test
    public void hearingAddProsecutionCounselTest() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String personId = randomUUID().toString();
        final String attendeeId = randomUUID().toString();
        final String status = RandomGenerator.STRING.next();
        final String addProsecutionCounselCommandPayload = createAddProsecutionCounselCommandPayload(personId, attendeeId, status);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-prosecution-counsel+json")
                .body(addProsecutionCounselCommandPayload).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-prosecution-counsels"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.prosecution-counsels+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecution-counsels", hasSize(1)),
                                withJsonPath("$.prosecution-counsels[0].id", is(attendeeId)),
                                withJsonPath("$.prosecution-counsels[0].personId", is(personId)),
                                withJsonPath("$.prosecution-counsels[0].status", is(status)
                        ))));
    }

    @Test
    public void hearingAddDefenceCounselTest() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String personId = randomUUID().toString();
        final String attendeeId = randomUUID().toString();
        final String defendantId = randomUUID().toString();
        final String status = RandomGenerator.STRING.next();
        final String addDefenceCounselCommandPayload = createAddDefenceCounselCommandPayload(personId, attendeeId, status, defendantId);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-defence-counsel+json")
                .body(addDefenceCounselCommandPayload).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-defence-counsels"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.defence-counsels+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.defence-counsels", hasSize(1)),
                                withJsonPath("$.defence-counsels[0].id", is(attendeeId)),
                                withJsonPath("$.defence-counsels[0].personId", is(personId)),
                                withJsonPath("$.defence-counsels[0].status", is(status)),
                                withJsonPath("$.defence-counsels[0].defendantIds[0].defendantId", is(defendantId)
                        ))));
    }

    private String createAddProsecutionCounselCommandPayload(final String personId, final String attendeeId, final String status) throws IOException {
        String addProsecutionCounselPayload = Resources.toString(
                getResource("hearing.command.add-prosecution-counsel.json"),
                defaultCharset());

        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$personId", personId);
        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$attendeeId", attendeeId);
        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$status", status);

        return addProsecutionCounselPayload;
    }

    private String createAddDefenceCounselCommandPayload(final String personId, final String attendeeId, final String status, final String defendantId) throws IOException {
        String addDefenceCounselPayload = Resources.toString(
                getResource("hearing.command.add-defence-counsel.json"),
                defaultCharset());

        addDefenceCounselPayload = addDefenceCounselPayload.replace("$personId", personId);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$attendeeId", attendeeId);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$status", status);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$defendantId", defendantId);

        return addDefenceCounselPayload;
    }

    private String createHearingEventDefinitionsPayload(List<HearingEventDefinition> hearingEventDefinitions) throws IOException {
        String hearingEventDefinitionsPayload = Resources.toString(
                getResource("hearing.command.create-hearing-event-definitions.json"),
                defaultCharset());

        hearingEventDefinitionsPayload = hearingEventDefinitionsPayload.replace("$actionLabel0", hearingEventDefinitions.get(0).getActionLabel());
        hearingEventDefinitionsPayload = hearingEventDefinitionsPayload.replace("$recordedLabel0", hearingEventDefinitions.get(0).getRecordedLabel());
        hearingEventDefinitionsPayload = hearingEventDefinitionsPayload.replace("$actionLabel1", hearingEventDefinitions.get(1).getActionLabel());
        hearingEventDefinitionsPayload = hearingEventDefinitionsPayload.replace("$recordedLabel1", hearingEventDefinitions.get(1).getRecordedLabel());

        return hearingEventDefinitionsPayload;
    }

    private List<HearingEventDefinition> createHearingEventDefinitions() {
        return Arrays.asList(
                new HearingEventDefinition(randomUUID().toString(), randomUUID().toString(), 2, null),
                new HearingEventDefinition(randomUUID().toString(), randomUUID().toString(), 1, null)
        );
    }

    private String createDraftResultCommandPayload(final String targetId) throws IOException {
        String draftResultCommandPayload = Resources.toString(
                getResource("hearing.draft-result.json"),
                defaultCharset());

        draftResultCommandPayload = draftResultCommandPayload.replace("$targetId", targetId);

        return draftResultCommandPayload;
    }


}
