package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyVerdict;

import uk.gov.justice.core.courts.VerdictType;

import java.util.UUID;

import org.junit.Test;

public class PleaVerdictUtilTest {

    @Test
    public void shouldTestGuiltyVerdict() {
        assertTrue(isGuiltyVerdict(new VerdictType("Dummy Category", "GUILTY", "", "", UUID.randomUUID(), 1, "")));
        assertFalse(isGuiltyVerdict(new VerdictType("Dummy Category", "NOT GUILTY", "", "", UUID.randomUUID(), 1, "")));

    }
}