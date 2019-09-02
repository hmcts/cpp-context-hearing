package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PROSECUTION_CASE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.OffenceHelper.transformOffence;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class OffenceAdded implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject offenceAdded) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(OFFENCE, transformOffence(offenceAdded.getJsonObject(OFFENCE)))
                .add(HEARING_ID, offenceAdded.getString(HEARING_ID))
                .add(DEFENDANT_ID, offenceAdded.getString(DEFENDANT_ID))
                .add(PROSECUTION_CASE_ID, offenceAdded.getString(PROSECUTION_CASE_ID));

        return transformOffenceBuilder.build();
    }
}
