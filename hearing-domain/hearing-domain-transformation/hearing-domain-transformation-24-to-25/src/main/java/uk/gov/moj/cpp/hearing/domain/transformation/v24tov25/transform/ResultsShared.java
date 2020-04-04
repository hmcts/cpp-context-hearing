package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COMPLETED_RESULT_LINES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SHARED_TIME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.UNCOMPLETED_RESULT_LINES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.VARIANT_DIRECTORY;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.CourtClerkHelper.transformCourtClerk;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.HearingHelper.transformHearing;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ResultsShared implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject resultsShared) {

        final JsonObjectBuilder transformResultsSharedBuilder = createObjectBuilder()
                .add(HEARING_ID, resultsShared.getString(HEARING_ID))
                .add(SHARED_TIME, resultsShared.getString(SHARED_TIME))
                .add(COURT_CLERK, transformCourtClerk(resultsShared.getJsonObject(COURT_CLERK)));

        if(resultsShared.containsKey(UNCOMPLETED_RESULT_LINES)) {
            transformResultsSharedBuilder.add(UNCOMPLETED_RESULT_LINES, resultsShared.getJsonObject(UNCOMPLETED_RESULT_LINES));
        }

        if(resultsShared.containsKey(COMPLETED_RESULT_LINES)) {
            transformResultsSharedBuilder.add(COMPLETED_RESULT_LINES, resultsShared.getJsonObject(COMPLETED_RESULT_LINES));
        }

        if(resultsShared.containsKey(HEARING)) {
            transformResultsSharedBuilder.add(HEARING, transformHearing(resultsShared.getJsonObject(HEARING)));
        }

        if(resultsShared.containsKey(VARIANT_DIRECTORY)) {
            transformResultsSharedBuilder.add(VARIANT_DIRECTORY, resultsShared.getJsonArray(VARIANT_DIRECTORY));
        }

        return transformResultsSharedBuilder.build();
    }
}
