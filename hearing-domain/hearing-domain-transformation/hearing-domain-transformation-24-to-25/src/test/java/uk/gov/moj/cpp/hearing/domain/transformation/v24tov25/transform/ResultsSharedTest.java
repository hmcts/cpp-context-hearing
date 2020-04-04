package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.USER_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

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
        JsonObject actual = resultsShared.transform(resultsSharedTemplate);
        assertThat(actual.getString(HEARING_ID), is(resultsSharedTemplate.getString(HEARING_ID)));
        assertThat(actual.getJsonObject(COURT_CLERK).getString(USER_ID), is(resultsSharedTemplate.getJsonObject(COURT_CLERK).getString(ID)));
    }
}