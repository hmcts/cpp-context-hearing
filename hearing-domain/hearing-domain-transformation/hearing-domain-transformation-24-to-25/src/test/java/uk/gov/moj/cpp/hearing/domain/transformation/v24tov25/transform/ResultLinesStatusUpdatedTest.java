package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.USER_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class ResultLinesStatusUpdatedTest {

    private ResultLinesStatusUpdated resultLinesStatusUpdated;

    @Before
    public void setUp() {
        resultLinesStatusUpdated =  new ResultLinesStatusUpdated();
    }
    
    @Test
    public void transform() {
        JsonObject resultLinesStatusUpdatedTemplate = buildTemplateFromFile("hearing.result-lines-status-updated.json");
        assert resultLinesStatusUpdatedTemplate != null;
        final JsonObject actual = resultLinesStatusUpdated.transform(resultLinesStatusUpdatedTemplate);
        assertThat(actual.getJsonObject(COURT_CLERK).getString(USER_ID), is(resultLinesStatusUpdatedTemplate.getJsonObject(COURT_CLERK).getString(ID)));
    }
}