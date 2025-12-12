package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.Objects.nonNull;
import uk.gov.justice.core.courts.VerdictType;

public class PleaVerdictUtil {



    private static final String GUILTY_VERDICT_STARTS_WITH = "GUILTY";

    private PleaVerdictUtil() {
    }

    public static boolean isGuiltyVerdict(VerdictType verdictType) {
        return nonNull(verdictType) && nonNull(verdictType.getCategoryType()) && verdictType.getCategoryType().startsWith(GUILTY_VERDICT_STARTS_WITH);
    }


}
