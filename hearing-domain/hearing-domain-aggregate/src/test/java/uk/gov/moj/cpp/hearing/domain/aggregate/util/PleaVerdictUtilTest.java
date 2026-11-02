package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyVerdict;

import uk.gov.justice.core.courts.VerdictType;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class PleaVerdictUtilTest {

    @Test
    public void shouldTestGuiltyVerdict() {
        assertTrue(isGuiltyVerdict(new VerdictType("Dummy Category", "GUILTY", "", "", UUID.randomUUID(), 1, "")));
        assertFalse(isGuiltyVerdict(new VerdictType("Dummy Category", "NOT GUILTY", "", "", UUID.randomUUID(), 1, "")));

    }
}