package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class InheritedVerdictAddedTest {

    private InheritedVerdictAdded inheritedVerdictAdded;

    @Before
    public void setUp() {
        inheritedVerdictAdded = new InheritedVerdictAdded();
    }

    @Test
    public void transform() {
        JsonObject inheritedVerdictAddedTemplate = buildTemplateFromFile("hearing.events.inherited-verdict-added.json");
        assert inheritedVerdictAddedTemplate != null;
        JsonObject actual = inheritedVerdictAdded.transform(inheritedVerdictAddedTemplate);
        assertThat(actual.getString(HEARING_ID), is(inheritedVerdictAddedTemplate.getString(HEARING_ID)));
    }
}