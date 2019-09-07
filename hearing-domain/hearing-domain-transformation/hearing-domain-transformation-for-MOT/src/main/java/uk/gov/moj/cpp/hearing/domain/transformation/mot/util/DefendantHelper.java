package uk.gov.moj.cpp.hearing.domain.transformation.mot.util;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.stream.IntStream;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ADDRESS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALIASES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ASSOCIATED_PERSONS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CRO_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DATE_OF_BIRTH;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENCE_ORGANISATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.FIRST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.GENDER;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.INTERPRETER;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.JUDICIAL_RESULTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.LAST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.LEGAL_ENTITY_DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MITIGATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MITIGATION_WELSH;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NATIONALITY;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NUMBER_OF_PREVIOUS_CONVICTIONS_CITED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PERSON_DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PERSON_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PNC_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_AUTHORITY_REFERENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.WITNESS_STATEMENT;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.WITNESS_STATEMENT_WELSH;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.OffenceHelper.transformOffences;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.OffenceHelper.transformOffencesForSendingSheet;

@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
public class DefendantHelper {

    private DefendantHelper() {
    }

    public static JsonArray transformDefendants(final JsonArray defendants, final String hearingId) {

        final JsonArrayBuilder transformedPayloadObjectBuilder = createArrayBuilder();

        IntStream.range(0, defendants.size()).mapToObj(index -> transformDefendant(defendants.getJsonObject(index), hearingId)).forEach(transformedPayloadObjectBuilder::add);

        return transformedPayloadObjectBuilder.build();
    }


    private static JsonObjectBuilder transformDefendant(final JsonObject defendant, final String hearingId) {

        final JsonObjectBuilder transformDefendantBuilder = createObjectBuilder()
                .add(ID, defendant.getJsonString(ID))
                .add(PROSECUTION_CASE_ID, defendant.getJsonString(PROSECUTION_CASE_ID))
                .add(OFFENCES, transformOffences(defendant.getJsonArray(OFFENCES), hearingId));

        if (defendant.containsKey(NUMBER_OF_PREVIOUS_CONVICTIONS_CITED)) {
            transformDefendantBuilder.add(NUMBER_OF_PREVIOUS_CONVICTIONS_CITED, defendant.getInt(NUMBER_OF_PREVIOUS_CONVICTIONS_CITED));
        }

        if (defendant.containsKey(PROSECUTION_AUTHORITY_REFERENCE)) {
            transformDefendantBuilder.add(PROSECUTION_AUTHORITY_REFERENCE, defendant.getString(PROSECUTION_AUTHORITY_REFERENCE));
        }

        if (defendant.containsKey(WITNESS_STATEMENT)) {
            transformDefendantBuilder.add(WITNESS_STATEMENT, defendant.getString(WITNESS_STATEMENT));
        }

        if (defendant.containsKey(WITNESS_STATEMENT_WELSH)) {
            transformDefendantBuilder.add(WITNESS_STATEMENT_WELSH, defendant.getString(WITNESS_STATEMENT_WELSH));
        }

        if (defendant.containsKey(MITIGATION)) {
            transformDefendantBuilder.add(MITIGATION, defendant.getString(MITIGATION));
        }

        if (defendant.containsKey(MITIGATION_WELSH)) {
            transformDefendantBuilder.add(MITIGATION_WELSH, defendant.getString(MITIGATION_WELSH));
        }

        if (defendant.containsKey(ASSOCIATED_PERSONS)) {
            transformDefendantBuilder.add(ASSOCIATED_PERSONS, defendant.getJsonArray(ASSOCIATED_PERSONS));
        }

        if (defendant.containsKey(DEFENCE_ORGANISATION)) {
            transformDefendantBuilder.add(DEFENCE_ORGANISATION, defendant.getJsonObject(DEFENCE_ORGANISATION));
        }

        if (defendant.containsKey(PERSON_DEFENDANT)) {
            transformDefendantBuilder.add(PERSON_DEFENDANT, defendant.getJsonObject(PERSON_DEFENDANT));
        }

        if (defendant.containsKey(LEGAL_ENTITY_DEFENDANT)) {
            transformDefendantBuilder.add(LEGAL_ENTITY_DEFENDANT, defendant.getJsonObject(LEGAL_ENTITY_DEFENDANT));
        }

        if (defendant.containsKey(ALIASES)) {
            transformDefendantBuilder.add(ALIASES, defendant.getJsonArray(ALIASES));
        }

        if (defendant.containsKey(JUDICIAL_RESULTS)) {
            transformDefendantBuilder.add(JUDICIAL_RESULTS, defendant.getJsonArray(JUDICIAL_RESULTS));
        }

        if (defendant.containsKey(CRO_NUMBER)) {
            transformDefendantBuilder.add(CRO_NUMBER, defendant.getString(CRO_NUMBER));
        }

        if (defendant.containsKey(PNC_ID)) {
            transformDefendantBuilder.add(PNC_ID, defendant.getString(PNC_ID));
        }

        return transformDefendantBuilder;
    }


    public static JsonArray transformDefendantsForSendingSheet(final JsonArray defendants) {

        final JsonArrayBuilder transformedPayloadObjectBuilder = createArrayBuilder();

        IntStream.range(0, defendants.size()).mapToObj(index -> transformDefendantForSendingSheet(defendants.getJsonObject(index))).forEach(transformedPayloadObjectBuilder::add);

        return transformedPayloadObjectBuilder.build();
    }

    private static JsonObjectBuilder transformDefendantForSendingSheet(final JsonObject defendant) {

        return createObjectBuilder()
                .add(ID, defendant.getJsonString(ID))
                .add(PERSON_ID, defendant.getJsonString(PERSON_ID))
                .add(FIRST_NAME, defendant.getJsonString(FIRST_NAME))
                .add(LAST_NAME, defendant.getJsonString(LAST_NAME))
                .add(NATIONALITY, defendant.getJsonString(NATIONALITY))
                .add(GENDER, defendant.getJsonString(GENDER))
                .add(ADDRESS, defendant.getJsonObject(ADDRESS))
                .add(DATE_OF_BIRTH, defendant.getJsonString(DATE_OF_BIRTH))
                .add(INTERPRETER, defendant.getJsonString(INTERPRETER))
                .add(OFFENCES, transformOffencesForSendingSheet(defendant.getJsonArray(OFFENCES)));

    }
}
