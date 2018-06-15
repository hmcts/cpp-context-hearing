package uk.gov.moj.cpp.hearing.it;

import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import javax.json.JsonObject;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
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
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.basicInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.caseTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimalInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@SuppressWarnings("unchecked")
public class AddWitnessIT extends AbstractIT {

    public static final String TYPE = "Defence";
    public static final String CLASSIFICATION_PROFESSIONAL = "Professional";
    public static final String CLASSIFICATION_EXPERT = "Expert";
    public static final String TITLE_MR = "Mr";
    public static final String TITLE_MISS = "Miss";
    public static final String FIRSTNAME = "James";
    public static final String LASTNAME = "Bond";

    @Test
    public void shouldAddThenUpdateDefenceWitness() {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), i -> {
                            i.getHearing().getDefendants().add(defendantTemplate(i.getCases().get(0).getCaseId()));
                        })
                ));

        Hearing hearing = hearingOne.it().getHearing();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingOne.getHearingId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";
        final String witnessId = UUID.randomUUID().toString();
        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearing.getId().toString());
        final TestUtilities.EventListener publicEventWitnessAdded =
                listenFor("public.hearing.events.witness-added-updated")
                        .withFilter(isJson(withJsonPath("$.witnessId", is(witnessId))));

        assertAddWitnessSingleDefendant(hearingOne.it(), hearing,
                hearingOne.getFirstDefendantId(),
                url, responseType,
                witnessId, commandAPIEndPoint, publicEventWitnessAdded);

        assertUpdateWitnessSingleDefendant(hearingOne.it(), hearing, hearingOne.getSecondDefendantId(),
                url,
                responseType,
                witnessId, commandAPIEndPoint, publicEventWitnessAdded);
    }

    private void assertUpdateWitnessSingleDefendant(final InitiateHearingCommand initiateHearing,
                                                    final Hearing hearing, final UUID defendantId,
                                                    final String url,
                                                    final String responseType, final String witnessId,
                                                    final String commandAPIEndPoint,
                                                    final TestUtilities.EventListener publicEventWitnessAdded) {
        final String firstName = "Jane";
        final String lastName = "Jones";
        final JsonObject updatedWitness = createObjectBuilder().add("id", witnessId)
                .add("hearingId", hearing.getId().toString()).add("type", TYPE)
                .add("classification", CLASSIFICATION_EXPERT).add("title", TITLE_MISS)
                .add("firstName", firstName).add("lastName", lastName)
                .add("defendants", createArrayBuilder().add(createObjectBuilder()
                        .add("defendantId", defendantId.toString())
                        .build()).build())
                .build();

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-update-witness+json")
                .body(updatedWitness.toString()).header(CPP_UID_HEADER).when()
                .post(commandAPIEndPoint).then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        publicEventWitnessAdded.waitFor();

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
                                withJsonPath("$.defenceWitnesses[0].id", is(witnessId.toString())),
                                withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                                withJsonPath("$.defenceWitnesses[0].classification", is(CLASSIFICATION_EXPERT)),
                                withJsonPath("$.defenceWitnesses[0].title", is(TITLE_MISS)),
                                withJsonPath("$.defenceWitnesses[0].firstName", is(firstName)),
                                withJsonPath("$.defenceWitnesses[0].lastName", is(lastName)),
                                withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId", is(defendantId.toString()))
                        )));
    }

    private void assertAddWitnessSingleDefendant(final InitiateHearingCommand initiateHearing,
                                                 final Hearing hearing, final UUID defendantId,
                                                 final String url,
                                                 final String responseType, final String witnessId,
                                                 final String commandAPIEndPoint,
                                                 final TestUtilities.EventListener publicEventWitnessAdded) {
        final JsonObject addWitness = createObjectBuilder().add("id", witnessId)
                .add("hearingId", hearing.getId().toString()).add("type", TYPE)
                .add("classification", CLASSIFICATION_PROFESSIONAL).add("title", TITLE_MR)
                .add("firstName", FIRSTNAME).add("lastName", LASTNAME)
                .add("defendants",
                        createArrayBuilder().add(createObjectBuilder()
                                .add("defendantId",
                                        defendantId.toString())
                                .build()).build())
                .build();

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-update-witness+json")
                .body(addWitness.toString()).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        publicEventWitnessAdded.waitFor();


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
                                withJsonPath("$.defenceWitnesses[0].id", is(witnessId.toString())),
                                withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                                withJsonPath("$.defenceWitnesses[0].classification",
                                        is(CLASSIFICATION_PROFESSIONAL)),
                                withJsonPath("$.defenceWitnesses[0].title",
                                        is(TITLE_MR)),
                                withJsonPath("$.defenceWitnesses[0].firstName", is(FIRSTNAME)),
                                withJsonPath("$.defenceWitnesses[0].lastName", is(LASTNAME)),
                                withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId",
                                        is(defendantId
                                                .toString()))
                        )));
    }

    @Test
    public void shouldEnrichHearingWithPastWitnessesTwoDefendantsOneWitness() {

        final UUID caseId = randomUUID();
        final UUID witnessId = randomUUID();

        //Initiate hearing command with 1 case and 2 defendants
        InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec,
                        with(basicInitiateHearingTemplate(), command -> {

                            command.setCases(asList(caseTemplate(caseId)));

                            command.getHearing().setDefendants(asList(
                                    defendantTemplate(caseId),
                                    defendantTemplate(caseId)
                            ));
                        })
                ));

        addDefenceWitnessTwoDefendants(
                hearingOne.getFirstDefendantId(),
                hearingOne.getSecondDefendantId(),
                witnessId,
                hearingOne.getHearingId()
        );

        //Initiate another hearing command with 1 case and 2 defendants
        InitiateHearingCommandHelper hearingTwo = new InitiateHearingCommandHelper(UseCases.initiateHearing(requestSpec,
                with(basicInitiateHearingTemplate(), command -> {

                    command.setCases(asList(
                            caseTemplate(caseId)
                    ));

                    command.getHearing().setDefendants(asList(
                            with(defendantTemplate(caseId), d -> {
                                d.setId(hearingOne.getFirstDefendantId());
                            }),
                            with(defendantTemplate(caseId), d -> {
                                d.setId(hearingOne.getSecondDefendantId());
                            })
                    ));
                })
        ));

        //I expect the new hearingTwo to have the witness information associated with each defendant.
        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingTwo.getHearingId()),
                "application/vnd.hearing.get.hearing.v2+json").withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingTwo.getHearingId().toString())),

                                withJsonPath("$.defenceWitnesses[0].id", is(witnessId.toString())),
                                withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                                withJsonPath("$.defenceWitnesses[0].classification", is(CLASSIFICATION_PROFESSIONAL)),
                                withJsonPath("$.defenceWitnesses[0].title", is(TITLE_MR)),
                                withJsonPath("$.defenceWitnesses[0].firstName", is(FIRSTNAME)),
                                withJsonPath("$.defenceWitnesses[0].lastName", is(LASTNAME)),
                                withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId",
                                        isOneOf(hearingTwo.getFirstDefendantId().toString(), hearingTwo.getSecondDefendantId().toString())
                                ),
                                withJsonPath("$.defenceWitnesses[0].defendants[1].defendantId",
                                        isOneOf(hearingTwo.getFirstDefendantId().toString(), hearingTwo.getSecondDefendantId().toString())
                                )
                        )));

        //Initiate another hearing command with 1 case and 1 defendant
        InitiateHearingCommandHelper hearingThree = new InitiateHearingCommandHelper(UseCases.initiateHearing(requestSpec,
                with(basicInitiateHearingTemplate(), command -> {

                    command.setCases(asList(
                            caseTemplate(caseId)
                    ));

                    command.getHearing().setDefendants(asList(
                            with(defendantTemplate(caseId), d -> {
                                d.setId(hearingOne.getSecondDefendantId());
                            })
                    ));
                })
        ));

        //I expect the new hearingThree to have the witness information associated with only the one defendant.
        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingThree.getHearingId()),
                "application/vnd.hearing.get.hearing.v2+json").withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingThree.getHearingId().toString())),

                                withJsonPath("$.defenceWitnesses[0].id", is(witnessId.toString())),
                                withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                                withJsonPath("$.defenceWitnesses[0].classification", is(CLASSIFICATION_PROFESSIONAL)),
                                withJsonPath("$.defenceWitnesses[0].title", is(TITLE_MR)),
                                withJsonPath("$.defenceWitnesses[0].firstName", is(FIRSTNAME)),
                                withJsonPath("$.defenceWitnesses[0].lastName", is(LASTNAME)),
                                withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId",
                                        isOneOf(hearingThree.getFirstDefendantId().toString())
                                )
                        )));
    }

    @Test
    public void shouldEnrichHearingWithPastWitnessesOneDefendantTwoWitnesses() {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(UseCases.initiateHearing(requestSpec, minimalInitiateHearingTemplate()));

        final UUID witnessIdOne = randomUUID();
        final UUID witnessIdTwo = randomUUID();

        addDefenceWitnessSingleDefendants(hearingOne.getFirstDefendantId(), witnessIdOne, hearingOne.getHearingId());
        addDefenceWitnessSingleDefendants(hearingOne.getFirstDefendantId(), witnessIdTwo, hearingOne.getHearingId());

        InitiateHearingCommandHelper hearingTwo = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec,
                        with(basicInitiateHearingTemplate(), command -> {

                            command.setCases(asList(caseTemplate(hearingOne.getFirstCaseId())));

                            command.getHearing().setDefendants(asList(
                                    with(defendantTemplate(hearingOne.getFirstCaseId()), d -> {
                                        d.setId(hearingOne.getFirstDefendantId());
                                    })
                            ));

                        })
                ));

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingTwo.getHearingId()),
                "application/vnd.hearing.get.hearing.v2+json").withHeader(CPP_UID_HEADER.getName(),
                CPP_UID_HEADER.getValue()).build()).timeout(30, TimeUnit.SECONDS).until(
                status().is(OK), print(),
                payload().isJson(allOf(withJsonPath("$.hearingId",
                        is(hearingTwo.getHearingId().toString())),

                        withJsonPath("$.cases[0].caseId", is(hearingTwo.getFirstCaseId().toString())),
                        withJsonPath("$.defenceWitnesses[0].id", isOneOf(witnessIdOne.toString(), witnessIdTwo.toString())),
                        withJsonPath("$.defenceWitnesses[0].type", is(TYPE)),
                        withJsonPath("$.defenceWitnesses[0].classification", is(CLASSIFICATION_PROFESSIONAL)),
                        withJsonPath("$.defenceWitnesses[0].title", is(TITLE_MR)),
                        withJsonPath("$.defenceWitnesses[0].firstName", is(FIRSTNAME)),
                        withJsonPath("$.defenceWitnesses[0].lastName", is(LASTNAME)),
                        withJsonPath("$.defenceWitnesses[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),

                        withJsonPath("$.defenceWitnesses[1].id", isOneOf(witnessIdOne.toString(), witnessIdTwo.toString())),
                        withJsonPath("$.defenceWitnesses[1].type", is(TYPE)),
                        withJsonPath("$.defenceWitnesses[1].classification", is(CLASSIFICATION_PROFESSIONAL)),
                        withJsonPath("$.defenceWitnesses[1].title", is(TITLE_MR)),
                        withJsonPath("$.defenceWitnesses[1].firstName", is(FIRSTNAME)),
                        withJsonPath("$.defenceWitnesses[1].lastName", is(LASTNAME)),
                        withJsonPath("$.defenceWitnesses[1].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString()))
                )));

    }

    private void addDefenceWitnessTwoDefendants(final UUID defendantIdOne,
                                                final UUID defendantIdTwo, final UUID witnessId, final UUID hearingId) {
        final JsonObject addWitness = createAddDefenceCommandMultipleDefendant(defendantIdOne,
                defendantIdTwo, witnessId, hearingId);

        invokeAddDefenceWitnessCommand(witnessId, hearingId, addWitness);
    }

    private void addDefenceWitnessSingleDefendants(final UUID defendantIdOne, final UUID witnessId,
                                                   final UUID hearingId) {
        final JsonObject addWitness =
                createAddDefenceCommandSingleDefendant(defendantIdOne, witnessId, hearingId);

        invokeAddDefenceWitnessCommand(witnessId, hearingId, addWitness);
    }

    private void invokeAddDefenceWitnessCommand(final UUID witnessId, final UUID hearingId,
                                                final JsonObject addWitness) {
        final String commandAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId.toString());

        final TestUtilities.EventListener publicEventWitnessAdded =
                listenFor("public.hearing.events.witness-added-updated").withFilter(isJson(
                        withJsonPath("$.witnessId", is(witnessId.toString()))));

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-update-witness+json")
                .body(addWitness.toString()).header(CPP_UID_HEADER).when()
                .post(commandAPIEndPoint).then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        publicEventWitnessAdded.waitFor();
    }

    private JsonObject createAddDefenceCommandMultipleDefendant(final UUID defendantIdOne,
                                                                final UUID defendantIdTwo, final UUID witnessId, final UUID hearingId) {
        return createObjectBuilder().add("id", witnessId.toString())
                .add("hearingId", hearingId.toString()).add("type", TYPE)
                .add("classification", CLASSIFICATION_PROFESSIONAL).add("title", TITLE_MR)
                .add("firstName", FIRSTNAME).add("lastName", LASTNAME)
                .add("defendants", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("defendantId", defendantIdOne.toString())
                                .build())
                        .add(createObjectBuilder()
                                .add("defendantId", defendantIdTwo.toString())
                                .build())
                        .build())
                .build();
    }

    private JsonObject createAddDefenceCommandSingleDefendant(final UUID defendantIdOne,
                                                              final UUID witnessId, final UUID hearingId) {
        return createObjectBuilder().add("id", witnessId.toString())
                .add("hearingId", hearingId.toString()).add("type", TYPE)
                .add("classification", CLASSIFICATION_PROFESSIONAL).add("title", TITLE_MR)
                .add("firstName", FIRSTNAME).add("lastName", LASTNAME)
                .add("defendants", createArrayBuilder().add(createObjectBuilder()
                        .add("defendantId", defendantIdOne.toString()).build())
                        .build())
                .build();
    }


}