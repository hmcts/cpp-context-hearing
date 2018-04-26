package uk.gov.moj.cpp.hearing.it;


import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import javax.json.JsonObject;
import java.text.MessageFormat;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplateWithOnlyMandatoryFields;

public class AddWitnessIT extends AbstractIT {

    public static final String TYPE = "Defense";
    public static final String CLASSIFICATION = "Professional";
    public static final String TITLE = "Mr";
    public static final String FIRSTNAME = "James";
    public static final String LASTNAME = "Bond";

    @Test
    public void shouldAddDefenceWitness(){

        InitiateHearingCommand initiateHearing = initiateHearingCommandTemplateWithOnlyMandatoryFields().build();


        final Hearing hearing = initiateHearing.getHearing();
        final UUID defendantId = hearing.getDefendants().get(0).getId();
        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        String witnessId = UUID.randomUUID().toString();
        JsonObject addWitness =  createObjectBuilder()
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

        Response writeResponse = given().spec(requestSpec).and()
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
}