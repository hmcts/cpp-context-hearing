package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import uk.gov.justice.core.courts.ProsecutionCase;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PLEA;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PLEA_MODEL;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.PleaHelper.transformPlea;

public class HearingOffencePleaUpdated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject offencePleaUpdated, Map<String, ProsecutionCase> hearingMap) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(PLEA_MODEL, transformPlea(offencePleaUpdated.getJsonObject(PLEA), hearingMap.get(offencePleaUpdated.getString(HEARING_ID))))
                .add(HEARING_ID, offencePleaUpdated.getString(HEARING_ID));

        return transformOffenceBuilder.build();
    }

}
