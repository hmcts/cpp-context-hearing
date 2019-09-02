package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CREATE_NOWS_REQUEST;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.USER_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class PendingNowsRequestedTest {

    private PendingNowsRequested pendingNowsRequested;

    @Before
    public void setUp() {
        pendingNowsRequested = new PendingNowsRequested();
    }

    @Test
    public void transform() {
        JsonObject pendingNowsRequestedTemplate = buildTemplateFromFile("hearing.events.pending-nows-requested.json");
        assert pendingNowsRequestedTemplate != null;
        JsonObject actual = pendingNowsRequested.transform(pendingNowsRequestedTemplate);
        assertThat(actual.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(HEARING).getString(ID), is(pendingNowsRequestedTemplate.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(HEARING).getString(ID)));
        assertThat(actual.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(COURT_CLERK).getString(USER_ID), is(pendingNowsRequestedTemplate.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(COURT_CLERK).getString(USER_ID)));
    }
}