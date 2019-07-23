package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.DefendantHelper.transformDefendantUpdated;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class DefendantDetailsUpdated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject defendantDetailsUpdated) {

        final JsonObjectBuilder transformDefendantBuilder = createObjectBuilder()
                .add(DEFENDANT, transformDefendantUpdated(defendantDetailsUpdated.getJsonObject(DEFENDANT)))
                .add(HEARING_ID, defendantDetailsUpdated.getString(HEARING_ID));

        return transformDefendantBuilder.build();
    }
}
