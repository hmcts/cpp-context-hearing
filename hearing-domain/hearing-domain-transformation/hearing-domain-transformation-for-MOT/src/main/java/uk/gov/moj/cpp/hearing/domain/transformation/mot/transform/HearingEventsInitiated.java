package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.HearingHelper.transformHearing;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

public class HearingEventsInitiated implements EventInstance {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Override
    public JsonObject transform(final JsonObject jsonObject, Map<String, ProsecutionCase> hearingMap) {

        final JsonObjectBuilder transformedPayloadObjectBuilder = createObjectBuilder()
                .add(HEARING, transformHearing(jsonObject.getJsonObject(HEARING)));

        if (jsonObject.getJsonObject(HEARING).containsKey(PROSECUTION_CASES)) {
            final ProsecutionCase prosecutionCase = this.jsonObjectToObjectConverter.convert(jsonObject.getJsonObject(HEARING).getJsonArray(PROSECUTION_CASES).getJsonObject(0), ProsecutionCase.class);
            hearingMap.put(jsonObject.getJsonObject(HEARING).getString(ID), prosecutionCase);
        }
        return transformedPayloadObjectBuilder.build();
    }
}
