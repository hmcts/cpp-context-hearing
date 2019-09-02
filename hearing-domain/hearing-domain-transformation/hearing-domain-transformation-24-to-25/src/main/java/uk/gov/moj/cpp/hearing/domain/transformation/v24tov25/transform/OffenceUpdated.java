package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.OffenceHelper.transformOffence;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class OffenceUpdated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject offenceUpdated) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(OFFENCE, transformOffence(offenceUpdated.getJsonObject(OFFENCE)))
                .add(HEARING_ID, offenceUpdated.getString(HEARING_ID))
                .add(DEFENDANT_ID, offenceUpdated.getString(DEFENDANT_ID));

        return transformOffenceBuilder.build();
    }
}
