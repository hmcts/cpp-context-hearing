package uk.gov.moj.cpp.hearing.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.CPP_UID_HEADER;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.ENDPOINT_PROPERTIES;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.retrieveMessage;

public class TestUtilities {


    public static InitiateHearingCommand.Builder initiateHearingCommandTemplate() {

        UUID caseId = randomUUID();

        return InitiateHearingCommand.builder()
                .addCase(Case.builder()
                        .withCaseId(caseId)
                        .withUrn(STRING.next())
                )
                .withHearing(Hearing.builder()
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
                        .withStartDateTime(FUTURE_ZONED_DATE_TIME.next())
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
                                                .withCustodyTimeLimitDate(FUTURE_ZONED_DATE_TIME.next())
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
                                                .withTitle(STRING.next())
                                                .withLegislation(STRING.next())
                                )
                        )
                );
    }

    public static InitiateHearingCommand.Builder initiateHearingCommandTemplateWithOnlyMandatoryFields() {

        UUID caseId = randomUUID();

        return InitiateHearingCommand.builder()
                .addCase(Case.builder()
                        .withCaseId(caseId)
                        .withUrn(STRING.next())
                )
                .withHearing(Hearing.builder()
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
                        .withStartDateTime(FUTURE_ZONED_DATE_TIME.next())
                        .withEstimateMinutes(INTEGER.next())
                        .addDefendant(Defendant.builder()
                                .withId(randomUUID())
                                .withPersonId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .addDefendantCase(
                                        DefendantCase.builder()
                                                .withCaseId(caseId)
                                )
                                .addOffence(
                                        Offence.builder()
                                                .withId(randomUUID())
                                                .withCaseId(caseId)
                                                .withOffenceCode(STRING.next())
                                                .withWording(STRING.next())
                                                .withStartDate(PAST_LOCAL_DATE.next())
                                )
                        )
                );
    }


    public static class EventListener {

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
        private RequestSpecification requestSpec;
        private String endpoint;
        private String type;
        private Object payload;
        private Object[] arguments = new Object[0];

        public CommandBuilder(RequestSpecification requestSpec, String endpoint) {
            this.requestSpec = requestSpec;
            this.endpoint = endpoint;
        }

        public CommandBuilder withArgs(Object... args) {
            this.arguments = args;
            return this;
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

            String url = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty(endpoint), arguments);
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String output = "";
            try {
                output = mapper.writeValueAsString(payload);
                System.out.println("Command Payload:");
                System.out.println(output);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            System.out.println(url);
            System.out.println(output);
            Response writeResponse = given().spec(requestSpec).and()
                    .contentType(type)
                    .body(output).header(CPP_UID_HEADER).when()
                    .post(url)
                    .then().extract().response();
            assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        }
    }

    public static CommandBuilder makeCommand(RequestSpecification requestSpec, String endpoint) {
        return new CommandBuilder(requestSpec, endpoint);
    }

    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }
}
