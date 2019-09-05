package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import uk.gov.justice.core.courts.ProsecutionCase;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.OffenceHelper.transformOffence;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

public class OffenceUpdated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject offenceUpdated, Map<String, ProsecutionCase> hearingMap) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(OFFENCE, transformOffence(offenceUpdated.getJsonObject(OFFENCE), offenceUpdated.getString(HEARING_ID)))
                .add(HEARING_ID, offenceUpdated.getString(HEARING_ID))
                .add(DEFENDANT_ID, offenceUpdated.getString(DEFENDANT_ID));

        return transformOffenceBuilder.build();
    }

}
