package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static java.util.Objects.nonNull;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ADDITIONAL_NATIONALITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ADDITIONAL_NATIONALITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ADDITIONAL_NATIONALITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ADDRESS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CONTACT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DATE_OF_BIRTH;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DISABILITY_STATUS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DOCUMENTATION_LANGUAGE_NEEDS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ETHNICITY;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.FIRST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.GENDER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.INTERPRETER_LANGUAGE_NEEDS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.LAST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.MIDDLE_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NATIONALITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NATIONALITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NATIONALITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NATIONAL_INSURANCE_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OCCUPATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OCCUPATION_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON_MARKERS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SPECIFIC_REQUIREMENTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TITLE;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
public class PersonHelper {

    private PersonHelper() {
    }

    public static JsonObject transformPerson(final JsonObject person) {
        return transformPersonObject(person).build();
    }

    public static JsonObject transformPerson(final JsonObject person, final String observedEthnicityId,
                                             final String observedEthnicityCode,
                                             final String observedEthnicityDescription,
                                             final String selfDefinedEthnicityId,
                                             final String selfDefinedEthnicityCode,
                                             final String selfDefinedEthnicityDescription) {

        final JsonObjectBuilder transformPersonBuilder = transformPersonObject(person);

        final JsonObject ethnicity = transformEthnicity(observedEthnicityId, observedEthnicityCode, observedEthnicityDescription, selfDefinedEthnicityId, selfDefinedEthnicityCode, selfDefinedEthnicityDescription);
        if (!ethnicity.isEmpty()) {
            transformPersonBuilder.add(ETHNICITY, ethnicity);
        }

        return transformPersonBuilder.build();
    }

    private static JsonObjectBuilder transformPersonObject(final JsonObject person) {

        final JsonObjectBuilder transformPersonBuilder = createObjectBuilder()
                .add(LAST_NAME, person.getString(LAST_NAME))
                .add(GENDER, person.getString(GENDER));

        if (person.containsKey(TITLE)) {
            transformPersonBuilder.add(TITLE, person.getString(TITLE));
        }

        if (person.containsKey(FIRST_NAME)) {
            transformPersonBuilder.add(FIRST_NAME, person.getString(FIRST_NAME));
        }

        if (person.containsKey(MIDDLE_NAME)) {
            transformPersonBuilder.add(MIDDLE_NAME, person.getString(MIDDLE_NAME));
        }

        if (person.containsKey(DATE_OF_BIRTH)) {
            transformPersonBuilder.add(DATE_OF_BIRTH, person.getString(DATE_OF_BIRTH));
        }

        if (person.containsKey(NATIONALITY_ID)) {
            transformPersonBuilder.add(NATIONALITY_ID, person.getString(NATIONALITY_ID));
        }

        if (person.containsKey(NATIONALITY_CODE)) {
            transformPersonBuilder.add(NATIONALITY_CODE, person.getString(NATIONALITY_CODE));
        }

        if (person.containsKey(NATIONALITY_DESCRIPTION)) {
            transformPersonBuilder.add(NATIONALITY_DESCRIPTION, person.getString(NATIONALITY_DESCRIPTION));
        }

        if (person.containsKey(ADDITIONAL_NATIONALITY_ID)) {
            transformPersonBuilder.add(ADDITIONAL_NATIONALITY_ID, person.getString(ADDITIONAL_NATIONALITY_ID));
        }

        if (person.containsKey(ADDITIONAL_NATIONALITY_CODE)) {
            transformPersonBuilder.add(ADDITIONAL_NATIONALITY_CODE, person.getString(ADDITIONAL_NATIONALITY_CODE));
        }

        if (person.containsKey(ADDITIONAL_NATIONALITY_DESCRIPTION)) {
            transformPersonBuilder.add(ADDITIONAL_NATIONALITY_DESCRIPTION, person.getString(ADDITIONAL_NATIONALITY_DESCRIPTION));
        }

        if (person.containsKey(DISABILITY_STATUS)) {
            transformPersonBuilder.add(DISABILITY_STATUS, person.getString(DISABILITY_STATUS));
        }

        if (person.containsKey(INTERPRETER_LANGUAGE_NEEDS)) {
            transformPersonBuilder.add(INTERPRETER_LANGUAGE_NEEDS, person.getString(INTERPRETER_LANGUAGE_NEEDS));
        }

        if (person.containsKey(DOCUMENTATION_LANGUAGE_NEEDS)) {
            transformPersonBuilder.add(DOCUMENTATION_LANGUAGE_NEEDS, person.getString(DOCUMENTATION_LANGUAGE_NEEDS));
        }

        if (person.containsKey(NATIONAL_INSURANCE_NUMBER)) {
            transformPersonBuilder.add(NATIONAL_INSURANCE_NUMBER, person.getString(NATIONAL_INSURANCE_NUMBER));
        }

        if (person.containsKey(OCCUPATION)) {
            transformPersonBuilder.add(OCCUPATION, person.getString(OCCUPATION));
        }

        if (person.containsKey(OCCUPATION_CODE)) {
            transformPersonBuilder.add(OCCUPATION_CODE, person.getString(OCCUPATION_CODE));
        }

        if (person.containsKey(SPECIFIC_REQUIREMENTS)) {
            transformPersonBuilder.add(SPECIFIC_REQUIREMENTS, person.getString(SPECIFIC_REQUIREMENTS));
        }

        if (person.containsKey(ADDRESS)) {
            transformPersonBuilder.add(ADDRESS, person.getJsonObject(ADDRESS));
        }

        if (person.containsKey(CONTACT)) {
            transformPersonBuilder.add(CONTACT, person.getJsonObject(CONTACT));
        }

        if (person.containsKey(PERSON_MARKERS)) {
            transformPersonBuilder.add(PERSON_MARKERS, person.getJsonArray(PERSON_MARKERS));
        }

        return transformPersonBuilder;
    }


    private static JsonObject transformEthnicity(final String observedEthnicityId,
                                                 final String observedEthnicityCode,
                                                 final String observedEthnicityDescription,
                                                 final String selfDefinedEthnicityId,
                                                 final String selfDefinedEthnicityCode,
                                                 final String selfDefinedEthnicityDescription) {

        final JsonObjectBuilder transformEthnicityBuilder = createObjectBuilder();

        if (nonNull(observedEthnicityId)) {
            transformEthnicityBuilder.add(OBSERVED_ETHNICITY_ID, observedEthnicityId);
        }

        if (nonNull(observedEthnicityCode)) {
            transformEthnicityBuilder.add(OBSERVED_ETHNICITY_CODE, observedEthnicityCode);
        }

        if (nonNull(observedEthnicityDescription)) {
            transformEthnicityBuilder.add(OBSERVED_ETHNICITY_DESCRIPTION, observedEthnicityDescription);
        }

        if (nonNull(selfDefinedEthnicityId)) {
            transformEthnicityBuilder.add(SELF_DEFINED_ETHNICITY_ID, selfDefinedEthnicityId);
        }

        if (nonNull(selfDefinedEthnicityCode)) {
            transformEthnicityBuilder.add(SELF_DEFINED_ETHNICITY_CODE, selfDefinedEthnicityCode);
        }

        if (nonNull(selfDefinedEthnicityDescription)) {
            transformEthnicityBuilder.add(SELF_DEFINED_ETHNICITY_DESCRIPTION, selfDefinedEthnicityDescription);
        }

        return transformEthnicityBuilder.build();
    }
}
