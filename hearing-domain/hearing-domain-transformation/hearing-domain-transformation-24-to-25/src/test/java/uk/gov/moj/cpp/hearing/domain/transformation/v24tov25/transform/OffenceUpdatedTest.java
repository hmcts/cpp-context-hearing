package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class OffenceUpdatedTest {

    private OffenceUpdated offenceUpdated;

    @Before
    public void setUp() {
        offenceUpdated = new OffenceUpdated();
    }

    @Test
    public void transform() {
        JsonObject offenceUpdatedTemplate = buildTemplateFromFile("hearing.events.offence-updated.json");
        assert offenceUpdatedTemplate != null;
        JsonObject actual = offenceUpdated.transform(offenceUpdatedTemplate);
        assertThat(actual.getString(HEARING_ID), is(offenceUpdatedTemplate.getString(HEARING_ID)));
    }
}