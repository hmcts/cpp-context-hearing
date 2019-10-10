package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALLOCATION_DECISION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE;

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
        assertThat(actual.getJsonObject(OFFENCE).
                getJsonObject(ALLOCATION_DECISION).getString(MOT_REASON_CODE), is("1"));
    }
}