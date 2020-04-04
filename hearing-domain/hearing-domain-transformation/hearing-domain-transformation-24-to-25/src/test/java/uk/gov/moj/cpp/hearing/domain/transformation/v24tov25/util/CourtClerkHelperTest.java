package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.JsonObjectSample.buildCourtClerk;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.FIRST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.LAST_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.USER_ID;

import javax.json.JsonObject;

import org.junit.Test;

public class CourtClerkHelperTest {

    @Test
    public void transformCourtClerk() {

        JsonObject courtClerk = buildCourtClerk();
        JsonObject actual = CourtClerkHelper.transformCourtClerk(courtClerk);
        assertThat(actual.getString(USER_ID), is(courtClerk.getString(ID)));
        assertThat(actual.getString(FIRST_NAME), is(courtClerk.getString(FIRST_NAME)));
        assertThat(actual.getString(LAST_NAME), is(courtClerk.getString(LAST_NAME)));
    }
}