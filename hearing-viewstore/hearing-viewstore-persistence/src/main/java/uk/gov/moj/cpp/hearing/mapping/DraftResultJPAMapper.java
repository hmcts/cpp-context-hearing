package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.result.DraftResultV2;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DraftResult;

import java.io.IOException;
import java.io.StringReader;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DraftResultJPAMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DraftResultJPAMapper.class.getName());

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    public DraftResultV2 fromJPA(final DraftResult entity) {

        final JsonObject entityPayload = jsonFromString(entity.getDraftResultPayload().toString());
        return getDraft(entityPayload);
    }

    private DraftResultV2 getDraft(final JsonObject entityPayload) {
        try {
            return mapper.readValue(entityPayload.toString(), DraftResultV2.class);
        } catch (IOException e) {
            LOGGER.error("fail to create draft result response.", e);
        }
        return null;
    }

    private JsonObject jsonFromString(String jsonObjectStr) {
        JsonObject object;
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr))) {
            object = jsonReader.readObject();
        }

        return object;
    }
}
