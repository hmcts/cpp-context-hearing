package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.APPEAL_PROCEEDINGS_PENDING;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.BREACH_PROCEEDINGS_PENDING;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CASE_MARKERS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CASE_STATUS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.INITIATION_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ORIGINATING_ORGANISATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.POLICE_OFFICER_IN_CASE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PROSECUTION_CASE_IDENTIFIER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.STATEMENT_OF_FACTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.STATEMENT_OF_FACTS_WELSH;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.DefendantHelper.transformDefendants;

import java.util.stream.IntStream;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ProsecutionCaseHelper {

    private ProsecutionCaseHelper() {
    }

    public static JsonArray transformProsecutionCases(final JsonArray prosecutionCases) {

        final JsonArrayBuilder transformedPayloadObjectBuilder = createArrayBuilder();

        IntStream.range(0, prosecutionCases.size()).mapToObj(index -> transformProsecutionCase(prosecutionCases.getJsonObject(index))).forEach(transformedPayloadObjectBuilder::add);

        return transformedPayloadObjectBuilder.build();
    }

    public static JsonObjectBuilder transformProsecutionCase(final JsonObject prosecutionCase) {

        final JsonObjectBuilder transformProsecutionCaseBuilder = createObjectBuilder()
                .add(ID, prosecutionCase.getString(ID))
                .add(PROSECUTION_CASE_IDENTIFIER, prosecutionCase.getJsonObject(PROSECUTION_CASE_IDENTIFIER))
                .add(INITIATION_CODE, prosecutionCase.getString(INITIATION_CODE))
                .add(DEFENDANTS, transformDefendants(prosecutionCase.getJsonArray(DEFENDANTS)));

        if (prosecutionCase.containsKey(ORIGINATING_ORGANISATION)) {
            transformProsecutionCaseBuilder.add(ORIGINATING_ORGANISATION, prosecutionCase.getString(ORIGINATING_ORGANISATION));
        }

        if (prosecutionCase.containsKey(CASE_STATUS)) {
            transformProsecutionCaseBuilder.add(CASE_STATUS, prosecutionCase.getString(CASE_STATUS));
        }

        if (prosecutionCase.containsKey(STATEMENT_OF_FACTS)) {
            transformProsecutionCaseBuilder.add(STATEMENT_OF_FACTS, prosecutionCase.getString(STATEMENT_OF_FACTS));
        }

        if (prosecutionCase.containsKey(STATEMENT_OF_FACTS_WELSH)) {
            transformProsecutionCaseBuilder.add(STATEMENT_OF_FACTS_WELSH, prosecutionCase.getString(STATEMENT_OF_FACTS_WELSH));
        }

        if (prosecutionCase.containsKey(BREACH_PROCEEDINGS_PENDING)) {
            transformProsecutionCaseBuilder.add(BREACH_PROCEEDINGS_PENDING, prosecutionCase.getBoolean(BREACH_PROCEEDINGS_PENDING));
        }

        if (prosecutionCase.containsKey(APPEAL_PROCEEDINGS_PENDING)) {
            transformProsecutionCaseBuilder.add(APPEAL_PROCEEDINGS_PENDING, prosecutionCase.getBoolean(APPEAL_PROCEEDINGS_PENDING));
        }

        if (prosecutionCase.containsKey(POLICE_OFFICER_IN_CASE)) {
            transformProsecutionCaseBuilder.add(POLICE_OFFICER_IN_CASE, prosecutionCase.getJsonObject(POLICE_OFFICER_IN_CASE));
        }

        if (prosecutionCase.containsKey(CASE_MARKERS)) {
            transformProsecutionCaseBuilder.add(CASE_MARKERS, prosecutionCase.getJsonObject(CASE_MARKERS));
        }
        return transformProsecutionCaseBuilder;
    }

}