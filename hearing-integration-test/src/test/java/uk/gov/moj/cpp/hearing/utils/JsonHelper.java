package uk.gov.moj.cpp.hearing.utils;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.json.JsonObject;

public class JsonHelper {

    public static <T> T fromJsonString(final String json, Class<T> type) {
        try {
            return new ObjectMapperProducer().objectMapper().readValue(json, type);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJsonObject(final JsonObject json, Class<T> type) {
        return fromJsonString(json.toString(), type);
    }
}
