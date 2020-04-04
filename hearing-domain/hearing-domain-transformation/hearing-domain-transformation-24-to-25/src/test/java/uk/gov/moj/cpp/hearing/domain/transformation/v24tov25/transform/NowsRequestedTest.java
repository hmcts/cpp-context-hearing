package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ACCOUNT_NUMBER;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.CREATE_NOWS_REQUEST;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.REQUEST_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.USER_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class NowsRequestedTest {

    private NowsRequested nowsRequested;

    @Before
    public void setUp() {
        nowsRequested = new NowsRequested();
    }

    @Test
    public void transform() {
        JsonObject nowsRequestedTemplate = buildTemplateFromFile("hearing.events.nows-requested.json");
        assert nowsRequestedTemplate != null;
        JsonObject actual = nowsRequested.transform(nowsRequestedTemplate);
        assertThat(actual.getString(REQUEST_ID), is(nowsRequestedTemplate.getString(REQUEST_ID)));
        assertThat(actual.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(HEARING).getString(ID), is(nowsRequestedTemplate.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(HEARING).getString(ID)));
        assertThat(actual.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(COURT_CLERK).getString(USER_ID), is(nowsRequestedTemplate.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(COURT_CLERK).getString(USER_ID)));
        assertThat(actual.getString(ACCOUNT_NUMBER), is(nowsRequestedTemplate.getString(ACCOUNT_NUMBER)));
    }
}