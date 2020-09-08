package uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.service;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.STRING;

import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPayloadTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPayloadTransformer.class);

    private static final String SLOTS_ATTRIBUTE = "slots";
    private static final String COURT_SCHEDULE_ID = "courtScheduleId";
    private static final String HEARING_ID = "hearingId";

    public JsonObject transform(final JsonObject payload) {
        LOGGER.debug("Before: Payload '{}'", payload);

        final JsonObjectBuilder targetPayload = createObjectBuilder();

        for (final Map.Entry<String, JsonValue> property : payload.entrySet()) {
            final String key = property.getKey();
            final JsonValue value = property.getValue();

            if(SLOTS_ATTRIBUTE.equals(key)) {
                if(ARRAY.equals(value.getValueType())) {
                    targetPayload.add(SLOTS_ATTRIBUTE, createTargetSlots(value));
                }
            } else if (HEARING_ID.equals(key)) {
                targetPayload.add(HEARING_ID, value);
            }
        }
        final JsonObject result = targetPayload.build();
        LOGGER.debug("After: Payload '{}'", result);
        return result;
    }

    private JsonArrayBuilder createTargetSlots(final JsonValue value) {
        final JsonArrayBuilder slotsBuilder = createArrayBuilder();
        final JsonArray slots = (JsonArray) value;
        slots.iterator().forEachRemaining(courtScheduleId -> {
            if(STRING.equals(courtScheduleId.getValueType())) {
                slotsBuilder.add(createObjectBuilder().add(COURT_SCHEDULE_ID, courtScheduleId));
            }
        });
        return slotsBuilder;
    }
}