package uk.gov.moj.cpp.hearing.test.matchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import uk.gov.justice.services.common.converter.JSONObjectValueObfuscator;
import uk.gov.justice.services.common.converter.exception.ConverterException;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonObject;
import java.io.IOException;

public class MappedToBeanMatcher<T> extends BaseMatcher<JsonEnvelope> {
    private Class<T> clz;
    private final Matcher<T> matcher;

    public MappedToBeanMatcher(Class<T> clz, Matcher<T> matcher) {
        this.clz = clz;
        this.matcher = matcher;
    }

    @Override
    public boolean matches(Object item) {
        JsonEnvelope jsonEnvelope = (JsonEnvelope) item;
        T subject = convert(clz, jsonEnvelope.payloadAsJsonObject());
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
                throw new ConverterException(String.format("Failed to convert %s to Object", JSONObjectValueObfuscator.obfuscated(source)));
            } else {
                return object;
            }
        } catch (IOException var4) {
            throw new IllegalArgumentException(String.format("Error while converting %s to JsonObject", JSONObjectValueObfuscator.obfuscated(source)), var4);
        }
    }

    public static <T> MappedToBeanMatcher<T> convertTo(Class<T> clazz, Matcher<T> matcher) {
        return new MappedToBeanMatcher<T>(clazz, matcher);
    }
}
