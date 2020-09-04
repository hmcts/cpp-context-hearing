package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.core.courts.PleaValue.ADMITS;
import static uk.gov.justice.core.courts.PleaValue.AUTREFOIS_CONVICT;
import static uk.gov.justice.core.courts.PleaValue.CHANGE_TO_GUILTY_AFTER_SWORN_IN;
import static uk.gov.justice.core.courts.PleaValue.CHANGE_TO_GUILTY_MAGISTRATES_COURT;
import static uk.gov.justice.core.courts.PleaValue.CHANGE_TO_GUILTY_NO_SWORN_IN;
import static uk.gov.justice.core.courts.PleaValue.CONSENTS;
import static uk.gov.justice.core.courts.PleaValue.GUILTY;
import static uk.gov.justice.core.courts.PleaValue.GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY;
import static uk.gov.justice.core.courts.PleaValue.GUILTY_TO_A_LESSER_OFFENCE_NAMELY;
import static uk.gov.justice.core.courts.PleaValue.MCA_GUILTY;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyPlea;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyVerdict;

import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.VerdictType;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class PleaVerdictUtilTest {

    private static final List<PleaValue> GUILTY_PLEA_LIST = newArrayList(GUILTY,
            MCA_GUILTY,
            AUTREFOIS_CONVICT,
            CONSENTS,
            CHANGE_TO_GUILTY_AFTER_SWORN_IN,
            CHANGE_TO_GUILTY_NO_SWORN_IN,
            CHANGE_TO_GUILTY_MAGISTRATES_COURT,
            GUILTY_TO_A_LESSER_OFFENCE_NAMELY,
            GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY,
            ADMITS);

    @Test
    public void shouldTestGuiltyVerdict() {
        assertTrue(isGuiltyVerdict(new VerdictType("Dummy Category", "GUILTY", "", "", UUID.randomUUID(), 1, "")));
        assertFalse(isGuiltyVerdict(new VerdictType("Dummy Category", "NOT GUILTY", "", "", UUID.randomUUID(), 1, "")));

    }

    @Test
    public void shouldTestGuiltyPlea() {

        for (PleaValue pleaValue : PleaValue.values()) {
            if (GUILTY_PLEA_LIST.contains(pleaValue)) {
                assertTrue(isGuiltyPlea(pleaValue));
            } else {
                assertFalse(isGuiltyPlea(pleaValue));
            }
        }
    }
}