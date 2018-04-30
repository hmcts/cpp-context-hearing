package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplateWithOnlyMandatoryFields;

import java.text.MessageFormat;
import java.util.UUID;

import javax.json.JsonObject;

import org.apache.http.HttpStatus;
import org.junit.Test;

import com.jayway.restassured.response.Response;

import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

@SuppressWarnings("unchecked")
public class AddWitnessIT extends AbstractIT {

    public static final String TYPE = "Defence";
    public static final String CLASSIFICATION = "Professional";
    public static final String TITLE = "Mr";
    public static final String FIRSTNAME = "James";
    public static final String LASTNAME = "Bond";


    @Test
    public void shouldAddDefenceWitness(){

        final InitiateHearingCommand initiateHearing = initiateHearingCommandTemplateWithOnlyMandatoryFields().build();


        final Hearing hearing = initiateHearing.getHearing();
        final UUID defendantId = hearing.getDefendants().get(0).getId();
        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        final String witnessId = UUID.randomUUID().toString();
        final JsonObject addWitness =  createObjectBuilder()
                .add("id", witnessId)
                .add("hearingId",hearing.getId().toString() )
                .add("type", TYPE)
                .add("classification", CLASSIFICATION)
                .add("title", TITLE)
                .add("firstName", FIRSTNAME)
                .add("lastName", LASTNAME)
                .add("defendantIds", createArrayBuilder().add(createObjectBuilder()
                        .add("defendantId", defendantId.toString()).build()).build())
                .build();

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearing.getId().toString());

