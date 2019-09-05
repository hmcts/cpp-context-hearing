package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALLOCATION_DECISION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASES;

public class ResultsSharedTest {

    private ResultsShared resultsShared;

    @Before
    public void setUp() {
        resultsShared = new ResultsShared();
    }

    @Test
    public void transform() {
        JsonObject resultsSharedTemplate = buildTemplateFromFile("hearing.results-shared.json");
        assert resultsSharedTemplate != null;
        JsonObject actual = resultsShared.transform(resultsSharedTemplate, null);
        assertThat(actual.getString(HEARING_ID), is(resultsSharedTemplate.getString(HEARING_ID)));
        assertThat(actual.getJsonObject(HEARING).
                getJsonArray(PROSECUTION_CASES).getJsonObject(0).
                getJsonArray(DEFENDANTS).getJsonObject(0).
                getJsonArray(OFFENCES).getJsonObject(0).
                getJsonObject(ALLOCATION_DECISION).getString(MOT_REASON_CODE), is("7"));
    }
}