package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildDefendantDetailsUpdatedTemplate;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ETHNICITY;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OBSERVED_ETHNICITY_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON_DEFENDANT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PERSON_DETAILS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_DESCRIPTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.SELF_DEFINED_ETHNICITY_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class DefendantDetailsUpdatedTest {

    private DefendantDetailsUpdated defendantDetailsUpdated;

    @Before
    public void setup() {
        defendantDetailsUpdated = new DefendantDetailsUpdated();
    }

    @Test
    public void transformDefendantDetailsUpdated_WithMandatory() {

        final JsonObject payload = buildDefendantDetailsUpdatedTemplate();
        System.out.println(payload);
        final JsonObject actual = defendantDetailsUpdated.transform(payload);

        assertThat(actual.getJsonObject(DEFENDANT).getString(ID), is(payload.getJsonObject(DEFENDANT).getString(ID)));
        assertThat(actual.getString(HEARING_ID), is(payload.getString(HEARING_ID)));
    }

    @Test
    public void transformDefendantDetailsUpdated_WithOptional() {

        final JsonObject payload = buildDefendantDetailsUpdatedTemplate(true);

        final JsonObject actual = defendantDetailsUpdated.transform(payload);

        assertThat(actual.getJsonObject(DEFENDANT).getString(ID), is(payload.getJsonObject(DEFENDANT).getString(ID)));

        assertThat(actual.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getJsonObject(PERSON_DETAILS).getJsonObject(ETHNICITY).getString(OBSERVED_ETHNICITY_ID),
                is(payload.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getString(OBSERVED_ETHNICITY_ID)));

        assertThat(actual.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getJsonObject(PERSON_DETAILS).getJsonObject(ETHNICITY).getString(OBSERVED_ETHNICITY_CODE),
                is(payload.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getString(OBSERVED_ETHNICITY_CODE)));

        assertThat(actual.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getJsonObject(PERSON_DETAILS).getJsonObject(ETHNICITY).getString(OBSERVED_ETHNICITY_DESCRIPTION),
                is(payload.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getString(OBSERVED_ETHNICITY_DESCRIPTION)));

        assertThat(actual.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getJsonObject(PERSON_DETAILS).getJsonObject(ETHNICITY).getString(SELF_DEFINED_ETHNICITY_ID),
                is(payload.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getString(SELF_DEFINED_ETHNICITY_ID)));

        assertThat(actual.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getJsonObject(PERSON_DETAILS).getJsonObject(ETHNICITY).getString(SELF_DEFINED_ETHNICITY_CODE),
                is(payload.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getString(SELF_DEFINED_ETHNICITY_CODE)));

        assertThat(actual.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getJsonObject(PERSON_DETAILS).getJsonObject(ETHNICITY).getString(SELF_DEFINED_ETHNICITY_DESCRIPTION),
                is(payload.getJsonObject(DEFENDANT).getJsonObject(PERSON_DEFENDANT).getString(SELF_DEFINED_ETHNICITY_DESCRIPTION)));

        assertThat(actual.getString(HEARING_ID), is(payload.getString(HEARING_ID)));
    }
}