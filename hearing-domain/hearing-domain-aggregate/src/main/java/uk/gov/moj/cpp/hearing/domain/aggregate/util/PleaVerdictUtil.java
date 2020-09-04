package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.Objects.nonNull;
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

import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.VerdictType;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class PleaVerdictUtil {

    private static final String GUILTY_VERDICT_STARTS_WITH = "GUILTY";

    public static final List<PleaValue> GUILTY_PLEA_VALUES = ImmutableList.of(GUILTY,
            MCA_GUILTY,
            AUTREFOIS_CONVICT,
            CONSENTS,
            CHANGE_TO_GUILTY_AFTER_SWORN_IN,
            CHANGE_TO_GUILTY_NO_SWORN_IN,
            CHANGE_TO_GUILTY_MAGISTRATES_COURT,
            GUILTY_TO_A_LESSER_OFFENCE_NAMELY,
            GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY,
            ADMITS);

    private PleaVerdictUtil() {
    }

    public static boolean isGuiltyVerdict(VerdictType verdictType) {
        return nonNull(verdictType) && nonNull(verdictType.getCategoryType()) && verdictType.getCategoryType().startsWith(GUILTY_VERDICT_STARTS_WITH);
    }

    @SuppressWarnings("squid:S2250")
    public static boolean isGuiltyPlea(PleaValue pleaValue) {
        return GUILTY_PLEA_VALUES.contains(pleaValue);
    }
}
