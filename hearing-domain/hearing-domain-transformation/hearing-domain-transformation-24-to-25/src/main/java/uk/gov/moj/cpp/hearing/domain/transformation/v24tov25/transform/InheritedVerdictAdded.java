package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.VERDICT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.OffenceHelper.transformVerdict;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class InheritedVerdictAdded implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject inheritedVerdictAdded) {
        final JsonObjectBuilder transformVerdictBuilder = createObjectBuilder()
                .add(VERDICT, transformVerdict(inheritedVerdictAdded.getJsonObject(VERDICT)))
                .add(HEARING_ID, inheritedVerdictAdded.getString(HEARING_ID));

        return transformVerdictBuilder.build();
    }
}
