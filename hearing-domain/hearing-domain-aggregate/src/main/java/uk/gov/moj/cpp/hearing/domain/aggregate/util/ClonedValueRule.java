package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.Verdict;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Identifies plea, verdict and conviction date values on an application offence that were cloned
 * (inherited) from an earlier hearing rather than entered at the current hearing (CHD-3361 AC2, CHD-3370 AC8).
 */
public final class ClonedValueRule {

    private static final String REOPEN = "MC80524";
    private static final String STAT_DEC = "MC80527";
    private static final String STAT_DEC_SJP = "MC80528";

    private static final Set<String> SHARE_RULE_APPLICATION_TYPES = Set.of(STAT_DEC, STAT_DEC_SJP);

    private ClonedValueRule() {
    }

    public static boolean isShareRuleApplication(final CourtApplication courtApplication) {
        final String applicationTypeCode = applicationTypeCode(courtApplication);
        return nonNull(applicationTypeCode) && SHARE_RULE_APPLICATION_TYPES.contains(applicationTypeCode);
    }

    /**
     * The conviction-date rule for a verdict entered on cloned values applies to REOPEN only (CHD-3370);
     * CHD-3361 (STAT DEC) has no acceptance criterion for it.
     */
    public static boolean isReopenApplication(final CourtApplication courtApplication) {
        return REOPEN.equals(applicationTypeCode(courtApplication));
    }

    public static String applicationTypeCode(final CourtApplication courtApplication) {
        return nonNull(courtApplication) && nonNull(courtApplication.getType()) ? courtApplication.getType().getCode() : null;
    }

    public static boolean isCloned(final Plea plea, final UUID currentHearingId, final CourtApplication courtApplication) {
        return nonNull(plea) && isCloned(plea.getOriginatingHearingId(), plea.getPleaDate(), currentHearingId, courtApplication);
    }

    public static boolean isCloned(final Verdict verdict, final UUID currentHearingId, final CourtApplication courtApplication) {
        return nonNull(verdict) && isCloned(verdict.getOriginatingHearingId(), verdict.getVerdictDate(), currentHearingId, courtApplication);
    }

    /**
     * A conviction date earlier than the application was received cannot have come from this hearing.
     */
    public static boolean isInheritedConvictionDate(final LocalDate convictionDate, final CourtApplication courtApplication) {
        return nonNull(convictionDate) && isBeforeApplicationReceived(convictionDate, courtApplication);
    }

    private static boolean isCloned(final UUID originatingHearingId, final LocalDate date, final UUID currentHearingId, final CourtApplication courtApplication) {
        // a date on the day the application was received counts as entered at this hearing (work order OQ-6);
        // a dateless value from another hearing counts as cloned (work order OQ-7)
        return !currentHearingId.equals(originatingHearingId)
                && (isNull(date) || isBeforeApplicationReceived(date, courtApplication));
    }

    private static boolean isBeforeApplicationReceived(final LocalDate date, final CourtApplication courtApplication) {
        return nonNull(courtApplication.getApplicationReceivedDate()) && date.isBefore(courtApplication.getApplicationReceivedDate());
    }
}
