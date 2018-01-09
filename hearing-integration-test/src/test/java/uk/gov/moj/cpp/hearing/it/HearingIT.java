package uk.gov.moj.cpp.hearing.it;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserLogsMultipleEvents;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andHearingResultsHaveBeenShared;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingAmendedPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingResultedPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.whenTheUserSharesAmendedResultsForTheHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.whenTheUserSharesResultsForAHearing;
import static uk.gov.moj.cpp.hearing.steps.data.ResultLevel.CASE;
import static uk.gov.moj.cpp.hearing.steps.data.ResultLevel.DEFENDANT;
import static uk.gov.moj.cpp.hearing.steps.data.ResultLevel.OFFENCE;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingDataFactory.amendedResultLine;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingDataFactory.resultLine;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingDataFactory.sharedResultLine;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.hearingEventDefinitionsWithPauseAndResumeEvents;
import static uk.gov.moj.cpp.hearing.steps.data.factory.HearingEventDataFactory.manyRandomEvents;
import static uk.gov.moj.cpp.hearing.utils.AuthorisationServiceStub.stubSetStatusForCapability;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.retrieveMessage;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import com.jayway.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Condition;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.steps.data.ResultLineData;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.json.JsonObject;

import com.google.common.io.Resources;
import com.jayway.jsonpath.matchers.IsJson;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

public class HearingIT extends AbstractIT {
    private static final String PLEA_COLLECTION = "pleas";
    private static final String FIELD_PLEA_ID = "pleaId";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_PLEA_DATE = "pleaDate";
    private static final String FIELD_PERSON_ID = "personId";
    public static final int EXPECTED_DEFAULT_HEARING_LENGTH = 15;

    @Test
    public void getHearing_CapabilityDisabled() throws IOException, InterruptedException {

        stubSetStatusForCapability("hearing.get.hearing", false);

        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.hearing+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(FORBIDDEN));

        stubSetStatusForCapability("hearing.get.hearing", true);
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
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

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
        final String personId1 = randomUUID().toString();
        final String attendeeId1 = randomUUID().toString();
        final String status1 = RandomGenerator.STRING.next();

        final String status2 = RandomGenerator.STRING.next();

        final String personId3 = randomUUID().toString();
        final String attendeeId3 = randomUUID().toString();
        final String status3 = RandomGenerator.STRING.next();

        final String addProsecutionCounselCommandPayload1 = createAddProsecutionCounselCommandPayload(personId1, attendeeId1, status1);
        final String addProsecutionCounselCommandPayload2 = createAddProsecutionCounselCommandPayload(personId1, attendeeId1, status2);
        final String addProsecutionCounselCommandPayload3 = createAddProsecutionCounselCommandPayload(personId3, attendeeId3, status3);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        // Add a prosecution Counsel to a hearing

