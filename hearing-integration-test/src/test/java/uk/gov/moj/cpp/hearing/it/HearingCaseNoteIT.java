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

import uk.gov.justice.json.schemas.core.NoteType;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.text.MessageFormat;
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

//        final JsonValue command = fileResourceObjectMapper.convertFromFile("hearing-case-note-saved.json", JsonValue.class);
        /*
  "hearingCaseNote": {
    "id": "60e7f03c-dd61-4aab-86aa-2d83e9d8d9b6",
    "note": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam lorem risus, volutpat et rhoncus ut, ultricies eu lacus. Sed lacinia rutrum libero, vitae finibus neque suscipit at. Suspendisse interdum lectus blandit tempus gravida. Fusce nec justo suscipit, dictum diam maximus, congue lacus. Nullam augue lectus, scelerisque non arcu sed, rhoncus hendrerit eros. Morbi maximus rutrum dui sed faucibus. Quisque pretium urna nisl, sed congue tortor accumsan non. Duis fringilla ultrices vestibulum. Suspendisse potenti. Quisque aliquam ipsum sapien, ut posuere sapien aliquam non. Cras porta lacinia libero vitae finibus.",
    "noteDateTime": "2018-10-14T19:43:37+0100",
    "noteType": "LEGAL",
    "originatingHearingId": "e9b2ef55-5dc0-4eb1-b0e3-f22f31a0e5cb",
    "prosecutionCases": ["98ffe827-7a8a-4117-b3cf-b9548d6a42da", "c9de1f3e-0621-40c8-a12c-69eb5b735554"],
    "courtClerk": {
      "id": "8610ffbd-c68b-4d28-903e-bb49745948ea",
      "firstName": "Jane",
      "lastName": "Something"
    }
  }
         */

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final String hearingId = hearing.getHearingId().toString();
        final JsonObjectBuilder hearingCaseNote = createObjectBuilder()
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

//        JsonObject saveHearingCaseNotePayload = (JsonObject) UseCases.saveHearingCaseNote(requestSpec, randomUUID(), command);

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
