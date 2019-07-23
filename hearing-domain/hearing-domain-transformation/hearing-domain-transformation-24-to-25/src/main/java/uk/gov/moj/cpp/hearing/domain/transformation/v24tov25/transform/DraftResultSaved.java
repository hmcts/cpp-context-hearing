package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TARGET;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.TargetHelper.transformTarget;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class DraftResultSaved implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject draftResultSaved) {
        final JsonObjectBuilder transformDraftResultBuilder = createObjectBuilder()
                .add(TARGET, transformTarget(draftResultSaved.getJsonObject(TARGET)));

        return transformDraftResultBuilder.build();
    }
}
