package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ASSOCIATED_PERSONS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DRAFT_RESULT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.FIRST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.GENDER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.LAST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON_DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON_DETAILS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PROSECUTION_CASE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.RESULT_LINES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ROLE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TARGET;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TARGET_ID;

import uk.gov.justice.core.courts.Gender;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

public class JsonObjectSample {

    public static JsonObject buildCourtClerk() {

        return createObjectBuilder()
                .add(ID, randomUUID().toString())
                .add(FIRST_NAME, STRING.next())
                .add(LAST_NAME, STRING.next())
                .build();
    }

    public static JsonObject buildTemplateFromString(final String payloadAsString) {
        try (StringReader reader = new StringReader(payloadAsString);
             final JsonReader jsonReader = Json.createReader(reader)) {
            return jsonReader.readObject();
        }
    }

    public static JsonObject buildTemplateFromFile(final String fileName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (final InputStream stream = loader.getResourceAsStream(fileName);
             final JsonReader jsonReader = Json.createReader(stream)) {
            return jsonReader.readObject();
        } catch (final IOException e) {
            e.printStackTrace();
            System.out.println("Error in reading payload " + fileName);
        }
        return null;
    }

    public static JsonObject buildDefendantDetailsUpdatedTemplate(final boolean... optional) {

        JsonObjectBuilder jsonObjectBuilder = createObjectBuilder()
                .add(HEARING_ID, randomUUID().toString());

        if (optional.length > 0 && optional[0]) {
            jsonObjectBuilder.add(DEFENDANT, buildDefendantWithOptional());
        } else {
            jsonObjectBuilder.add(DEFENDANT, buildDefendant());
        }

        return jsonObjectBuilder.build();
    }

    public static JsonObject buildDraftResultSavedTemplate(final boolean... optional) {
        JsonObjectBuilder jsonObjectBuilder = createObjectBuilder()
                .add(TARGET, buildTarget(optional.length > 0 && optional[0]));

        return jsonObjectBuilder.build();
    }

    private static JsonObject buildTarget(boolean optional) {
        JsonObjectBuilder jsonObjectBuilder = createObjectBuilder()
                .add(HEARING_ID, randomUUID().toString())
                .add(TARGET_ID, randomUUID().toString());

        if (optional) {
            jsonObjectBuilder.add(DEFENDANT_ID, randomUUID().toString())
                    .add(OFFENCE_ID, randomUUID().toString())
                    .add(DRAFT_RESULT, STRING.next())
                    .add(RESULT_LINES, createArrayBuilder().build());
        }

        return jsonObjectBuilder.build();
    }

    private static JsonObject buildDefendant() {
        return createObjectBuilder()
                .add(ID, randomUUID().toString())
                .add(PROSECUTION_CASE_ID, randomUUID().toString())
                .build();
    }

    private static JsonObject buildDefendantWithOptional() {
        return createObjectBuilder()
                .add(ID, randomUUID().toString())
                .add(PROSECUTION_CASE_ID, randomUUID().toString())
                .add(PERSON_DEFENDANT, buildPersonDefendant())
                .add(ASSOCIATED_PERSONS, buildAssociatedPersons())
                .build();
    }

    private static JsonArray buildAssociatedPersons() {
        return createArrayBuilder()
                .add(createObjectBuilder()
                        .add(PERSON, buildPersonDetails())
                        .add(ROLE, STRING.next())
                ).build();
    }

    private static JsonObject buildPersonDefendant() {
        return createObjectBuilder()
                .add(PERSON_DETAILS, buildPersonDetails())
                .add(OBSERVED_ETHNICITY_ID, randomUUID().toString())
                .add(OBSERVED_ETHNICITY_CODE, STRING.next())
                .add(OBSERVED_ETHNICITY_DESCRIPTION, STRING.next())
                .add(SELF_DEFINED_ETHNICITY_ID, randomUUID().toString())
                .add(SELF_DEFINED_ETHNICITY_CODE, STRING.next())
                .add(SELF_DEFINED_ETHNICITY_DESCRIPTION, STRING.next())
                .build();
    }

    private static JsonObject buildPersonDetails() {
        return createObjectBuilder()
                .add(LAST_NAME, STRING.next())
                .add(GENDER, randomEnum(Gender.class).next().toString())
                .add(ETHNICITY_ID, randomUUID().toString())
                .add(ETHNICITY_CODE, STRING.next())
                .add(ETHNICITY_DESCRIPTION, STRING.next())
                .build();
    }

}
