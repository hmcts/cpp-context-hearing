package uk.gov.moj.cpp.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CreateJsonSampleUtil {
    public static final String HEARING_COMMAND_HEARING_COMMAND_API_SRC_RAML_JSON = "./hearing-command/hearing-command-api/src/raml/json";
    public static final String HEARING_COMMAND_HEARING_COMMAND_HANDLER_SRC_RAML_JSON = "./hearing-command/hearing-command-handler/src/raml/json";
    public static final String HEARING_EVENT_HEARING_EVENT_LISTENER_SRC_RAML_JSON = "./hearing-event/hearing-event-listener/src/raml/json";
    public static final String HEARING_EVENT_HEARING_EVENT_PROCESSOR_SRC_RAML_JSON = "./hearing-event/hearing-event-processor/src/raml/json";

    private CreateJsonSampleUtil() {

    }

    public static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        return objectMapper;
    }

}

