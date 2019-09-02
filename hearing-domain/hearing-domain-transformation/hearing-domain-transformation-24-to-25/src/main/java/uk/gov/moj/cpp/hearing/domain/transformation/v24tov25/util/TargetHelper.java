package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.APPLICATION_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DRAFT_RESULT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.RESULT_LINES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TARGET_ID;

import java.util.stream.IntStream;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class TargetHelper {

    private TargetHelper() {

    }

    public static JsonArray transformTargets(final JsonArray targets) {

        final JsonArrayBuilder transformedPayloadObjectBuilder = createArrayBuilder();

        IntStream.range(0, targets.size())
                .mapToObj(index -> transformTarget(targets.getJsonObject(index)))
                .forEach(transformedPayloadObjectBuilder::add);

        return transformedPayloadObjectBuilder.build();
    }

    public static JsonObject transformTarget(final JsonObject target) {

        final JsonObjectBuilder transformDraftResultBuilder = createObjectBuilder()
                .add(HEARING_ID, target.getString(HEARING_ID))
                .add(TARGET_ID, target.getString(TARGET_ID));

        if (target.containsKey(DEFENDANT_ID)) {
            transformDraftResultBuilder.add(DEFENDANT_ID, target.getString(DEFENDANT_ID));
        }

        if (target.containsKey(OFFENCE_ID)) {
            transformDraftResultBuilder.add(OFFENCE_ID, target.getString(OFFENCE_ID));
        }

        if (target.containsKey(APPLICATION_ID)) {
            transformDraftResultBuilder.add(APPLICATION_ID, target.getString(APPLICATION_ID));
        }

        if (target.containsKey(DRAFT_RESULT)) {
            transformDraftResultBuilder.add(DRAFT_RESULT, target.getString(DRAFT_RESULT));
        }

        //For Draft result saved, result lines are always empty array.
        if (target.containsKey(RESULT_LINES)) {
            transformDraftResultBuilder.add(RESULT_LINES, target.getJsonArray(RESULT_LINES));
        }

        return transformDraftResultBuilder.build();
    }
}
