package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.HearingHelper.transformHearing;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class HearingEventsInitiated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject jsonObject) {

        final JsonObjectBuilder transformedPayloadObjectBuilder = createObjectBuilder()
                .add(HEARING, transformHearing(jsonObject.getJsonObject(HEARING)));

        return transformedPayloadObjectBuilder.build();
    }
}
