package uk.gov.moj.cpp.hearing.event;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelopeProvider;

import java.io.StringReader;
import java.util.List;

import javax.json.Json;

import com.fasterxml.jackson.core.JsonProcessingException;

public class Framework5Fix {

    //TODO THIS CLASS IS USED TO ENSURE THE NEW ENVELOPER CALLS ARGUMENTS CAN BE VERIFIED

    private static final ObjectMapperProducer objectMapperProducer = new ObjectMapperProducer();
    private static final DefaultJsonEnvelopeProvider defaultJsonEnvelopeProvider = new DefaultJsonEnvelopeProvider();

    private Framework5Fix() {
        //private
    }

    public static List<JsonEnvelope> standardizeJsonEnvelopes(final List<Object> allValues) {
        return allValues.stream().map(Framework5Fix::toJsonEnvelope).collect(toList());
    }

    public static JsonEnvelope toJsonEnvelope(Object env) {
        if (env instanceof JsonEnvelope) {
            return (JsonEnvelope) env;
        } else if (env instanceof DefaultEnvelope) {
            final DefaultEnvelope e = (DefaultEnvelope) env;
            final String jsonString;
            try {
                jsonString = objectMapperProducer.objectMapper().writeValueAsString(e.payload());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
            return defaultJsonEnvelopeProvider.envelopeFrom(e.metadata(), Json.createReader(new StringReader(jsonString)).readObject());
        } else {
            throw new IllegalArgumentException("don't know how to convert this");
        }
    }
}
