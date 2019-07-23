package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ARREST_SUMMONS_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.BAIL_CONDITIONS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.BAIL_STATUS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CUSTODY_TIME_LIMIT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DRIVER_LICENCE_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DRIVER_LICENSE_ISSUE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DRIVER_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.EMPLOYER_ORGANISATION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.EMPLOYER_PAYROLL_REFERENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERCEIVED_BIRTH_YEAR;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON_DETAILS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.VEHICLE_OPERATOR_LICENCE_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.PersonHelper.transformPerson;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@SuppressWarnings({"squid:MethodCyclomaticComplexity"})
public class PersonDefendantHelper {

    private PersonDefendantHelper() {
    }

    public static JsonObject transformPersonDefendant(final JsonObject personDefendant) {

        final JsonObjectBuilder transformPersonDefendantBuilder = createObjectBuilder()
                .add(PERSON_DETAILS, transformPerson(
                        personDefendant.getJsonObject(PERSON_DETAILS),
                        personDefendant.getString(OBSERVED_ETHNICITY_ID, null),
                        personDefendant.getString(OBSERVED_ETHNICITY_CODE, null),
                        personDefendant.getString(OBSERVED_ETHNICITY_DESCRIPTION, null),
                        personDefendant.getString(SELF_DEFINED_ETHNICITY_ID, null),
                        personDefendant.getString(SELF_DEFINED_ETHNICITY_CODE, null),
                        personDefendant.getString(SELF_DEFINED_ETHNICITY_DESCRIPTION, null)
                ));

        if (personDefendant.containsKey(BAIL_STATUS)) {
            transformPersonDefendantBuilder.add(BAIL_STATUS, personDefendant.getString(BAIL_STATUS));
        }

        if (personDefendant.containsKey(BAIL_CONDITIONS)) {
            transformPersonDefendantBuilder.add(BAIL_CONDITIONS, personDefendant.getString(BAIL_CONDITIONS));
        }

        if (personDefendant.containsKey(CUSTODY_TIME_LIMIT)) {
            transformPersonDefendantBuilder.add(CUSTODY_TIME_LIMIT, personDefendant.getString(CUSTODY_TIME_LIMIT));
        }

        if (personDefendant.containsKey(PERCEIVED_BIRTH_YEAR)) {
            transformPersonDefendantBuilder.add(PERCEIVED_BIRTH_YEAR, personDefendant.getInt(PERCEIVED_BIRTH_YEAR));
        }

        if (personDefendant.containsKey(DRIVER_NUMBER)) {
            transformPersonDefendantBuilder.add(DRIVER_NUMBER, personDefendant.getString(DRIVER_NUMBER));
        }

        if (personDefendant.containsKey(DRIVER_LICENCE_CODE)) {
            transformPersonDefendantBuilder.add(DRIVER_LICENCE_CODE, personDefendant.getString(DRIVER_LICENCE_CODE));
        }

        if (personDefendant.containsKey(DRIVER_LICENSE_ISSUE)) {
            transformPersonDefendantBuilder.add(DRIVER_LICENSE_ISSUE, personDefendant.getString(DRIVER_LICENSE_ISSUE));
        }

        if (personDefendant.containsKey(VEHICLE_OPERATOR_LICENCE_NUMBER)) {
            transformPersonDefendantBuilder.add(VEHICLE_OPERATOR_LICENCE_NUMBER, personDefendant.getString(VEHICLE_OPERATOR_LICENCE_NUMBER));
        }

        if (personDefendant.containsKey(ARREST_SUMMONS_NUMBER)) {
            transformPersonDefendantBuilder.add(ARREST_SUMMONS_NUMBER, personDefendant.getString(ARREST_SUMMONS_NUMBER));
        }

        if (personDefendant.containsKey(EMPLOYER_ORGANISATION)) {
            transformPersonDefendantBuilder.add(EMPLOYER_ORGANISATION, personDefendant.getJsonObject(EMPLOYER_ORGANISATION));
        }

        if (personDefendant.containsKey(EMPLOYER_PAYROLL_REFERENCE)) {
            transformPersonDefendantBuilder.add(EMPLOYER_PAYROLL_REFERENCE, personDefendant.getString(EMPLOYER_PAYROLL_REFERENCE));
        }

        return transformPersonDefendantBuilder.build();
    }


}
