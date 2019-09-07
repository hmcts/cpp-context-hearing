package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.JsonObjectSample.buildTemplateFromFile;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ALLOCATION_DECISION;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_IDS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.MOT_REASON_CODE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE;

public class OffenceUpdatedOnHearingsTest {

    private OffenceUpdatedOnHearings offenceUpdatedOnHearings;

    @Before
    public void setUp() {
        offenceUpdatedOnHearings = new OffenceUpdatedOnHearings();
    }

    @Test
    public void transform() {
        JsonObject offenceUpdatedTemplate = buildTemplateFromFile("hearing.events.offence-updated-on-hearings.json");
        assert offenceUpdatedTemplate != null;
        JsonObject actual = offenceUpdatedOnHearings.transform(offenceUpdatedTemplate, null);
        assertThat(actual.getJsonArray(HEARING_IDS), is(offenceUpdatedTemplate.getJsonArray(HEARING_IDS)));
        assertThat(actual.getJsonObject(OFFENCE).
                getJsonObject(ALLOCATION_DECISION).getString(MOT_REASON_CODE), is("1"));
    }
}