        final Response writeResponse1 = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-prosecution-counsel+json")
                .body(addProsecutionCounselCommandPayload1).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse1.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-prosecution-counsels"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.prosecution-counsels+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecution-counsels", hasSize(1)),
                                withJsonPath("$.prosecution-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.prosecution-counsels[0].personId", is(personId1)),
                                withJsonPath("$.prosecution-counsels[0].status", is(status1)
                                ))));

        // Update the prosecution's details

        final Response writeResponse3 = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-prosecution-counsel+json")
                .body(addProsecutionCounselCommandPayload2).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse3.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecution-counsels", hasSize(1)),
                                withJsonPath("$.prosecution-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.prosecution-counsels[0].personId", is(personId1)),
                                withJsonPath("$.prosecution-counsels[0].status", is(status2)
                                ))));

        // Add another prosecution Counsel to the same hearing
        final Response writeResponse2 = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-prosecution-counsel+json")
                .body(addProsecutionCounselCommandPayload3).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse1.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecution-counsels", hasSize(2)),
                                withJsonPath("$.prosecution-counsels[0].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.prosecution-counsels[0].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.prosecution-counsels[0].status", isOneOf(status2, status3)),
                                withJsonPath("$.prosecution-counsels[1].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.prosecution-counsels[1].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.prosecution-counsels[1].status", isOneOf(status2, status3))
                        )));
    }

    @Test
    public void hearingAddDefenceCounselTest() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();

        final String personId1 = randomUUID().toString();
        final String attendeeId1 = randomUUID().toString();
        final String defendantId1 = randomUUID().toString();
        final String status1 = RandomGenerator.STRING.next();

        final String defendantId2 = randomUUID().toString();
        final String status2 = RandomGenerator.STRING.next();

        final String personId3 = randomUUID().toString();
        final String attendeeId3 = randomUUID().toString();
        final String defendantId3 = randomUUID().toString();
        final String status3 = RandomGenerator.STRING.next();

        final String addDefenceCounselCommandPayload1 = createAddDefenceCounselCommandPayload(personId1, attendeeId1, status1, defendantId1);
        final String addDefenceCounselCommandPayload2 = createAddDefenceCounselCommandPayload(personId1, attendeeId1, status2, defendantId2);
        final String addDefenceCounselCommandPayload3 = createAddDefenceCounselCommandPayload(personId3, attendeeId3, status3, defendantId3);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-defence-counsel+json")
                .body(addDefenceCounselCommandPayload1).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-defence-counsels"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.defence-counsels+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.defence-counsels", hasSize(1)),
                                withJsonPath("$.defence-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.defence-counsels[0].personId", is(personId1)),
                                withJsonPath("$.defence-counsels[0].status", is(status1)),
                                withJsonPath("$.defence-counsels[0].defendantIds[0].defendantId", is(defendantId1)
                                ))));

        // Edit Defence Counsel
        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-defence-counsel+json")
                .body(addDefenceCounselCommandPayload2).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.defence-counsels", hasSize(1)),
                                withJsonPath("$.defence-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.defence-counsels[0].personId", is(personId1)),
                                withJsonPath("$.defence-counsels[0].status", is(status2)),
                                withJsonPath("$.defence-counsels[0].defendantIds[0].defendantId", is(defendantId2)
                                ))));

        // Add another defence counsel
        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-defence-counsel+json")
                .body(addDefenceCounselCommandPayload3).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.defence-counsels", hasSize(2)),
                                withJsonPath("$.defence-counsels[0].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.defence-counsels[0].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.defence-counsels[0].status", isOneOf(status2, status3)),
                                withJsonPath("$.defence-counsels[0].defendantIds[0].defendantId", isOneOf(defendantId2, defendantId3)),
                                withJsonPath("$.defence-counsels[1].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.defence-counsels[1].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.defence-counsels[1].status", isOneOf(status2, status3)),
                                withJsonPath("$.defence-counsels[1].defendantIds[0].defendantId", isOneOf(defendantId2, defendantId3))
                        )));
    }

    @Test
    public void shouldShareAllAvailableResultLines() {
        final UUID hearingId = randomUUID();

        final ResultLineData resultForCase = resultLine(CASE);
        final ResultLineData resultForOffence = resultLine(OFFENCE);
        final ResultLineData resultForDefendant = resultLine(DEFENDANT);

        final List<ResultLineData> resultLines = newArrayList(resultForCase, resultForDefendant, resultForOffence);

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        whenTheUserSharesResultsForAHearing(hearingId, resultLines);

        thenHearingResultedPublicEventShouldBePublished(hearingId, resultLines);
    }

    @Test
    public void shouldShareAllAvailableResultLinesEvenAfterManyEventsLogged() {
        final UUID hearingId = randomUUID();

        final ResultLineData resultForCase = resultLine(CASE);
        final ResultLineData resultForOffence = resultLine(OFFENCE);
        final ResultLineData resultForDefendant = resultLine(DEFENDANT);

        final List<ResultLineData> resultLines = newArrayList(resultForCase, resultForDefendant, resultForOffence);

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitionsWithPauseAndResumeEvents());

        whenUserLogsMultipleEvents(manyRandomEvents(hearingId, 30));
        whenTheUserSharesResultsForAHearing(hearingId, resultLines);

        thenHearingResultedPublicEventShouldBePublished(hearingId, resultLines);
    }

    @Test
    public void shouldNotifyWhenASharedResultIsAmended() {
        final UUID hearingId = randomUUID();

        final ResultLineData resultForCase = resultLine(CASE);
        final ResultLineData resultForOffence = resultLine(OFFENCE);
        final ResultLineData resultForDefendant = resultLine(DEFENDANT);

        final List<ResultLineData> resultLines = newArrayList(resultForCase, resultForDefendant, resultForOffence);

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        andHearingResultsHaveBeenShared(hearingId, resultLines);

        final ResultLineData amendedResultForOffence = amendedResultLine(resultForOffence);
        final ResultLineData sharedResultForCase = sharedResultLine(resultForCase);
        final ResultLineData sharedResultForDefendant = sharedResultLine(resultForDefendant);
        whenTheUserSharesAmendedResultsForTheHearing(hearingId, newArrayList(sharedResultForCase, sharedResultForDefendant, amendedResultForOffence));

        thenHearingAmendedPublicEventShouldBePublished(hearingId, amendedResultForOffence);
    }

    private void checkSendingSheetCompleteFlow(String caseId, List<String> pleaIds) {
        JSONArray pleas =  waitForPleasForCase(caseId, pleaIds.size());
        Map<String, JSONObject> pleaIdToJsonObject = new HashMap<>();
        for (int done=0; done<pleas.length();  done++) {
            JSONObject pleaJson = pleas.getJSONObject(done);
            pleaIdToJsonObject.put(pleaJson.getString("pleaId"), pleaJson);
        }
        pleaIds.stream().forEach(
                        pleaId ->  {
                            Assert.assertTrue("expected pleaId " + pleaId, pleaIdToJsonObject.containsKey(pleaId)  );
                            JSONObject pleaJson = pleaIdToJsonObject.get(pleaId);
                            //check the hearing
                            String hearingId = pleaJson.getString("hearingId");
                            JSONObject hearingJson = getExistingHearing(hearingId);
                            //  now load up the hearing and do some checks !
                            Assert.assertEquals(EXPECTED_DEFAULT_HEARING_LENGTH,  hearingJson.getInt("duration"));
                            Assert.assertEquals("Magistrate Court Hearing",  hearingJson.getString("hearingType"));
                            Assert.assertEquals("courtCentreName",  hearingJson.getString("courtCentreName"));
                            JSONArray caseIds = hearingJson.getJSONArray("caseIds");
                            Assert.assertEquals(1, caseIds.length());
                            Assert.assertEquals( caseId, caseIds.getString(0));
                        }
        );
    }

    @Test
    public void progressionSendingSheetCompleteNoneGuilty() throws IOException, InterruptedException {
        final MessageProducer messageProducer = publicEvents.createProducer();
        final String eventName = "public.progression.events.sending-sheet-completed";
        final String userId = UUID.randomUUID().toString();

        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), eventName)
                .withUserId(userId)
                .build();
        String resource = eventName + ".noguilty" + ".json";
        //could use builders instead
        UUID caseID = UUID.randomUUID();

        String eventPayloadString = getStringFromResource(resource).
                replaceAll("CASE_ID", caseID.toString());
        final JsonObject eventPayload = new StringToJsonObjectConverter().convert(eventPayloadString);
        sendMessage(messageProducer, eventName, eventPayload, metadata);
        ConditionTimeoutException timeout=null;
        try {
            waitForPleasForCase(caseID.toString(), 1);
        } catch (ConditionTimeoutException ex) {
            timeout = ex;
        }
        Assert.assertTrue("exepected a timeout exception", timeout!=null );
    }


    @Test
    public void progressionSendingSheetComplete1GuiltyPlea() throws IOException, InterruptedException {
        final MessageProducer messageProducer = publicEvents.createProducer();
        final String eventName = "public.progression.events.sending-sheet-completed";
        final String userId = UUID.randomUUID().toString();

        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), eventName)
                .withUserId(userId)
                .build();
        String resource = eventName + ".json";
        //could use builders instead
        UUID caseID = UUID.randomUUID();
        UUID pleaID = UUID.randomUUID();
        UUID courtCentreID = UUID.randomUUID();

        String eventPayloadString = getStringFromResource(resource).
                replaceAll("CASE_ID", caseID.toString()).replaceAll("PLEA_ID", pleaID.toString());
        eventPayloadString = eventPayloadString.replaceAll("COURT_CENTRE_ID", courtCentreID.toString());
        final JsonObject eventPayload = new StringToJsonObjectConverter().convert(eventPayloadString);
        sendMessage(messageProducer, eventName, eventPayload, metadata);
        checkSendingSheetCompleteFlow(caseID.toString(), Arrays.asList(pleaID.toString()));
    }

    @Test
    public void progressionSendingSheetCompletePartialGuiltyThreeConvictionDates() throws IOException, InterruptedException {
        final MessageProducer messageProducer = publicEvents.createProducer();
        final String eventName = "public.progression.events.sending-sheet-completed";
        final String userId = UUID.randomUUID().toString();

        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), eventName)
                .withUserId(userId)
                .build();
        String resource = eventName + ".partialguilty" + ".json";
        //could use builders instead

        UUID caseID = UUID.randomUUID();
        UUID courtCentreID = UUID.randomUUID();

        List<UUID> offenceIDs = new ArrayList<>();
        List<UUID> pleaIDs = new ArrayList<>();

        String payloadString = getStringFromResource(resource);
        payloadString = payloadString.replaceAll("CASE_ID", caseID.toString());
        payloadString = payloadString.replaceAll("COURT_CENTRE_ID", courtCentreID.toString());

        for (int done=0; done<=4; done++) {
            UUID offenceID = UUID.randomUUID();
            UUID pleaID = UUID.randomUUID();
            offenceIDs.add(offenceID);
            pleaIDs.add(pleaID);
            payloadString = payloadString.replaceAll("OFFENCE_ID_" + done, offenceID.toString());
            payloadString = payloadString.replaceAll("PLEA_ID_" + done, pleaID.toString());
        }

        final JsonObject eventPayload = new StringToJsonObjectConverter().convert(payloadString);
        sendMessage(messageProducer, eventName, eventPayload, metadata);

        checkSendingSheetCompleteFlow(caseID.toString(), pleaIDs.stream().map(pleaID->pleaID.toString()).collect(Collectors.toList()));
    }


    private JSONArray waitForPleasForCase(String caseId, int pleaCount) {
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.pleas+json";
        String payload = poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(  withJsonPath("$.pleas[" +  (pleaCount-1)  + "]")))
                       ).getPayload();
        JSONObject jsonObject = new JSONObject(payload);
        JSONArray jsonPleas =  jsonObject.getJSONArray("pleas");
        Assert.assertEquals("expected plea count", pleaCount, jsonPleas.length());
        return jsonPleas;
    }

    private JSONObject getExistingHearing(String hearingId) {
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing"), hearingId.toString());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.hearing+json";

        String payload = poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK)).getPayload();
        return new JSONObject(payload);
    }

    @Test
    public void hearingAddHearingsTest() throws Exception {
        final MessageProducer messageProducer = publicEvents.createProducer();
        final String caseId = UUID.randomUUID().toString();
        final String hearingId = UUID.randomUUID().toString();
        final String userId = UUID.randomUUID().toString();
        final String commandName = "public.hearing-confirmed";
        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), commandName)
                .withUserId(userId)
                .build();
        final JsonObject eventPayload = getSendCaseForListingPayload("public.hearing-added.json",
                caseId, hearingId);
        final MessageConsumer messageConsumer = publicEvents.createConsumer(commandName);
        sendMessage(messageProducer, commandName, eventPayload, metadata);
        final JsonPath message = retrieveMessage(messageConsumer);

        assertThat(message.prettify(), new IsJson<String>(
                        AllOf.allOf(
                withJsonPath("$._metadata.name", IsEqual.equalTo(commandName)),
                withJsonPath("$.hearing.id", IsEqual.equalTo(hearingId)),
                withJsonPath("$.caseId", IsEqual.equalTo(caseId))
        )));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.hearing+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId))
                        )));
    }


        @Test
    public void hearingAddPlea() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();
        final String pleaId = randomUUID().toString();
        final String pleaValue = "GUILTY";
        final String pleaDateString = "2017-02-01";
        final String offenceId = randomUUID().toString();
        final String defendantId = randomUUID().toString();
        final String personId = "d3a0d0f9-78b0-47c6-a362-5febf0485d0f";

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        final String body = getStringFromResource("hearing.update-plea.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_PLEA_ID", pleaId)
                .replace("PLEA_VALUE", pleaValue)
                .replace("PLEA_DATE", pleaDateString)
                .replace("RANDOM_OFFENCE_ID", offenceId)
                .replace("RANDOM_DEFENDANT_ID", defendantId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.pleas+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), is(pleaId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), is(offenceId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(pleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(pleaDateString)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PERSON_ID), is(personId))
                        )));
    }

    @Test
    public void hearingAddMultiplePlea() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();
        final String pleaId_1 = randomUUID().toString();
        final String pleaValue_1 = "GUILTY";
        final String pleaDateString_1 = "2017-02-01";
        final String offenceId_1 = randomUUID().toString();
        final String pleaId_2 = randomUUID().toString();
        final String pleaValue_2 = "NOT GUILTY";
        final String pleaDateString_2 = "2017-02-02";
        final String offenceId_2 = randomUUID().toString();
        final String defendantId = randomUUID().toString();

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        final String body = getStringFromResource("hearing.update-multiple-plea.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_PLEA_ID_1", pleaId_1)
                .replace("PLEA_VALUE_1", pleaValue_1)
                .replace("PLEA_DATE_1", pleaDateString_1)
                .replace("RANDOM_OFFENCE_ID_1", offenceId_1)
                .replace("RANDOM_PLEA_ID_2", pleaId_2)
                .replace("PLEA_VALUE_2", pleaValue_2)
                .replace("PLEA_DATE_2", pleaDateString_2)
                .replace("RANDOM_OFFENCE_ID_2", offenceId_2)
                .replace("RANDOM_DEFENDANT_ID", defendantId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.pleas+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(pleaValue_1, pleaValue_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(pleaDateString_1, pleaDateString_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(pleaValue_1, pleaValue_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(pleaDateString_1, pleaDateString_2))
                        )));
    }

    @Test
    public void hearingUpdatePlea() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();
        final String pleaId = randomUUID().toString();
        final String originalPleaValue = "NOT GUILTY";
        final String originalPleaDateString = "2017-02-01";
        final String updatedPleaValue = "GUILTY";
        final String updatedPleaDateString = "2017-02-02";
        final String offenceId = randomUUID().toString();
        final String defendantId = randomUUID().toString();

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        String body = getStringFromResource("hearing.update-plea.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_PLEA_ID", pleaId)
                .replace("PLEA_VALUE", originalPleaValue)
                .replace("PLEA_DATE", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID", offenceId)
                .replace("RANDOM_DEFENDANT_ID", defendantId);

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), caseId);
        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.pleas+json";

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract

                        ().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), is(pleaId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), is(offenceId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(originalPleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(originalPleaDateString))
                        )));

        // Update plea and call the endpoint again
        body = body.replace(originalPleaValue, updatedPleaValue).replace(originalPleaDateString, updatedPleaDateString);
        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf
                                (
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), is(pleaId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), is(offenceId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(updatedPleaValue)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(updatedPleaDateString))
                                )));

    }

    @Test
    public void hearingAddMultipleUpdateSinglePlea() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();
        final String pleaId_1 = randomUUID().toString();
        final String originalPleaValue = "NOT GUILTY";
        final String originalPleaDateString = "2017-02-01";
        final String offenceId_1 = randomUUID().toString();
        final String pleaId_2 = randomUUID().toString();
        final String updatedPleaValue = "GUILTY";
        final String updatedPleaDateString = "2017-02-02";
        final String offenceId_2 = randomUUID().toString();
        final String defendantId = randomUUID().toString();

        String body = getStringFromResource("hearing.update-multiple-plea.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_PLEA_ID_1", pleaId_1)
                .replace("PLEA_VALUE_1", originalPleaValue)
                .replace("PLEA_DATE_1", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID_1", offenceId_1)
                .replace("RANDOM_PLEA_ID_2", pleaId_2)
                .replace("PLEA_VALUE_2", originalPleaValue)
                .replace("PLEA_DATE_2", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID_2", offenceId_2)
                .replace("RANDOM_DEFENDANT_ID", defendantId);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), caseId);
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String mediaType = "application/vnd.hearing.get.case.pleas+json";

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));


        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(originalPleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(originalPleaDateString)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_VALUE), is(originalPleaValue)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(originalPleaDateString))
                        )));

        // Update only one plea and call the endpoint again ( with both the pleas )
        body = body.replaceFirst(originalPleaValue, updatedPleaValue).replaceFirst(originalPleaDateString, updatedPleaDateString);

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));


        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(originalPleaValue, updatedPleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(originalPleaDateString, updatedPleaDateString)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(originalPleaValue, updatedPleaValue)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(originalPleaDateString, updatedPleaDateString))
                        )));

    }

    @Test
    public void hearingAddMultiplePleaSameOffenceId() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();
        final String pleaId_1 = randomUUID().toString();
        final String pleaValue_1 = "GUILTY";
        final String pleaDateString_1 = "2017-02-01";
        // Use same offenceId to simulate a reject condition. In this case
        // entire update command will be rejected , no add / updates will be performed.
        final String offenceId = randomUUID().toString();
        final String pleaId_2 = randomUUID().toString();
        final String pleaValue_2 = "NOT GUILTY";
        final String pleaDateString_2 = "2017-02-02";
        final String defendantId = randomUUID().toString();

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        final String body = getStringFromResource("hearing.update-multiple-plea.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_PLEA_ID_1", pleaId_1)
                .replace("PLEA_VALUE_1", pleaValue_1)
                .replace("PLEA_DATE_1", pleaDateString_1)
                .replace("RANDOM_OFFENCE_ID_1", offenceId)
                .replace("RANDOM_PLEA_ID_2", pleaId_2)
                .replace("PLEA_VALUE_2", pleaValue_2)
                .replace("PLEA_DATE_2", pleaDateString_2)
                .replace("RANDOM_OFFENCE_ID_2", offenceId)
                .replace("RANDOM_DEFENDANT_ID", defendantId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.pleas+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", PLEA_COLLECTION), is(emptyIterable()))
                        )));
    }

    @Test
    public void hearingUpdatePleaOnlyPleaDateUpdate() throws IOException, InterruptedException {
        final String hearingId = randomUUID().toString();
        final String caseId = randomUUID().toString();
        final String pleaId = randomUUID().toString();
        final String originalPleaValue = "NOT GUILTY";
        final String originalPleaDateString = "2017-02-01";
        final String updatedPleaDateString = "2017-02-02";
        final String offenceId = randomUUID().toString();
        final String defendantId = randomUUID().toString();

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        String body = getStringFromResource("hearing.update-plea.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_PLEA_ID", pleaId)
                .replace("PLEA_VALUE", originalPleaValue)
                .replace("PLEA_DATE", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID", offenceId)
                .replace("RANDOM_DEFENDANT_ID", defendantId);

        // Use get plea by hearingId query endpoint
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.hearing.id"), hearingId);
        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.hearing.pleas+json";

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract

                        ().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), is(pleaId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), is(offenceId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(originalPleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(originalPleaDateString))
                        )));

        // Update plea and call the endpoint again
        body = body.replace(originalPleaDateString, updatedPleaDateString);
        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf
                                (
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), is(pleaId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), is(offenceId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(originalPleaValue)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(updatedPleaDateString))
                                )));

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

    private String createDraftResultCommandPayload(final String targetId) throws IOException {
        String draftResultCommandPayload = Resources.toString(
                getResource("hearing.draft-result.json"),
                defaultCharset());

        draftResultCommandPayload = draftResultCommandPayload.replace("$targetId", targetId);

        return draftResultCommandPayload;
    }

    private JsonObject getSendCaseForListingPayload(final String resource, final String caseId, final String hearingId) throws IOException {
        String sendCaseForListingEventPayloadString = getStringFromResource(resource);
        sendCaseForListingEventPayloadString = sendCaseForListingEventPayloadString.replace("RANDOM_CASE_ID", caseId);
        sendCaseForListingEventPayloadString = sendCaseForListingEventPayloadString.replace("RANDOM_HEARING_ID", hearingId);
        return new StringToJsonObjectConverter().convert(sendCaseForListingEventPayloadString);
    }

    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path),
                defaultCharset());
    }
}
