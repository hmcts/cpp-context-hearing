package uk.gov.moj.cpp.hearing.it;

import com.google.common.io.Resources;
import com.jayway.awaitility.core.ConditionTimeoutException;
import com.jayway.jsonpath.matchers.IsJson;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsEqual;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.steps.data.ResultLineData;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.json.JsonObject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.initiateHearingCommandTemplate;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserLogsMultipleEvents;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andHearingResultsHaveBeenShared;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingPleaUpdatedPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingResultAmendedPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingResultedPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingUpdatePleaIgnoredPublicEventShouldBePublished;
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
    private static final int EXPECTED_DEFAULT_HEARING_LENGTH = 15;

    @Test
    public void getHearing_CapabilityDisabled() {
        stubSetStatusForCapability("hearing.get.hearing", false);

        final String hearingId = randomUUID().toString();

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
    public void hearingSaveDraftResultTest() throws IOException {
        final String targetId = randomUUID().toString();
        final String hearingId = randomUUID().toString();
        final String draftResultCommandPayload = createDraftResultCommandPayload(targetId);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        final String publicConfirmEventName = "public.hearing.draft-result-saved";
        final MessageConsumer messageConsumer = publicEvents.createConsumer(publicConfirmEventName);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.save-draft-result+json")
                .body(draftResultCommandPayload).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final JsonPath message = retrieveMessage(messageConsumer);

        assertThat(message.prettify(), new IsJson<>(
                AllOf.allOf(
                        withJsonPath("$._metadata.name", IsEqual.equalTo(publicConfirmEventName)),
                        withJsonPath("$.hearingId", IsEqual.equalTo(hearingId)),
                        withJsonPath("$.targetId", IsEqual.equalTo(targetId))
                )));

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

        thenHearingResultAmendedPublicEventShouldBePublished(hearingId, amendedResultForOffence);
    }

    private void checkSendingSheetCompleteFlow(final String caseId, final List<String> pleaIds) {

        final List<String> pleaIdsToCheck = new ArrayList<>(pleaIds);

        StreamSupport.stream(waitForPleasForCase(caseId, pleaIds.size()).spliterator(), false)
                .map(JSONObject.class::cast)
                .forEach(pleaJson -> {
                    pleaIdsToCheck.remove(pleaJson.getString("pleaId"));

                    final JSONObject hearingJson = getExistingHearing(pleaJson.getString("hearingId"));

                    assertThat(hearingJson.getInt("duration"), is(EXPECTED_DEFAULT_HEARING_LENGTH));
                    assertThat(hearingJson.getString("hearingType"), is("Magistrate Court Hearing"));
                    assertThat(hearingJson.getString("courtCentreName"), is("courtCentreName"));
                    assertThat(hearingJson.getJSONArray("caseIds"), contains(caseId));
                });

        assertThat("Not all expected pleas were present", pleaIdsToCheck, is(empty()));
    }

    @Test
    public void progressionSendingSheetCompleteNoneGuilty() throws IOException {
        final MessageProducer messageProducer = publicEvents.createProducer();
        final String eventName = "public.progression.events.sending-sheet-completed";
        final String userId = UUID.randomUUID().toString();

        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), eventName)
                .withUserId(userId)
                .build();
        final String resource = eventName + ".noguilty" + ".json";
        //could use builders instead
        final UUID caseID = UUID.randomUUID();

        final String eventPayloadString = getStringFromResource(resource).replaceAll("CASE_ID", caseID.toString());
        final JsonObject eventPayload = new StringToJsonObjectConverter().convert(eventPayloadString);
        sendMessage(messageProducer, eventName, eventPayload, metadata);
        ConditionTimeoutException timeout = null;
        try {
            waitForPleasForCase(caseID.toString(), 1);
        } catch (final ConditionTimeoutException ex) {
            timeout = ex;
        }
        assertThat("expected a timeout", timeout, is(not(nullValue())));
    }

    @Ignore("GPE-3032 - depends on sending sheet complete refactor")
    @Test
    public void progressionSendingSheetComplete1GuiltyPlea() throws IOException {
        final MessageProducer messageProducer = publicEvents.createProducer();
        final String eventName = "public.progression.events.sending-sheet-completed";
        final String userId = UUID.randomUUID().toString();

        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), eventName)
                .withUserId(userId)
                .build();
        final String resource = eventName + ".json";

        final UUID caseID = UUID.randomUUID();
        final UUID pleaID = UUID.randomUUID();
        final UUID courtCentreID = UUID.randomUUID();

        String eventPayloadString = getStringFromResource(resource).
                replaceAll("CASE_ID", caseID.toString()).replaceAll("PLEA_ID", pleaID.toString());
        eventPayloadString = eventPayloadString.replaceAll("COURT_CENTRE_ID", courtCentreID.toString());
        final JsonObject eventPayload = new StringToJsonObjectConverter().convert(eventPayloadString);
        sendMessage(messageProducer, eventName, eventPayload, metadata);
        checkSendingSheetCompleteFlow(caseID.toString(), Arrays.asList(pleaID.toString()));
    }

    @Ignore("GPE-3032 - depends on sending sheet complete refactor")
    @Test
    public void progressionSendingSheetCompletePartialGuiltyThreeConvictionDates() throws IOException {
        final MessageProducer messageProducer = publicEvents.createProducer();
        final String eventName = "public.progression.events.sending-sheet-completed";
        final String userId = UUID.randomUUID().toString();

        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), eventName)
                .withUserId(userId)
                .build();
        final String resource = eventName + ".partialguilty" + ".json";
        //could use builders instead

        final UUID caseID = UUID.randomUUID();
        final UUID courtCentreID = UUID.randomUUID();

        final List<UUID> offenceIDs = new ArrayList<>();
        final List<UUID> pleaIDs = new ArrayList<>();

        String payloadString = getStringFromResource(resource);
        payloadString = payloadString.replaceAll("CASE_ID", caseID.toString());
        payloadString = payloadString.replaceAll("COURT_CENTRE_ID", courtCentreID.toString());

        for (int done = 0; done <= 4; done++) {
            final UUID offenceID = UUID.randomUUID();
            final UUID pleaID = UUID.randomUUID();
            offenceIDs.add(offenceID);
            pleaIDs.add(pleaID);
            payloadString = payloadString.replaceAll("OFFENCE_ID_" + done, offenceID.toString());
            payloadString = payloadString.replaceAll("PLEA_ID_" + done, pleaID.toString());
        }

        final JsonObject eventPayload = new StringToJsonObjectConverter().convert(payloadString);
        sendMessage(messageProducer, eventName, eventPayload, metadata);

        checkSendingSheetCompleteFlow(caseID.toString(), pleaIDs.stream().map(pleaID -> pleaID.toString()).collect(Collectors.toList()));
    }


    private JSONArray waitForPleasForCase(final String caseId, final int pleaCount) {
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.pleas+json";
        final String payload = poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.pleas[" + (pleaCount - 1) + "]")))
                ).getPayload();
        final JSONObject jsonObject = new JSONObject(payload);
        final JSONArray jsonPleas = jsonObject.getJSONArray("pleas");
        Assert.assertEquals("expected plea count", pleaCount, jsonPleas.length());
        return jsonPleas;
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
        final JsonObject eventPayload = getHearingConfirmedPayload("public.hearing-confirmed.json",
                caseId, hearingId);
        final MessageConsumer messageConsumer = publicEvents.createConsumer(commandName);
        sendMessage(messageProducer, commandName, eventPayload, metadata);
        final JsonPath message = retrieveMessage(messageConsumer);

        assertThat(message.prettify(), new IsJson<>(
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
                                withJsonPath("$.hearingId", is(hearingId)),
                                withJsonPath("$.caseIds[0]", is(caseId)),
                                withJsonPath("$.courtCentreName", is("Liverpool Crown Court")),
                                withJsonPath("$.courtCentreId", is("e8821a38-546d-4b56-9992-ebdd772a561f")),
                                withJsonPath("$.roomName", is("3")),
                                withJsonPath("$.roomId", is("6bb8a527-2a23-4d7d-b6e2-dd94d2a7d63d")),
                                withJsonPath("$.startDate", is("2016-06-01")),
                                withJsonPath("$.hearingType", is("TRIAL")),
                                withJsonPath("$.duration", is(7200)),
                                withJsonPath("$.judge.id", is("1daefec3-2f76-8109-82d9-2e60544a6c01")),
                                withJsonPath("$.judge.firstName", is("Neil")),
                                withJsonPath("$.judge.lastName", is("Flewitt")),
                                withJsonPath("$.judge.title", is("HHJ"))
                        )));
    }

    @Test
    public void hearingAddPlea() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        final String hearingId = initiateHearingCommand.getHearing().getId().toString();
        final String caseId = initiateHearingCommand.getCases().get(0).getCaseId().toString();
        final String pleaId = randomUUID().toString();
        final String pleaValue = "GUILTY";
        final String pleaDateString = "2017-02-01";
        final String offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString();
        final String defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString();
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

        thenHearingPleaUpdatedPublicEventShouldBePublished(caseId);
    }

    @Test
    public void hearingAddMultiplePlea() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);


        final String pleaId_1 = randomUUID().toString();
        final String pleaValue_1 = "GUILTY";
        final String pleaDateString_1 = "2017-02-01";
        final String pleaId_2 = randomUUID().toString();
        final String pleaValue_2 = "NOT GUILTY";
        final String pleaDateString_2 = "2017-02-02";

        InitiateHearingCommand.Builder builder = initiateHearingCommandTemplate();

        builder.getHearing().getDefendants().get(0).addOffence(Offence.builder()
                .withId(randomUUID())
                .withCaseId(builder.getCases().get(0).getCaseId())
                .withOffenceCode(STRING.next())
                .withWording(STRING.next())
                .withSection(STRING.next())
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withOrderIndex(INTEGER.next())
                .withCount(INTEGER.next())
                .withConvictionDate(PAST_LOCAL_DATE.next()));

        InitiateHearingCommand initiateHearingCommand = builder.build();

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), initiateHearingCommand.getHearing().getId().toString());
        final String body = getStringFromResource("hearing.update-multiple-plea.json").replace("RANDOM_CASE_ID", initiateHearingCommand.getCases().get(0).getCaseId().toString())
                .replace("RANDOM_PLEA_ID_1", pleaId_1)
                .replace("PLEA_VALUE_1", pleaValue_1)
                .replace("PLEA_DATE_1", pleaDateString_1)
                .replace("RANDOM_OFFENCE_ID_1", initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())
                .replace("RANDOM_PLEA_ID_2", pleaId_2)
                .replace("PLEA_VALUE_2", pleaValue_2)
                .replace("PLEA_DATE_2", pleaDateString_2)
                .replace("RANDOM_OFFENCE_ID_2", initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString())
                .replace("RANDOM_DEFENDANT_ID", initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString());

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), initiateHearingCommand.getCases().get(0).getCaseId().toString());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.pleas+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())

                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString(),
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString()
                                )),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(pleaValue_1, pleaValue_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(pleaDateString_1, pleaDateString_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString(),
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString()
                                )),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(pleaValue_1, pleaValue_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(pleaDateString_1, pleaDateString_2))
                        )));
        thenHearingPleaUpdatedPublicEventShouldBePublished(initiateHearingCommand.getCases().get(0).getCaseId().toString());
    }

    @Test
    public void hearingUpdatePlea() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final String pleaId = randomUUID().toString();
        final String originalPleaValue = "NOT GUILTY";
        final String originalPleaDateString = "2017-02-01";
        final String updatedPleaValue = "GUILTY";
        final String updatedPleaDateString = "2017-02-02";

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), initiateHearingCommand.getHearing().getId().toString());
        String body = getStringFromResource("hearing.update-plea.json").replace("RANDOM_CASE_ID", initiateHearingCommand.getCases().get(0).getCaseId().toString())
                .replace("RANDOM_PLEA_ID", pleaId)
                .replace("PLEA_VALUE", originalPleaValue)
                .replace("PLEA_DATE", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID", initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())
                .replace("RANDOM_DEFENDANT_ID", initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString());

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), initiateHearingCommand.getCases().get(0).getCaseId().toString());
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
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), is(pleaId)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(originalPleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(originalPleaDateString))
                        )));

        thenHearingPleaUpdatedPublicEventShouldBePublished(initiateHearingCommand.getCases().get(0).getCaseId().toString());

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
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(initiateHearingCommand.getHearing().getId().toString())),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString())),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(updatedPleaValue)),
                                        withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(updatedPleaDateString))
                                )));

        thenHearingPleaUpdatedPublicEventShouldBePublished(initiateHearingCommand.getCases().get(0).getCaseId().toString());

        //Adding different Plea Id to same offence should ignore update plea
        body = body.replace(pleaId, UUID.randomUUID().toString());
        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-plea+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        thenHearingUpdatePleaIgnoredPublicEventShouldBePublished(initiateHearingCommand.getCases().get(0).getCaseId().toString());
    }

    @Test
    public void hearingAddMultipleUpdateSinglePlea() throws IOException {

        InitiateHearingCommand.Builder builder = initiateHearingCommandTemplate();

        builder.getHearing().getDefendants().get(0).addOffence(Offence.builder()
                .withId(randomUUID())
                .withCaseId(builder.getCases().get(0).getCaseId())
                .withOffenceCode(STRING.next())
                .withWording(STRING.next())
                .withSection(STRING.next())
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withOrderIndex(INTEGER.next())
                .withCount(INTEGER.next())
                .withConvictionDate(PAST_LOCAL_DATE.next()));

        InitiateHearingCommand initiateHearingCommand = builder.build();

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        final String pleaId_1 = randomUUID().toString();
        final String originalPleaValue = "NOT GUILTY";
        final String originalPleaDateString = "2017-02-01";
        final String pleaId_2 = randomUUID().toString();
        final String updatedPleaValue = "GUILTY";
        final String updatedPleaDateString = "2017-02-02";

        String body = getStringFromResource("hearing.update-multiple-plea.json").replace("RANDOM_CASE_ID", initiateHearingCommand.getCases().get(0).getCaseId().toString())
                .replace("RANDOM_PLEA_ID_1", pleaId_1)
                .replace("PLEA_VALUE_1", originalPleaValue)
                .replace("PLEA_DATE_1", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID_1", initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())
                .replace("RANDOM_PLEA_ID_2", pleaId_2)
                .replace("PLEA_VALUE_2", originalPleaValue)
                .replace("PLEA_DATE_2", originalPleaDateString)
                .replace("RANDOM_OFFENCE_ID_2", initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString())
                .replace("RANDOM_DEFENDANT_ID", initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString());


        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), initiateHearingCommand.getHearing().getId().toString());

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.pleas.by.case.id"), initiateHearingCommand.getCases().get(0).getCaseId().toString());
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
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString(),
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString()
                                )),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), is(originalPleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), is(originalPleaDateString)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString(),
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString()
                                )),
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
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_CASE_ID), is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_HEARING_ID), is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_DEFENDANT_ID), is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString(),
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString()
                                )),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(originalPleaValue, updatedPleaValue)),
                                withJsonPath(format("$.%s[0].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(originalPleaDateString, updatedPleaDateString)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_ID), isOneOf(pleaId_1, pleaId_2)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_OFFENCE_ID), isOneOf(
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString(),
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString()
                                )),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_VALUE), isOneOf(originalPleaValue, updatedPleaValue)),
                                withJsonPath(format("$.%s[1].%s", PLEA_COLLECTION, FIELD_PLEA_DATE), isOneOf(originalPleaDateString, updatedPleaDateString))
                        )));

    }

    @Test
    public void hearingAddMultiplePleaSameOffenceId() throws IOException {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();


        final String hearingId = initiateHearingCommand.getHearing().getId().toString();
        final String caseId = initiateHearingCommand.getCases().get(0).getCaseId().toString();
        final String pleaId_1 = randomUUID().toString();
        final String pleaValue_1 = "GUILTY";
        final String pleaDateString_1 = "2017-02-01";
        // Use same offenceId to simulate a reject condition. In this case
        // entire update command will be rejected , no add / updates will be performed.
        final String offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString();
        final String pleaId_2 = randomUUID().toString();
        final String pleaValue_2 = "NOT GUILTY";
        final String pleaDateString_2 = "2017-02-02";
        final String defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString();

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
    public void hearingUpdatePleaOnlyPleaDateUpdate() throws IOException {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        final String hearingId = initiateHearingCommand.getHearing().getId().toString();
        final String caseId = initiateHearingCommand.getCases().get(0).getCaseId().toString();
        final String pleaId = randomUUID().toString();
        final String originalPleaValue = "NOT GUILTY";
        final String originalPleaDateString = "2017-02-01";
        final String updatedPleaDateString = "2017-02-02";
        final String offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString();
        final String defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString();

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


    private String createDraftResultCommandPayload(final String targetId) throws IOException {
        String draftResultCommandPayload = Resources.toString(
                getResource("hearing.draft-result.json"),
                defaultCharset());

        draftResultCommandPayload = draftResultCommandPayload.replace("$targetId", targetId);

        return draftResultCommandPayload;
    }

    private JsonObject getHearingConfirmedPayload(final String resource, final String caseId, final String hearingId) throws IOException {
        String sendCaseForListingEventPayloadString = getStringFromResource(resource);
        sendCaseForListingEventPayloadString = sendCaseForListingEventPayloadString.replace("RANDOM_CASE_ID", caseId);
        sendCaseForListingEventPayloadString = sendCaseForListingEventPayloadString.replace("RANDOM_HEARING_ID", hearingId);
        return new StringToJsonObjectConverter().convert(sendCaseForListingEventPayloadString);
    }


}
