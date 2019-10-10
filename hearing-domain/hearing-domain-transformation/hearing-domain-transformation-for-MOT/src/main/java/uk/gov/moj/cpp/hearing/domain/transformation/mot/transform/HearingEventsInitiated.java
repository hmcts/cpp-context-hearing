package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.HearingHelper.transformHearing;

public class HearingEventsInitiated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject jsonObject) {

        final JsonObjectBuilder transformedPayloadObjectBuilder = createObjectBuilder()
                .add(HEARING, transformHearing(jsonObject.getJsonObject(HEARING)));

        return transformedPayloadObjectBuilder.build();
    }

    @Override
    public JsonObject transform(final JsonObject jsonObject, final Map<String, String> valueMap) {

        final JsonObjectBuilder transformedPayloadObjectBuilder = createObjectBuilder()
                .add(HEARING, transformHearing(jsonObject.getJsonObject(HEARING)));

        if (jsonObject.getJsonObject(HEARING).containsKey(PROSECUTION_CASES)) {
            valueMap.put(PROSECUTION_CASE_ID, jsonObject.getJsonObject(HEARING).getJsonArray(PROSECUTION_CASES).getJsonObject(0).getString(ID));
            valueMap.put(DEFENDANT_ID, jsonObject.getJsonObject(HEARING)
                    .getJsonArray(PROSECUTION_CASES).getJsonObject(0)
                    .getJsonArray(DEFENDANTS).getJsonObject(0).getString(ID));
        }
        return transformedPayloadObjectBuilder.build();
    }
}
