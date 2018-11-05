package uk.gov.moj.cpp.hearing.test.matchers;

import uk.gov.justice.services.common.converter.JSONObjectValueObfuscator;
import uk.gov.justice.services.common.converter.exception.ConverterException;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class MapJsonObjectToTypeMatcher<T> extends BaseMatcher<JsonObject> {
    private Class<T> clz;
    private final Matcher<T> matcher;

    public MapJsonObjectToTypeMatcher(Class<T> clz, Matcher<T> matcher) {
        this.clz = clz;
        this.matcher = matcher;
    }

    @Override
    public boolean matches(Object item) {
        JsonObject jsonObject = (JsonObject) item;
        T subject = convert(clz, jsonObject);
        return this.matcher.matches(subject);
    }

    @Override
    public void describeTo(Description description) {
        this.matcher.describeTo(description);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        JsonEnvelope jsonEnvelope = (JsonEnvelope) item;
        T subject = convert(clz, jsonEnvelope.payloadAsJsonObject());
        this.matcher.describeMismatch(subject, description);
    }

    public static <T> T convert(Class<T> clazz, JsonObject source) {
        ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
        try {
            T object = mapper.readValue(mapper.writeValueAsString(source), clazz);
            if (object == null) {
                throw new ConverterException(String.format("Failed to convert %s to Object", source));
            } else {
                return object;
            }
        } catch (IOException var4) {
            throw new IllegalArgumentException(String.format("Error while converting %s to JsonObject", source), var4);
        }
    }

    public static <T> MapJsonObjectToTypeMatcher<T> convertTo(Class<T> clazz, Matcher<T> matcher) {
        return new MapJsonObjectToTypeMatcher<>(clazz, matcher);
    }

    public static <T> Matcher<JsonEnvelope> convertToEnvelopeMatcher(Class<T> clazz, Matcher<T> matcher) {
        final MapJsonObjectToTypeMatcher<T> underlyingMatcher = convertTo(clazz, matcher);
        return new BaseMatcher<JsonEnvelope>() {

            @Override
            public void describeTo(Description description) {
                underlyingMatcher.describeTo(description);
            }

            @Override
            public boolean matches(Object o) {
                if (o == null || !(o instanceof JsonEnvelope)) {
                    return false;
                }
                final JsonObject payload = ((JsonEnvelope) o).payloadAsJsonObject();
                return underlyingMatcher.matches(payload);
            }
        };
    }



}
