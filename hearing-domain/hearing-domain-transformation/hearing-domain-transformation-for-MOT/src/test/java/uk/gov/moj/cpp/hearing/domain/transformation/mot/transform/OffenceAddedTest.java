package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALLOCATION_DECISION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASE_ID;

public class OffenceAddedTest {

    private OffenceAdded offenceAdded;

    @Before
    public void setUp() {
        offenceAdded = new OffenceAdded();
    }

    @Test
    public void transform() {
        JsonObject offenceAddedTemplate = buildTemplateFromFile("hearing.events.offence-added.json");
        assert offenceAddedTemplate != null;
        JsonObject actual = offenceAdded.transform(offenceAddedTemplate);
        assertThat(actual.getString(HEARING_ID), is(offenceAddedTemplate.getString(HEARING_ID)));
        assertThat(actual.getString(PROSECUTION_CASE_ID), is(offenceAddedTemplate.getString(PROSECUTION_CASE_ID)));
        assertThat(actual.getString(DEFENDANT_ID), is(offenceAddedTemplate.getString(DEFENDANT_ID)));
        assertThat(actual.getJsonObject(OFFENCE).getJsonObject(ALLOCATION_DECISION).getString(MOT_REASON_CODE), is("1"));

    }
}