package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static java.util.Objects.nonNull;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ALIASES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ASSOCIATED_PERSONS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CRO_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENCE_ORGANISATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.JUDICIAL_RESULTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.LAST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.LEGAL_ENTITY_DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.MITIGATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.MITIGATION_WELSH;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NUMBER_OF_PREVIOUS_CONVICTIONS_CITED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON_DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PNC_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PROSECUTION_AUTHORITY_REFERENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PROSECUTION_CASE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ROLE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.WITNESS_STATEMENT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.WITNESS_STATEMENT_WELSH;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.OffenceHelper.transformOffences;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.PersonDefendantHelper.transformPersonDefendant;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.PersonHelper.transformPerson;

import java.util.stream.IntStream;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
public class DefendantHelper {

    private DefendantHelper() {
    }

    public static JsonArray transformDefendants(final JsonArray defendants) {

        final JsonArrayBuilder transformedPayloadObjectBuilder = createArrayBuilder();

        IntStream.range(0, defendants.size()).mapToObj(index -> transformDefendant(defendants.getJsonObject(index))).forEach(transformedPayloadObjectBuilder::add);

        return transformedPayloadObjectBuilder.build();
    }

    public static JsonObjectBuilder transformDefendantUpdated(final JsonObject defendant) {

        final JsonObjectBuilder transformDefendantBuilder = createObjectBuilder()
                .add(ID, defendant.getString(ID))
                .add(PROSECUTION_CASE_ID, defendant.getString(PROSECUTION_CASE_ID));

        JsonObject personDefendant = null;

        if (defendant.containsKey(PERSON_DEFENDANT)) {
            personDefendant = defendant.getJsonObject(PERSON_DEFENDANT);
        }

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
            transformDefendantBuilder.add(ASSOCIATED_PERSONS, transformAssociatedPersons(defendant.getJsonArray(ASSOCIATED_PERSONS)));
        }

        if (defendant.containsKey(DEFENCE_ORGANISATION)) {
            transformDefendantBuilder.add(DEFENCE_ORGANISATION, defendant.getJsonObject(DEFENCE_ORGANISATION));
        }

        if (defendant.containsKey(PERSON_DEFENDANT) && nonNull(personDefendant)) {
            transformDefendantBuilder.add(PERSON_DEFENDANT, transformPersonDefendant(personDefendant));
        }

        if (defendant.containsKey(LEGAL_ENTITY_DEFENDANT)) {
            transformDefendantBuilder.add(LEGAL_ENTITY_DEFENDANT, defendant.getJsonObject(LEGAL_ENTITY_DEFENDANT));
        }

        return transformDefendantBuilder;
    }

    private static JsonObjectBuilder transformDefendant(final JsonObject defendant) {

        final JsonObjectBuilder transformDefendantBuilder = createObjectBuilder()
                .add(ID, defendant.getString(ID))
                .add(PROSECUTION_CASE_ID, defendant.getString(PROSECUTION_CASE_ID))
                .add(OFFENCES, transformOffences(defendant.getJsonArray(OFFENCES)));

        JsonObject personDefendant = null;

        if (defendant.containsKey(PERSON_DEFENDANT)) {
            personDefendant = defendant.getJsonObject(PERSON_DEFENDANT);
            transformDefendantBuilder.add(PERSON_DEFENDANT, transformPersonDefendant(personDefendant));
        }

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

        if (defendant.containsKey(CRO_NUMBER)) {
            transformDefendantBuilder.add(CRO_NUMBER, defendant.getString(CRO_NUMBER));
        }

        if (nonNull(personDefendant) && personDefendant.containsKey(PNC_ID)) {
            transformDefendantBuilder.add(PNC_ID, personDefendant.getString(PNC_ID));
        }

        if (defendant.containsKey(ASSOCIATED_PERSONS)) {
            transformDefendantBuilder.add(ASSOCIATED_PERSONS, transformAssociatedPersons(defendant.getJsonArray(ASSOCIATED_PERSONS)));
        }

        if (defendant.containsKey(DEFENCE_ORGANISATION)) {
            transformDefendantBuilder.add(DEFENCE_ORGANISATION, defendant.getJsonObject(DEFENCE_ORGANISATION));
        }

        if (defendant.containsKey(LEGAL_ENTITY_DEFENDANT)) {
            transformDefendantBuilder.add(LEGAL_ENTITY_DEFENDANT, defendant.getJsonObject(LEGAL_ENTITY_DEFENDANT));
        }

        if (nonNull(personDefendant) && personDefendant.containsKey(ALIASES)) {

            final JsonArray aliases = personDefendant.getJsonArray(ALIASES);

            final JsonArrayBuilder arrayBuilder = createArrayBuilder();

            IntStream.range(0, aliases.size()).mapToObj(i -> createObjectBuilder().add(LAST_NAME, aliases.getString(i))).forEach(arrayBuilder::add);

            transformDefendantBuilder.add(ALIASES, arrayBuilder.build());
        }

        if (defendant.containsKey(JUDICIAL_RESULTS)) {
            transformDefendantBuilder.add(JUDICIAL_RESULTS, defendant.getJsonArray(JUDICIAL_RESULTS));
        }

        return transformDefendantBuilder;
    }

    private static JsonArray transformAssociatedPersons(final JsonArray associatedPersons) {

        final JsonArrayBuilder transformedPayloadObjectBuilder = createArrayBuilder();

        IntStream.range(0, associatedPersons.size()).mapToObj(index -> transformAssociatedPersons(associatedPersons.getJsonObject(index))).forEach(transformedPayloadObjectBuilder::add);

        return transformedPayloadObjectBuilder.build();
    }

    private static JsonObject transformAssociatedPersons(final JsonObject associatedPersons) {

        final JsonObjectBuilder transformAssociatedPersonsBuilder = createObjectBuilder();

        if(associatedPersons.containsKey(PERSON)) {
            transformAssociatedPersonsBuilder.add(PERSON, transformPerson(associatedPersons.getJsonObject(PERSON), null, null, null, null, null, null));
        }

        if(associatedPersons.containsKey(ROLE)) {
            transformAssociatedPersonsBuilder.add(ROLE, associatedPersons.getString(ROLE));
        }

        return transformAssociatedPersonsBuilder.build();
    }
}
