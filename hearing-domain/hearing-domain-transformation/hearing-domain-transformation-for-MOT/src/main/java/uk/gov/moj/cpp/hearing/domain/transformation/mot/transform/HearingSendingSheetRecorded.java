package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CROWN_COURT_HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.HearingHelper.transformHearingForSendingSheet;

public class HearingSendingSheetRecorded implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject jsonObject) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(HEARING, transformHearingForSendingSheet(jsonObject.getJsonObject(HEARING)))
                .add(CROWN_COURT_HEARING, jsonObject.getJsonObject(CROWN_COURT_HEARING));

        return transformOffenceBuilder.build();
    }
}
