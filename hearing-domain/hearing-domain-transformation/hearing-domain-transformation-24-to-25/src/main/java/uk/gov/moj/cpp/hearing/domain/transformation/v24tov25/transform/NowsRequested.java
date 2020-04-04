package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ACCOUNT_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CREATE_NOWS_REQUEST;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NOWS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NOW_TYPES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.REQUEST_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SHARED_RESULT_LINES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TARGETS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.HearingHelper.transformHearing;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.TargetHelper.transformTargets;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class NowsRequested implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject nowRequested) {

        final JsonObjectBuilder transformNowRequestedBuilder = createObjectBuilder()
                .add(CREATE_NOWS_REQUEST, transformNowRequest(nowRequested.getJsonObject(CREATE_NOWS_REQUEST)))
                .add(REQUEST_ID, nowRequested.getString(REQUEST_ID))
                .add(ACCOUNT_NUMBER, nowRequested.getString(ACCOUNT_NUMBER));

        return transformNowRequestedBuilder.build();
    }

    private JsonObject transformNowRequest(final JsonObject nowRequest) {

        final JsonObjectBuilder transformNowRequestBuilder = createObjectBuilder()
                .add(HEARING, transformHearing(nowRequest.getJsonObject(HEARING)))
                .add(SHARED_RESULT_LINES, nowRequest.getJsonArray(SHARED_RESULT_LINES))
                .add(NOWS, nowRequest.getJsonArray(NOWS))
                .add(NOW_TYPES, nowRequest.getJsonArray(NOW_TYPES))
                .add(COURT_CLERK, nowRequest.getJsonObject(COURT_CLERK));

        return transformNowRequestBuilder.build();
    }
}
