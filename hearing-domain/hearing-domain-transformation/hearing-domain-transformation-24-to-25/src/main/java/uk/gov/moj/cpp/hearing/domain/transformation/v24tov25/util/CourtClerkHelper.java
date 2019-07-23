package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.FIRST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.LAST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.USER_ID;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class CourtClerkHelper {

    private CourtClerkHelper() {

    }

    public static JsonObject transformCourtClerk(final JsonObject courtClerk) {

        final JsonObjectBuilder transformCourtClerkBuilder = createObjectBuilder()
                .add(USER_ID, courtClerk.getString(ID))
                .add(FIRST_NAME, courtClerk.getString(FIRST_NAME))
                .add(LAST_NAME, courtClerk.getString(LAST_NAME));

        return transformCourtClerkBuilder.build();
    }
}
