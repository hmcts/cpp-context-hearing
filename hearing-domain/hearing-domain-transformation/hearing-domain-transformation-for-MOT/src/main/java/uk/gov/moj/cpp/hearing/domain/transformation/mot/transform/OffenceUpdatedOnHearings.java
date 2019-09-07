package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import uk.gov.justice.core.courts.ProsecutionCase;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import java.util.Map;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_IDS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.OffenceHelper.transformOffence;

public class OffenceUpdatedOnHearings implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject offenceUpdatedOnHearings, Map<String, ProsecutionCase> hearingMap) {
        final JsonObjectBuilder transformOffenceBuilder = createObjectBuilder()
                .add(OFFENCE, transformOffence(offenceUpdatedOnHearings.getJsonObject(OFFENCE), offenceUpdatedOnHearings.getJsonArray(HEARING_IDS).get(0).toString()))
                .add(HEARING_IDS, offenceUpdatedOnHearings.getJsonArray(HEARING_IDS))
                .add(DEFENDANT_ID, offenceUpdatedOnHearings.getString(DEFENDANT_ID));

        return transformOffenceBuilder.build();
    }
}
