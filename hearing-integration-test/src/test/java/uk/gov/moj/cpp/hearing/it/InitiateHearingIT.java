package uk.gov.moj.cpp.hearing.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;

import javax.jms.MessageConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.retrieveMessage;

public class InitiateHearingIT extends AbstractIT {


    @Test
    public void initiateHearing_shouldInitiateHearing_whenInitiateHearingCommandIsMade() {


        InitiateHearingCommand initiateHearing = initiateHearingCommandTemplate().build();

        EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString()))));

        makeCommand("hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        JsonPath message = publicEventTopic.waitFor();


        System.out.println(message.prettify());

        //TODO assert projection here.


        //TODO - add query.
    }


    public static InitiateHearingCommand.Builder initiateHearingCommandTemplate() {
        UUID caseId = randomUUID();
        System.out.println("caseID==" + caseId);

        return InitiateHearingCommand.builder()
                .addCase(Case.builder()
                        .withCaseId(caseId)
                        .withUrn(STRING.next())
                )
                .withHearing(
                        Hearing.builder()
                                .withId(randomUUID())
                                .withType(STRING.next())
                                .withCourtCentreId(randomUUID())
                                .withCourtCentreName(STRING.next())
                                .withCourtRoomId(randomUUID())
                                .withCourtRoomName(STRING.next())
                                .withJudge(
                                        Judge.builder()
                                                .withId(randomUUID())
                                                .withTitle(STRING.next())
                                                .withFirstName(STRING.next())
                                                .withLastName(STRING.next())
                                )
                                .withStartDateTime(FUTURE_LOCAL_DATE.next())
                                .withNotBefore(false)
                                .withEstimateMinutes(INTEGER.next())
                                .addDefendant(Defendant.builder()
                                        .withId(randomUUID())
                                        .withPersonId(randomUUID())
                                        .withFirstName(STRING.next())
                                        .withLastName(STRING.next())
                                        .withNationality(STRING.next())
                                        .withGender(STRING.next())
                                        .withAddress(
                                                Address.builder()
                                                        .withAddress1(STRING.next())
                                                        .withAddress2(STRING.next())
                                                        .withAddress3(STRING.next())
                                                        .withAddress4(STRING.next())
                                                        .withPostCode(STRING.next())
                                        )
                                        .withDateOfBirth(PAST_LOCAL_DATE.next())
                                        .withDefenceOrganisation(STRING.next())
                                        .withInterpreter(
                                                Interpreter.builder()
                                                        .withNeeded(false)
                                                        .withLanguage(STRING.next())
                                        )
                                        .addDefendantCase(
                                                DefendantCase.builder()
                                                        .withCaseId(caseId)
                                                        .withBailStatus(STRING.next())
                                                        .withCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next())
                                        )
                                        .addOffence(
                                                Offence.builder()
                                                        .withId(randomUUID())
                                                        .withCaseId(caseId)
                                                        .withOffenceCode(STRING.next())
                                                        .withWording(STRING.next())
                                                        .withSection(STRING.next())
                                                        .withStartDate(PAST_LOCAL_DATE.next())
                                                        .withEndDate(PAST_LOCAL_DATE.next())
                                                        .withOrderIndex(INTEGER.next())
                                                        .withCount(INTEGER.next())
                                                        .withConvictionDate(PAST_LOCAL_DATE.next())
                                        )
                                )
                );
    }


    private static class EventListener {

        private MessageConsumer messageConsumer;
        private String eventType;
        private List<Matcher<Object>> matchers = new ArrayList<>();

        public EventListener(final String eventType) {
            this.eventType = eventType;
            this.messageConsumer = publicEvents.createConsumer(eventType);
        }

        private boolean matches(String json) {
            return matchers.stream().allMatch(m -> m.matches(json));
        }

        public JsonPath waitFor() {

            JsonPath message = retrieveMessage(messageConsumer);

            while (message != null && !matches(message.prettify())) {
                message = retrieveMessage(messageConsumer);
            }
            if (message == null) {
                fail("Expected '" + eventType + "' message to emit on the public.event topic.");
            }
            return message;
        }

        public EventListener withFilter(Matcher<Object> matcher) {
            this.matchers.add(matcher);
            return this;
        }
    }

    public static EventListener listenFor(String mediaType) {
        return new EventListener(mediaType);
    }


    public static class CommandBuilder {
        private String endpoint;
        private String type;
        private Object payload;

        public CommandBuilder(final String endpoint) {
            this.endpoint = endpoint;
        }

        public CommandBuilder ofType(final String type) {
            this.type = type;
            return this;
        }

        public CommandBuilder withPayload(final Object payload) {
            this.payload = payload;
            return this;
        }

        public void executeSuccessfully() {

            ObjectMapper mapper = new ObjectMapper();


            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String output = "";
            try {
                output = mapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            Response writeResponse = given().spec(requestSpec).and()
                    .contentType(type)
                    .body(output).header(CPP_UID_HEADER).when()
                    .post(ENDPOINT_PROPERTIES.getProperty(endpoint))
                    .then().extract().response();
            assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        }
    }

    public static CommandBuilder makeCommand(String endpoint) {
        return new CommandBuilder(endpoint);
    }
}