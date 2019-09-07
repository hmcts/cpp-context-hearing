package uk.gov.moj.cpp.hearing.domain.transformation.mot.util;

import uk.gov.justice.core.courts.ProsecutionCase;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PLEA;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASE_ID;

@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
public class PleaHelper {

    private PleaHelper() {
    }


    public static JsonObject transformPlea(final JsonObject plea, final ProsecutionCase prosecutionCase) {
        //add required fields,
        final JsonObjectBuilder offenceBuilder = createObjectBuilder()
                .add(OFFENCE_ID, plea.getString(OFFENCE_ID))
                .add(PROSECUTION_CASE_ID, prosecutionCase.getId().toString())
                .add(DEFENDANT_ID, prosecutionCase.getDefendants().get(0).getId().toString())
                .add(PLEA, plea);

        return offenceBuilder.build();

    }

}