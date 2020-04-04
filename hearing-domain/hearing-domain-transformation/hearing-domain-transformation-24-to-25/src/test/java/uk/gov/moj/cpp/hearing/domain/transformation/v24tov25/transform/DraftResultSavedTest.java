package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildDraftResultSavedTemplate;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromString;

import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TARGET;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.TARGET_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class DraftResultSavedTest {

    private DraftResultSaved draftResultSaved;

    @Before
    public void setUp() {
        draftResultSaved = new DraftResultSaved();
    }

    @Test
    public void transformDraftResultSaved_withMandatory() {
        JsonObject draftResultSavedTemplate = buildDraftResultSavedTemplate();
        JsonObject actual = draftResultSaved.transform(draftResultSavedTemplate);
        assertThat(actual.getJsonObject(TARGET).getString(HEARING_ID), is(draftResultSavedTemplate.getJsonObject(TARGET).getString(HEARING_ID)));
        assertThat(actual.getJsonObject(TARGET).getString(TARGET_ID), is(draftResultSavedTemplate.getJsonObject(TARGET).getString(TARGET_ID)));
    }

    @Test
    public void transformDraftResultSaved_withOptional() {
        JsonObject draftResultSavedTemplate = buildTemplateFromFile("hearing.draft-result-saved.json");
        assert draftResultSavedTemplate != null;
        JsonObject draftResultJsonObject = buildTemplateFromString(draftResultSavedTemplate.getJsonObject("target").getString("draftResult"));
        JsonObject actual = draftResultSaved.transform(draftResultSavedTemplate);
        assertThat(actual.getJsonObject(TARGET).getString(HEARING_ID), is(draftResultSavedTemplate.getJsonObject(TARGET).getString(HEARING_ID)));
        assertThat(actual.getJsonObject(TARGET).getString(TARGET_ID), is(draftResultSavedTemplate.getJsonObject(TARGET).getString(TARGET_ID)));
        assertThat(draftResultJsonObject.isNull("delegatedPowers"), is(true));
    }
}