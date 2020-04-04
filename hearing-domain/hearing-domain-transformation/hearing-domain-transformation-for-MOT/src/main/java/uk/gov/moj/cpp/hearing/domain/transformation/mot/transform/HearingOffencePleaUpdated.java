package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PLEA;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PLEA_MODEL;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.PleaHelper.transformPlea;

import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class HearingOffencePleaUpdated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject offencePleaUpdated) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(PLEA_MODEL, transformPlea(offencePleaUpdated.getJsonObject(PLEA)))
                .add(HEARING_ID, offencePleaUpdated.getString(HEARING_ID));

        return transformOffenceBuilder.build();
    }


    @Override
    public JsonObject transform(final JsonObject offencePleaUpdated, final Map<String, String> valueMap) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(HEARING_ID, offencePleaUpdated.getString(HEARING_ID));
        if(offencePleaUpdated.containsKey(PLEA)) {
            transformOffenceBuilder.add(PLEA_MODEL, transformPlea(offencePleaUpdated.getJsonObject(PLEA), valueMap));
        }

        return transformOffenceBuilder.build();
    }
}
