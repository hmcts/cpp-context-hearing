package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_CASE_NOTE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.USER_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class HearingCaseNoteSavedTest {

    private HearingCaseNoteSaved hearingCaseNoteSaved;

    @Before
    public void setUp() {
        hearingCaseNoteSaved = new HearingCaseNoteSaved();
    }

    @Test
    public void transform() {
        JsonObject hearingCaseNoteSavedTemplate = buildTemplateFromFile("hearing.hearing-case-note-saved.json");
        assert hearingCaseNoteSavedTemplate != null;
        JsonObject actual = hearingCaseNoteSaved.transform(hearingCaseNoteSavedTemplate);
        assertThat(actual.getJsonObject(HEARING_CASE_NOTE).getString(ID), is(hearingCaseNoteSavedTemplate.getJsonObject(HEARING_CASE_NOTE).getString(ID)));
        assertThat(actual.getJsonObject(HEARING_CASE_NOTE).getJsonObject(COURT_CLERK).getString(USER_ID), is(hearingCaseNoteSavedTemplate.getJsonObject(HEARING_CASE_NOTE).getJsonObject(COURT_CLERK).getString(ID)));
    }
}