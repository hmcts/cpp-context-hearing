package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CATEGORY;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CATEGORY_TYPE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.VERDICT;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.VERDICT_TYPE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.VERDICT_TYPE_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class HearingOffenceVerdictUpdatedTest {

    private HearingOffenceVerdictUpdated hearingOffenceVerdictUpdated;

    @Before
    public void setUp() {
        hearingOffenceVerdictUpdated = new HearingOffenceVerdictUpdated();
    }


    @Test
    public void transform() {
        JsonObject hearingOffenceVerdictUpdatedTemplate = buildTemplateFromFile("hearing.hearing-offence-verdict-updated.json");
        assert hearingOffenceVerdictUpdatedTemplate != null;
        JsonObject actual = hearingOffenceVerdictUpdated.transform(hearingOffenceVerdictUpdatedTemplate);
        assertThat(actual.getString(HEARING_ID), is(hearingOffenceVerdictUpdatedTemplate.getString(HEARING_ID)));
        assertThat(actual.getJsonObject(VERDICT).getJsonObject(VERDICT_TYPE).getString(ID),
                is(hearingOffenceVerdictUpdatedTemplate.getJsonObject(VERDICT).getJsonObject(VERDICT_TYPE).getString(VERDICT_TYPE_ID)));
        assertThat(actual.getJsonObject(VERDICT).getJsonObject(VERDICT_TYPE).getString(CATEGORY_TYPE),
                is(hearingOffenceVerdictUpdatedTemplate.getJsonObject(VERDICT).getJsonObject(VERDICT_TYPE).getString(CATEGORY_TYPE)));
        assertThat(actual.getJsonObject(VERDICT).getJsonObject(VERDICT_TYPE).getString(CATEGORY),
                is(hearingOffenceVerdictUpdatedTemplate.getJsonObject(VERDICT).getJsonObject(VERDICT_TYPE).getString(CATEGORY)));
    }
}