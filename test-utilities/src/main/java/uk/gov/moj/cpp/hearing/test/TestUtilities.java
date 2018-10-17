package uk.gov.moj.cpp.hearing.test;

import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.internal.JsonContext;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;

public class TestUtilities {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperProducer().objectMapper();

    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> asList(T... a) {
        return new ArrayList<>(Arrays.asList(a));
    }

    public static <T> T at(Collection<T> item, int index) {
        final Iterator<T> it = item.iterator();
        T o = null;
        for (int i = 0; i <= index; i++) {
            o = it.next();
        }
        return o;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Set<T> asSet(T... a) {
        return new HashSet<>(Arrays.asList(a));
    }

    public static <T> Matcher<T> print() {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof JsonContext) {
                    JsonContext jsonContext = (JsonContext) o;
                    System.out.println(jsonContext.jsonString());
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                //not required
            }
        };

    }

    public static Metadata metadataFor(final String commandName, final UUID commandId) {
        return metadataBuilder()
                .withName(commandName)
                .withId(commandId)
                .build();
    }

    public static void matchEvent(final Stream<JsonEnvelope> jsonEnvelopeStream,
                                  final String eventName,
                                  final JsonValue expectedResultPayload) {

        boolean matched = false;

        for (final JsonEnvelope jsonEnvelope : jsonEnvelopeStream.collect(Collectors.toList())) {
            if (jsonEnvelope.metadata().name().equals(eventName)) {
                matched = true;
                final JsonNode actualEvent = generatedEventAsJsonNode(jsonEnvelope.payloadAsJsonObject());
                Assert.assertThat(actualEvent, CoreMatchers.equalTo(generatedEventAsJsonNode(expectedResultPayload)));
                break;
            }
        }

        assertTrue(matched);
    }

    public static JsonNode generatedEventAsJsonNode(final Object generatedEvent) {
        return OBJECT_MAPPER.valueToTree(generatedEvent);
    }
}
