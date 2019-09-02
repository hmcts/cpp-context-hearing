package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ALCOHOL_READING_METHOD;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ALCOHOL_READING_METHOD_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE_FACTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PROSECUTION_CASE_ID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

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
        assertThat(actual.getJsonObject(OFFENCE).getJsonObject(OFFENCE_FACTS).getString(ALCOHOL_READING_METHOD_CODE),
                is(offenceAddedTemplate.getJsonObject(OFFENCE).getJsonObject(OFFENCE_FACTS).getString(ALCOHOL_READING_METHOD)));
    }
}