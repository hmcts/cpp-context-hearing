package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import uk.gov.justice.core.courts.ProsecutionCase;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CREATE_NOWS_REQUEST;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NOWS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NOW_TYPES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.SHARED_RESULT_LINES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.HearingHelper.transformHearing;

public class NowsRequested implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject nowRequested, Map<String, ProsecutionCase> hearingMap) {

        final JsonObjectBuilder transformNowRequestedBuilder = createObjectBuilder()
                .add(CREATE_NOWS_REQUEST, transformNowRequest(nowRequested.getJsonObject(CREATE_NOWS_REQUEST)));

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
