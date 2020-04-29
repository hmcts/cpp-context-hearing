package uk.gov.moj.cpp.hearing.it;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Optional.ofNullable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.ENDPOINT_PROPERTIES;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.retrieveMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_NOT_HAPPENED_TIMEOUT_IN_MILLIS;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.MapJsonObjectToTypeMatcher;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.UUID;

import javax.jms.MessageConsumer;
import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utilities {

    public static EventListener listenFor(String mediaType) {
        return new EventListener(mediaType);
    }

    public static EventListener listenFor(String mediaType, long timeout) {
        return new EventListener(mediaType, timeout);
    }

    public static CommandBuilder makeCommand(RequestSpecification requestSpec, String endpoint) {
        return new CommandBuilder(requestSpec, endpoint);
    }

    public static class EventListener {

        private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);
        private MessageConsumer messageConsumer;
        private String eventType;
        private Matcher<?> matcher;
        private long timeout;

        public EventListener(final String eventType) {
            this(eventType, DEFAULT_NOT_HAPPENED_TIMEOUT_IN_MILLIS);
        }

        public EventListener(final String eventType, long timeout) {
            this.eventType = eventType;
            this.messageConsumer = getPublicTopicInstance().createConsumer(eventType);
            this.timeout = timeout;
        }

        public void expectNone() {
            expectNoneWithin(timeout);
        }

        public void expectNoneWithin(long timeout) {

            JsonPath message = retrieveMessage(messageConsumer, timeout);

            while (message != null && !this.matcher.matches(message.prettify())) {
                message = retrieveMessage(messageConsumer);
            }
            if (message != null) {
                fail("expected no messages");
            }
        }

        public JsonPath waitFor() {
            JsonPath message = retrieveMessage(messageConsumer, timeout);
            StringDescription description = new StringDescription();

            while (message != null && !this.matcher.matches(message.prettify())) {
                description = new StringDescription();
                description.appendText("Expected ");
                this.matcher.describeTo(description);
                description.appendText(" but ");
                this.matcher.describeMismatch(message.prettify(), description);

                message = retrieveMessage(messageConsumer);
            }

            if (message == null) {
                fail("Expected '" + eventType + "' message to emit on the public.event topic: " + description.toString());
            } else {
                LOGGER.info("message:" + message.prettify());
            }

            return message;
        }

        public EventListener withFilter(Matcher<?> matcher) {
            this.matcher = matcher;
            return this;
        }

        public EventListener withFilter(final BeanMatcher<?> beanMatcher, final String expectedMetaDataName, final String expectedMetaDataContextUser) {

            this.matcher = new BaseMatcher() {
                Object beanValue;
                String metadataError = null;

                @Override
                public boolean matches(final Object o) {
                    final JSONObject jsonObjectMutable = new JSONObject(o.toString());
                    final JSONObject metadata = jsonObjectMutable.getJSONObject("_metadata");
                    final String actualMetadataContextUser = metadata != null ? metadata.getJSONObject("context").getString("user") : "";
                    final String actualMetadataName = metadata != null ? metadata.getString("name") : "";
                    if (!actualMetadataName.equals(expectedMetaDataName) || !actualMetadataContextUser.equals(expectedMetaDataContextUser)) {
                        metadataError = String.format("actual metadata user/name %s/%s doesnt match expected %s/%s ", actualMetadataContextUser, actualMetadataName, expectedMetaDataContextUser, expectedMetaDataName);
                        return false;
                    }

                    jsonObjectMutable.remove("_metadata");
                    JsonObject jsonObject = Json.createReader(new StringReader(jsonObjectMutable.toString())).readObject();
                    beanValue = MapJsonObjectToTypeMatcher.convert(PublicHearingDraftResultSaved.class, jsonObject);
                    return beanMatcher.matches(beanValue);
                }

                @Override
                public void describeMismatch(Object item, Description description) {
                    if (metadataError != null) {
                        description.appendText(metadataError);
                    } else {
                        beanMatcher.describeMismatch(beanValue, description);
                    }
                }

                @Override
                public void describeTo(Description description) {
                    beanMatcher.describeTo(description);
                }

            };

            return this;
        }
    }

    public static class JsonUtil {

        public static ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper;
        }

        public static String toJsonString(final Object o) throws JsonProcessingException {
            return objectMapper().writeValueAsString(o);
        }

        public static <T> T fromJsonString(final String str, Class<T> theClass) throws IOException {
            return objectMapper().reader().forType(theClass).readValue(str);
        }

        public static JsonObject objectToJsonObject(final Object o) throws JsonProcessingException {
            return (new StringToJsonObjectConverter()).convert(toJsonString(o));
        }
    }

    public static class CommandBuilder {
        private static final Logger LOGGER = LoggerFactory.getLogger(CommandBuilder.class);

        private RequestSpecification requestSpec;
        private String endpoint;
        private String type;
        private String payloadAsString;
        private UUID cppUserId;
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

        public CommandBuilder withPayload(final String payload) {
            this.payloadAsString = payload;
            LOGGER.info("Command Payload: {}", payloadAsString);
            return this;
        }

        public CommandBuilder withCppUserId(final UUID cppUserId) {
            this.cppUserId = cppUserId;
            return this;
        }


        public CommandBuilder withPayload(final Object payload) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                this.payloadAsString = JsonUtil.toJsonString(payload);

                LOGGER.info("Command Payload: {}", payloadAsString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return this;
        }

        public void executeSuccessfully() {

            String url = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty(endpoint), arguments);
            LOGGER.info("Command url: {}", url);

            Response writeResponse = given().spec(requestSpec).and()
                    .contentType(type)
                    .accept(type)
                    .body(ofNullable(this.payloadAsString).orElse(""))
                    .header(new Header(USER_ID, cppUserId != null ? cppUserId.toString() : AbstractIT.getLoggedInUser().toString())).when()
                    .post(url)
                    .then().extract().response();
            assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        }
    }


}
