package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALLOCATION_DECISION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CREATE_NOWS_REQUEST;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.USER_ID;

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
        JsonObject actual = nowsRequested.transform(nowsRequestedTemplate, null);
        assertThat(actual.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(HEARING).getString(ID), is(nowsRequestedTemplate.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(HEARING).getString(ID)));
        assertThat(actual.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(COURT_CLERK).getString(USER_ID), is(nowsRequestedTemplate.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(COURT_CLERK).getString(USER_ID)));
        assertThat(actual.getJsonObject(CREATE_NOWS_REQUEST).getJsonObject(HEARING).
                getJsonArray(PROSECUTION_CASES).getJsonObject(0).
                getJsonArray(DEFENDANTS).getJsonObject(0).
                getJsonArray(OFFENCES).getJsonObject(0).
                getJsonObject(ALLOCATION_DECISION).getString(MOT_REASON_CODE), is("1"));
    }
}