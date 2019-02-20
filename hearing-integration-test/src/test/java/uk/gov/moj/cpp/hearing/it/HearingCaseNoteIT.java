package uk.gov.moj.cpp.hearing.it;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.NoteType;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.jayway.restassured.response.Response;
import org.junit.Test;

public class HearingCaseNoteIT extends AbstractIT {

    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    @Test
    public void shouldSaveHearingCaseNote() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final String hearingId = hearing.getHearingId().toString();
        final JsonObjectBuilder hearingCaseNote = createObjectBuilder()
                .add("id", randomUUID().toString())
                .add("note", STRING.next())
                .add("noteDateTime", ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next()))
                .add("originatingHearingId", hearingId)
                .add("prosecutionCases", createArrayBuilder().add(randomUUID().toString()).add(randomUUID().toString()))
                .add("noteType", randomEnum(NoteType.class).next().toString())
                .add("courtClerk", createObjectBuilder()
                        .add("id", randomUUID().toString())
                        .add("firstName", STRING.next())
                        .add("lastName", STRING.next()));

        final JsonObject command = createObjectBuilder().add("hearingCaseNote", hearingCaseNote)
                .build();

        final String commandUrl = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.save-hearing-case-note"), hearingId);
        final Response response = given().spec(requestSpec)
                .and().contentType("application/vnd.hearing.save-hearing-case-note+json")
                .and().header(USER_ID, getLoggedInUser())
                .and().body(command.toString())
                .when().post(commandUrl)
                .then().extract().response();

        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));

        final JsonObject hearingCaseNoteCommand = command.getJsonObject("hearingCaseNote");
        poll(requestParams(getURL("hearing.get.hearing", hearingId),
                "application/vnd.hearing.get.hearing+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(30, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.id", is(hearingId)),
                                withJsonPath("$.hearing.hearingCaseNotes[*].note", contains(hearingCaseNoteCommand.getString("note"))),
                                withJsonPath("$.hearing.hearingCaseNotes[*].noteDateTime", contains(hearingCaseNoteCommand.getString("noteDateTime")))
                        ))
                );


    }
}