        final TestUtilities.EventListener publicEventWitnessAdded = listenFor("public.hearing.events.witness-added")
                .withFilter(isJson(withJsonPath("$.witnessId", is(witnessId.toString()))));

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-witness+json")
                .body(addWitness.toString()).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        publicEventWitnessAdded.waitFor();

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearing.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString())),
                                withJsonPath("$.hearingType", equalStr(hearing, "type")),
                                withJsonPath("$.courtCentreName", equalStr(hearing, "courtCentreName")),
                                withJsonPath("$.roomName", equalStr(hearing, "courtRoomName")),
                                withJsonPath("$.roomId", equalStr(hearing, "courtRoomId")),
                                withJsonPath("$.courtCentreId", equalStr(hearing, "courtCentreId")),
                                withJsonPath("$.judge.id", equalStr(hearing, "judge.id")),
                                withJsonPath("$.judge.title", equalStr(hearing, "judge.title")),
                                withJsonPath("$.judge.firstName", equalStr(hearing, "judge.firstName")),
                                withJsonPath("$.judge.lastName", equalStr(hearing, "judge.lastName")),
                                withJsonPath("$.cases[0].caseId", equalStr(initiateHearing, "cases[0].caseId")),
                                withJsonPath("$.cases[0].caseUrn", equalStr(initiateHearing, "cases[0].urn")),
                                withJsonPath("$.cases[0].defendants[0].defendantId", equalStr(hearing, "defendants[0].id")),
                                withJsonPath("$.cases[0].defendants[0].firstName", equalStr(hearing, "defendants[0].firstName")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", equalStr(hearing, "defendants[0].offences[0].id")),
                                withJsonPath("$.defenceWitnesses[0].id", is(witnessId.toString())),
                                withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                                withJsonPath("$.defenceWitnesses[0].classification", is(CLASSIFICATION)),
                                withJsonPath("$.defenceWitnesses[0].title", is(TITLE)),
                                withJsonPath("$.defenceWitnesses[0].firstName", is(FIRSTNAME)),
                                withJsonPath("$.defenceWitnesses[0].lastName", is(LASTNAME)),
                                withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId", is(defendantId.toString()))
                        )));


    }

    @Test
    public void shouldEnrichHearingWithPastWitnessesTwoDefendantsOneWitness() {
        
        final UUID defendantIdOne = randomUUID();
        final UUID defendantIdTwo = randomUUID();
        final UUID caseId = randomUUID();
        final UUID hearingIdOne = randomUUID();
        final UUID hearingIdTwo = randomUUID();
        final UUID hearingIdThree = randomUUID();
        final UUID witnessId = randomUUID();

        final Hearing hearingOne =
                        initiateFirstHearing(caseId, hearingIdOne, defendantIdOne, defendantIdTwo);

        addDefenceWitnessTwoDefendants(defendantIdOne, defendantIdTwo, witnessId, hearingOne);
        

        relistFirstHearingEnrichDefenceWitnessesForTwoDefendants(defendantIdOne,
                        defendantIdTwo, caseId, hearingIdTwo, witnessId);


        relistSecondHearingWithOneDefendant(defendantIdTwo, caseId, hearingIdThree,
                        witnessId);
    }

    @Test
    public void shouldEnrichHearingWithPastWitnessesOneDefendantTwoWitnesses() {

        final UUID defendantIdOne = randomUUID();
        final UUID caseId = randomUUID();
        final UUID hearingIdOne = randomUUID();
        final UUID hearingIdTwo = randomUUID();
        final UUID witnessIdOne = randomUUID();
        final UUID witnessIdTwo = randomUUID();

        final Hearing hearingOne =
                        initiateFirstHearing(caseId, hearingIdOne, defendantIdOne);

        addDefenceWitnessSingleDefendants(defendantIdOne, witnessIdOne, hearingOne);
        addDefenceWitnessSingleDefendants(defendantIdOne, witnessIdTwo, hearingOne);

        relistFirstHearingEnrichDefenceWitnessesOneDefendant(defendantIdOne, caseId, hearingIdTwo,
                        witnessIdOne, witnessIdTwo);

    }


    private void relistFirstHearingEnrichDefenceWitnessesOneDefendant(final UUID defendantIdOne,
                    final UUID caseId, final UUID hearing, final UUID witnessIdOne,
                    final UUID witnessIdTwo) {

        TestUtilities.EventListener publicEventTopic;
        final InitiateHearingCommand initiateHearingTwo =
                        initiateHearingCommandTemplateWithOnlyMandatoryFields(caseId, hearing,
                                        defendantIdOne).build();
        final Hearing hearingTwo = initiateHearingTwo.getHearing();
        publicEventTopic = listenFor("public.hearing.initiated").withFilter(
                        isJson(withJsonPath("$.hearingId", is(hearing.toString()))));

        makeCommand(requestSpec, "hearing.initiate").ofType("application/vnd.hearing.initiate+json")
                        .withPayload(initiateHearingTwo).executeSuccessfully();
        publicEventTopic.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(
                        ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                        initiateHearingTwo.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";


        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(),
                        CPP_UID_HEADER.getValue()).build()).until(
                                        status().is(OK), print(),
                                        payload().isJson(allOf(withJsonPath("$.hearingId",
                                                        is(initiateHearingTwo.getHearing().getId()
                                                                        .toString())),
                                                        withJsonPath("$.hearingType",
                                                                        equalStr(hearingTwo,
                                                                                        "type")),
                                                        withJsonPath("$.courtCentreName", equalStr(
                                                                        hearingTwo,
                                                                        "courtCentreName")),
                                                        withJsonPath("$.roomName", equalStr(
                                                                        hearingTwo,
                                                                        "courtRoomName")),
                                                        withJsonPath("$.roomId", equalStr(
                                                                        hearingTwo, "courtRoomId")),
                                                        withJsonPath("$.courtCentreId", equalStr(
                                                                        hearingTwo,
                                                                        "courtCentreId")),
                                                        withJsonPath("$.judge.id", equalStr(
                                                                        hearingTwo, "judge.id")),
                                                        withJsonPath("$.judge.title", equalStr(
                                                                        hearingTwo, "judge.title")),
                                                        withJsonPath("$.judge.firstName", equalStr(
                                                                        hearingTwo,
                                                                        "judge.firstName")),
                                                        withJsonPath("$.judge.lastName", equalStr(
                                                                        hearingTwo,
                                                                        "judge.lastName")),
                                                        withJsonPath("$.cases[0].caseId", equalStr(
                                                                        initiateHearingTwo,
                                                                        "cases[0].caseId")),

                                                        withJsonPath("$.defenceWitnesses[0].id",
                                                                        isOneOf(witnessIdOne
                                                                                        .toString(),
                                                                                        witnessIdTwo.toString())),
                                                        withJsonPath("$.defenceWitnesses[0].type",
                                                                        is(TYPE)),
                                                        withJsonPath("$.defenceWitnesses[0].classification",
                                                                        is(CLASSIFICATION)),
                                                        withJsonPath("$.defenceWitnesses[0].title",
                                                                        is(TITLE)),
                                                        withJsonPath("$.defenceWitnesses[0].firstName",
                                                                        is(FIRSTNAME)),
                                                        withJsonPath("$.defenceWitnesses[0].lastName",
                                                                        is(LASTNAME)),
                                                        withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId",
                                                                        is(defendantIdOne
                                                                                        .toString())),

                                                        withJsonPath("$.defenceWitnesses[1].id",
                                                                        isOneOf(witnessIdOne
                                                                                        .toString(),
                                                                                        witnessIdTwo.toString())),
                                                        withJsonPath("$.defenceWitnesses[1].type",
                                                                        is(TYPE)),
                                                        withJsonPath("$.defenceWitnesses[1].classification",
                                                                        is(CLASSIFICATION)),
                                                        withJsonPath("$.defenceWitnesses[1].title",
                                                                        is(TITLE)),
                                                        withJsonPath("$.defenceWitnesses[1].firstName",
                                                                        is(FIRSTNAME)),
                                                        withJsonPath("$.defenceWitnesses[1].lastName",
                                                                        is(LASTNAME)),
                                                        withJsonPath("$.defenceWitnesses[1].defendants[0].defendantId",
                                                                        is(defendantIdOne
                                                                                        .toString()))

                                        )));

    }

    private void relistSecondHearingWithOneDefendant(final UUID defendantIdTwo, final UUID caseId,
                    final UUID hearingIdThree, final UUID witnessId) {
        final String responseType = "application/vnd.hearing.get.hearing.v2+json";


        final InitiateHearingCommand initiateHearingThree =
                        initiateHearingCommandTemplateWithOnlyMandatoryFields(caseId,
                                        hearingIdThree, defendantIdTwo).build();
        
        final Hearing hearingThree = initiateHearingThree.getHearing();
        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                        .withFilter(
                        isJson(withJsonPath("$.hearingId", is(hearingIdThree.toString()))));

        makeCommand(requestSpec, "hearing.initiate").ofType("application/vnd.hearing.initiate+json")
                        .withPayload(initiateHearingThree).executeSuccessfully();
        publicEventTopic.waitFor();
        
        final String queryAPIEndPoint = MessageFormat.format(
                        ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                        initiateHearingThree.getHearing().getId()); 
        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        
        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
        .until(status().is(OK),
                print(),
                payload().isJson(allOf(
                        withJsonPath("$.hearingId", is(initiateHearingThree.getHearing().getId().toString())),
                        withJsonPath("$.hearingType", equalStr(hearingThree, "type")),
                        withJsonPath("$.courtCentreName", equalStr(hearingThree, "courtCentreName")),
                        withJsonPath("$.roomName", equalStr(hearingThree, "courtRoomName")),
                        withJsonPath("$.roomId", equalStr(hearingThree, "courtRoomId")),
                        withJsonPath("$.courtCentreId", equalStr(hearingThree, "courtCentreId")),
                        withJsonPath("$.judge.id", equalStr(hearingThree, "judge.id")),
                        withJsonPath("$.judge.title", equalStr(hearingThree, "judge.title")),
                        withJsonPath("$.judge.firstName", equalStr(hearingThree, "judge.firstName")),
                        withJsonPath("$.judge.lastName", equalStr(hearingThree, "judge.lastName")),
                        withJsonPath("$.cases[0].caseId", equalStr(initiateHearingThree, "cases[0].caseId")),

                        withJsonPath("$.defenceWitnesses[0].id", is(witnessId.toString())),
                        withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                        withJsonPath("$.defenceWitnesses[0].classification", is(CLASSIFICATION)),
                        withJsonPath("$.defenceWitnesses[0].title", is(TITLE)),
                        withJsonPath("$.defenceWitnesses[0].firstName", is(FIRSTNAME)),
                        withJsonPath("$.defenceWitnesses[0].lastName", is(LASTNAME)),
                        withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId",
                                        is(defendantIdTwo.toString()))
                        )));
    }

    private void relistFirstHearingEnrichDefenceWitnessesForTwoDefendants(final UUID defendantIdOne,
                    final UUID defendantIdTwo, final UUID caseId, final UUID hearingIdTwo,
                    final UUID witnessId) {
        TestUtilities.EventListener publicEventTopic;
        final InitiateHearingCommand initiateHearingTwo =
                        initiateHearingCommandTemplateWithOnlyMandatoryFields(caseId, hearingIdTwo,
                                        defendantIdOne, defendantIdTwo).build();
        final Hearing hearingTwo = initiateHearingTwo.getHearing();
        publicEventTopic = listenFor("public.hearing.initiated").withFilter(
                        isJson(withJsonPath("$.hearingId", is(hearingIdTwo.toString()))));

        makeCommand(requestSpec, "hearing.initiate").ofType("application/vnd.hearing.initiate+json")
                        .withPayload(initiateHearingTwo).executeSuccessfully();
        publicEventTopic.waitFor();
        
        final String queryAPIEndPoint = MessageFormat.format(
                        ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                        initiateHearingTwo.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";
        

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
        .until(status().is(OK),
                print(),
                payload().isJson(allOf(
                        withJsonPath("$.hearingId", is(initiateHearingTwo.getHearing().getId().toString())),
                        withJsonPath("$.hearingType", equalStr(hearingTwo, "type")),
                        withJsonPath("$.courtCentreName", equalStr(hearingTwo, "courtCentreName")),
                        withJsonPath("$.roomName", equalStr(hearingTwo, "courtRoomName")),
                        withJsonPath("$.roomId", equalStr(hearingTwo, "courtRoomId")),
                        withJsonPath("$.courtCentreId", equalStr(hearingTwo, "courtCentreId")),
                        withJsonPath("$.judge.id", equalStr(hearingTwo, "judge.id")),
                        withJsonPath("$.judge.title", equalStr(hearingTwo, "judge.title")),
                        withJsonPath("$.judge.firstName", equalStr(hearingTwo, "judge.firstName")),
                        withJsonPath("$.judge.lastName", equalStr(hearingTwo, "judge.lastName")),
                        withJsonPath("$.cases[0].caseId", equalStr(initiateHearingTwo, "cases[0].caseId")),

                        withJsonPath("$.defenceWitnesses[0].id", is(witnessId.toString())),
                        withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                        withJsonPath("$.defenceWitnesses[0].classification", is(CLASSIFICATION)),
                        withJsonPath("$.defenceWitnesses[0].title", is(TITLE)),
                        withJsonPath("$.defenceWitnesses[0].firstName", is(FIRSTNAME)),
                        withJsonPath("$.defenceWitnesses[0].lastName", is(LASTNAME)),
                        withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId",
                                        isOneOf(defendantIdOne
                                                        .toString(),
                                                        defendantIdTwo.toString())),
                        withJsonPath("$.defenceWitnesses[0].defendants[1].defendantId",
                                        isOneOf(defendantIdOne
                                                        .toString(),
                                                        defendantIdTwo.toString()))
                       )));

    }

    private Hearing initiateFirstHearing(final UUID caseId, final UUID hearingIdOne,
                    final UUID... defendantIds) {
        final InitiateHearingCommand initiateHearingOne =
                        initiateHearingCommandTemplateWithOnlyMandatoryFields(caseId, hearingIdOne,
                                        defendantIds).build();


        final Hearing hearingOne = initiateHearingOne.getHearing();
        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                        .withFilter(isJson(withJsonPath("$.hearingId",
                                        is(hearingOne.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate").ofType("application/vnd.hearing.initiate+json")
                        .withPayload(initiateHearingOne).executeSuccessfully();

        publicEventTopic.waitFor();

        return hearingOne;
    }

    private void addDefenceWitnessTwoDefendants(final UUID defendantIdOne,
                    final UUID defendantIdTwo, final UUID witnessId, final Hearing hearing) {
        final JsonObject addWitness = createAddDefenceCommandMultipleDefendant(defendantIdOne,
                        defendantIdTwo, witnessId, hearing);

        invokeAddDefenceWitnessCommand(witnessId, hearing, addWitness);
    }

    private void addDefenceWitnessSingleDefendants(final UUID defendantIdOne, final UUID witnessId,
                    final Hearing hearing) {
        final JsonObject addWitness =
                        createAddDefenceCommandSingleDefendant(defendantIdOne, witnessId, hearing);

        invokeAddDefenceWitnessCommand(witnessId, hearing, addWitness);
    }

    private void invokeAddDefenceWitnessCommand(final UUID witnessId, final Hearing hearing,
                    final JsonObject addWitness) {
        final String commandAPIEndPoint = MessageFormat.format(
                        ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"),
                        hearing.getId().toString());

        final TestUtilities.EventListener publicEventWitnessAdded =
                        listenFor("public.hearing.events.witness-added").withFilter(isJson(
                                        withJsonPath("$.witnessId", is(witnessId.toString()))));

        final Response writeResponse = given().spec(requestSpec).and()
                        .contentType("application/vnd.hearing.add-witness+json")
                        .body(addWitness.toString()).header(CPP_UID_HEADER).when()
                        .post(commandAPIEndPoint).then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        publicEventWitnessAdded.waitFor();
    }

    private JsonObject createAddDefenceCommandMultipleDefendant(final UUID defendantIdOne,
                    final UUID defendantIdTwo, final UUID witnessId, final Hearing hearing) {
        return createObjectBuilder().add("id", witnessId.toString())
                        .add("hearingId", hearing.getId().toString()).add("type", TYPE)
                        .add("classification", CLASSIFICATION).add("title", TITLE)
                        .add("firstName", FIRSTNAME).add("lastName", LASTNAME)
                        .add("defendantIds", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                        .add("defendantId",
                                                                        defendantIdOne.toString())
                                                        .build())
                                        .add(createObjectBuilder()
                                                        .add("defendantId",
                                                                        defendantIdTwo.toString())
                                                        .build())
                                        .build())
                        .build();
    }

    private JsonObject createAddDefenceCommandSingleDefendant(final UUID defendantIdOne,
                    final UUID witnessId, final Hearing hearing) {
        return createObjectBuilder().add("id", witnessId.toString())
                        .add("hearingId", hearing.getId().toString()).add("type", TYPE)
                        .add("classification", CLASSIFICATION).add("title", TITLE)
                        .add("firstName", FIRSTNAME).add("lastName", LASTNAME)
                        .add("defendantIds", createArrayBuilder().add(createObjectBuilder()
                                        .add("defendantId", defendantIdOne.toString()).build())
                                        .build())
                        .build();
    }
}