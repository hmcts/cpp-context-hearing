package uk.gov.moj.cpp.hearing.domain.transformation.corechanges.transform;

import static javax.json.Json.createObjectBuilder;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.FIELD_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.FIELD_MASTER_DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_DEFENDANT_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_APPLICATION_DETAIL_CHANGED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_CASE_DEFENDANTS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_CASE_DEFENDANTS_UPDATED_FOR_HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_HEARING_EXTENDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_RESULTS_SHARED;

import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.transformation.corechanges.TransformUtil;

import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.slf4j.Logger;
public class MasterDefendantIdEventTransformer implements HearingEventTransformer {

    private static final Map<String, Pattern> eventAndJsonPaths = Collections.unmodifiableMap(
            Stream.of(new String[][]{
                    {HEARING_DEFENDANT_ADDED, "defendant"},
                    {HEARING_EVENTS_INITIATED, "hearing\\.prosecutionCases\\.\\d\\.defendants\\.\\d"},
                    {HEARING_EVENTS_NOWS_REQUESTED, "createNowsRequest\\.hearing\\.prosecutionCases\\.\\d\\.defendants\\.\\d"},
                    {HEARING_EVENTS_HEARING_EXTENDED, "courtApplication\\.applicant\\.defendant|courtApplication\\.respondents\\.\\d\\.partyDetails\\.defendant"},
                    {HEARING_EVENTS_PENDING_NOWS_REQUESTED, "createNowsRequest\\.hearing\\.prosecutionCases\\.\\d\\.defendants\\.\\d"},
                    {HEARING_RESULTS_SHARED, "hearing\\.prosecutionCases\\.\\d\\.defendants\\.\\d"},
                    {HEARING_EVENTS_APPLICATION_DETAIL_CHANGED, "courtApplication\\.applicant\\.defendant|courtApplication\\.respondents\\.\\d\\.partyDetails\\.defendant"},
                    {HEARING_EVENTS_CASE_DEFENDANTS_UPDATED, "prosecutionCase\\.defendants\\.\\d"},
                    {HEARING_EVENTS_CASE_DEFENDANTS_UPDATED_FOR_HEARING, "prosecutionCase\\.defendants\\.\\d"},
            }).collect(Collectors.toMap(data -> data[0], data -> Pattern.compile(data[1]))));

    private static final Logger LOGGER = getLogger(MasterDefendantIdEventTransformer.class);

    public static Map<String, Pattern> getEventAndJsonPaths() {
        return eventAndJsonPaths;
    }

    @Override
    public JsonObject transform(final Metadata eventMetadata, final JsonObject payload) {
        final JsonObjectBuilder transformedPayloadObjectBuilder;
        Pattern jsonPath = eventAndJsonPaths.get(eventMetadata.name().toLowerCase());

        final BiFunction<JsonValue, Deque<String>, Object> filter = (jsonValue, path) -> {
            if (!path.isEmpty() && match(jsonPath, path) && (jsonValue instanceof JsonObject)) {
                return defendantTransform((JsonObject) jsonValue);
            } else {
                return jsonValue;
            }
        };

        transformedPayloadObjectBuilder = TransformUtil.cloneObjectWithPathFilter(payload, filter);

        return transformedPayloadObjectBuilder.build();
    }

    public boolean match(final Pattern jsonPath, final Deque<String> path) {
        String pathMerged = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(path.descendingIterator(), 0),
                        false)
                .collect(Collectors.joining("."));
        return jsonPath.matcher(String.join(".", pathMerged)).matches();
    }


    private Object defendantTransform(final JsonObject jsonObject) {
        final JsonObjectBuilder result = createObjectBuilder();
        for (final Map.Entry<String, JsonValue> property : jsonObject.entrySet()) {
            String key = property.getKey();
            JsonValue value = property.getValue();
            result.add(key, value);
            if (key.equalsIgnoreCase(FIELD_ID)) {
                if (jsonObject.containsKey(FIELD_MASTER_DEFENDANT_ID)) {
                    LOGGER.warn("Defendant {} already have {} with value {} ", value, FIELD_MASTER_DEFENDANT_ID, jsonObject.getString(FIELD_MASTER_DEFENDANT_ID));
                } else {
                    result.add(FIELD_MASTER_DEFENDANT_ID, value);
                }
            }
        }
        return result;
    }

}
