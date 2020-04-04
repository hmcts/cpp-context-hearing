package uk.gov.moj.cpp.hearing.domain.transformation.mot.util;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PLEA;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASE_ID;

@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
public class PleaHelper {

    private PleaHelper() {
    }


    public static JsonObject transformPlea(final JsonObject plea, final Map<String, String> valueMap) {
        //add required fields,
        final JsonObjectBuilder offenceBuilder = createObjectBuilder()
                .add(OFFENCE_ID, plea.getString(OFFENCE_ID))
                .add(PROSECUTION_CASE_ID, valueMap.get(PROSECUTION_CASE_ID))
                .add(DEFENDANT_ID, valueMap.get(DEFENDANT_ID))
                .add(PLEA, plea);
        return offenceBuilder.build();

    }

    public static JsonObject transformPlea(final JsonObject plea) {
        //add required fields,
        final JsonObjectBuilder offenceBuilder = createObjectBuilder()
                .add(OFFENCE_ID, plea.getString(OFFENCE_ID))
                .add(PLEA, plea);
        return offenceBuilder.build();
    }

}