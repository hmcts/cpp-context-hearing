package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.LAST_SHARED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SHARED_RESULT_LINES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.CourtClerkHelper.transformCourtClerk;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ResultLinesStatusUpdated implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject resultLinesStatusUpdated) {

        final JsonObjectBuilder transformResultLinesStatusUpdatedBuilder = createObjectBuilder()
                .add(HEARING_ID, resultLinesStatusUpdated.getString(HEARING_ID))
                .add(LAST_SHARED_DATE_TIME, resultLinesStatusUpdated.getString(LAST_SHARED_DATE_TIME))
                .add(SHARED_RESULT_LINES, resultLinesStatusUpdated.getJsonArray(SHARED_RESULT_LINES))
                .add(COURT_CLERK, transformCourtClerk(resultLinesStatusUpdated.getJsonObject(COURT_CLERK)));

        return transformResultLinesStatusUpdatedBuilder.build();
    }
}
