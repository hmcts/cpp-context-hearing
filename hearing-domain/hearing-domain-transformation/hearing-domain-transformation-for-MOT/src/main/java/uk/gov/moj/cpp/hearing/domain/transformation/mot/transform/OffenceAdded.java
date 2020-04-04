package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.OffenceHelper.transformOffence;

public class OffenceAdded implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject offenceAdded) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(OFFENCE, transformOffence(offenceAdded.getJsonObject(OFFENCE),offenceAdded.getString(HEARING_ID)))
                .add(HEARING_ID, offenceAdded.getString(HEARING_ID))
                .add(DEFENDANT_ID, offenceAdded.getString(DEFENDANT_ID))
                .add(PROSECUTION_CASE_ID, offenceAdded.getString(PROSECUTION_CASE_ID));

        return transformOffenceBuilder.build();
    }
}
