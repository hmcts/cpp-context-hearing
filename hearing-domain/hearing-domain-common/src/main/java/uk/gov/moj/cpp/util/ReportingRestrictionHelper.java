package uk.gov.moj.cpp.util;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ReportingRestriction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportingRestrictionHelper {

    private ReportingRestrictionHelper() {
    }

    public static List<Offence> dedupAllReportingRestrictions(final List<Offence> updatedOffences) {
        if (updatedOffences == null) {
            return updatedOffences;
        }

        return updatedOffences.
                stream().
                map(ReportingRestrictionHelper::dedupAllReportingRestrictionsForOffence).
                collect(toList());
    }

    public static ProsecutionCase dedupAllReportingRestrictions(final ProsecutionCase prosecutionCase) {
        if (prosecutionCase == null) {
            return prosecutionCase;
        }

        final ProsecutionCase result = ProsecutionCase.prosecutionCase().withValuesFrom(prosecutionCase)
                .withDefendants(
                        dedupAllReportingRestrictionsForDefendants(prosecutionCase.getDefendants())
                )
                .build();


        return result;
    }

    public static List<Defendant> dedupAllReportingRestrictionsForDefendants(final List<Defendant> defendants) {
        if (defendants == null) {
            return defendants;
        }
        return defendants.stream()
                .map(d -> dedupAllReportingRestrictions(d))
                .collect(toList());
    }

    public static Hearing dedupAllReportingRestrictions(final Hearing hearing) {
        if (hearing == null) {
            return hearing;
        }

        return Hearing.hearing().
                withValuesFrom(hearing).
                withProsecutionCases(dedupAllReportingRestrictionsForCases(hearing.getProsecutionCases())).
                withCourtApplications(dedupAllReportingRestrictionsForCourtApplications(hearing.getCourtApplications())).
                build();

    }

    public static List<ProsecutionCase> dedupAllReportingRestrictionsForCases(final List<ProsecutionCase> prosecutionCases) {
        if (prosecutionCases == null) {
            return prosecutionCases;
        }

        return prosecutionCases.
                stream().
                map(ReportingRestrictionHelper::dedupAllReportingRestrictions).
                collect(toList());
    }

    public static List<CourtApplication> dedupAllReportingRestrictionsForCourtApplications(final List<CourtApplication> courtApplications) {
        if (courtApplications == null) {
            return courtApplications;
        }

        return courtApplications.
                stream().
                map(ReportingRestrictionHelper::dedupAllReportingRestrictions).
                collect(toList());
    }

    public static final Defendant dedupAllReportingRestrictions(final Defendant defendant) {
        if (defendant == null) {
            return defendant;
        }

        return Defendant.defendant().withValuesFrom(defendant)
                .withOffences(dedupAllReportingRestrictions(defendant.getOffences()))
                .build();
    }


    public static CourtApplication dedupAllReportingRestrictions(final CourtApplication courtApplication) {
        if (courtApplication == null) {
            return courtApplication;
        }

        final CourtApplication result = CourtApplication.courtApplication().
                withValuesFrom(courtApplication).
                withCourtApplicationCases(dedupAllReportingRestrictionsForCourtApplicationCases(courtApplication.getCourtApplicationCases())).
                withCourtOrder(dedupAllReportingRestrictions(courtApplication.getCourtOrder())).build();

        return result;
    }

    private static List<CourtApplicationCase> dedupAllReportingRestrictionsForCourtApplicationCases(final List<CourtApplicationCase> courtApplicationCases) {
        if (isEmpty(courtApplicationCases)) {
            return courtApplicationCases;
        }
        return courtApplicationCases.
                stream().
                map(ReportingRestrictionHelper::dedupAllReportingRestrictions).
                collect(toList());
    }

    private static CourtApplicationCase dedupAllReportingRestrictions(final CourtApplicationCase courtApplicationCase) {
        if (courtApplicationCase == null) {
            return courtApplicationCase;
        }

        return CourtApplicationCase.
                courtApplicationCase().
                withValuesFrom(courtApplicationCase).
                withOffences(dedupAllReportingRestrictions(courtApplicationCase.
                        getOffences())).
                build();
    }

    private static CourtOrder dedupAllReportingRestrictions(final CourtOrder courtOrder) {
        if (courtOrder == null) {
            return courtOrder;
        }

        return CourtOrder.courtOrder().
                withValuesFrom(courtOrder).
                withCourtOrderOffences(dedupAllReportingRestrictionsFOrCourtOrderOffences(courtOrder.
                        getCourtOrderOffences())).build();

    }

    private static List<CourtOrderOffence> dedupAllReportingRestrictionsFOrCourtOrderOffences(final List<CourtOrderOffence> courtOrderOffences) {
        if (isEmpty(courtOrderOffences)) {
            return courtOrderOffences;
        }


        return courtOrderOffences.stream().map(ReportingRestrictionHelper::dedupAllReportingRestrictions).collect(toList());
    }

    private static CourtOrderOffence dedupAllReportingRestrictions(final CourtOrderOffence coOff) {
        if (coOff == null) {
            return coOff;
        }

        return CourtOrderOffence.courtOrderOffence().
                withValuesFrom(coOff).
                withOffence(dedupAllReportingRestrictionsForOffence(coOff.getOffence())).build();

    }

    private static Offence dedupAllReportingRestrictionsForOffence(final Offence offence) {
        if (offence == null) {
            return offence;
        }

        return Offence.offence().
                withValuesFrom(offence).
                withReportingRestrictions(dedupReportingRestrictions(offence.getReportingRestrictions())).
                build();
    }

    public static List<ReportingRestriction> dedupReportingRestrictions(final List<ReportingRestriction> reportingRestrictions) {
        if (reportingRestrictions == null) {
            return reportingRestrictions;
        }

        final Map<String, ReportingRestriction> res = new LinkedHashMap<>();
        for (final ReportingRestriction current : reportingRestrictions) {
            final String key = getKey(current);
            ReportingRestriction prev = res.get(key);
            if (prev == null) {
                prev = current;
            }

            res.put(key, oldestOf(prev, current));
        }

        return new ArrayList<>(res.values());
    }

    private static String getKey(final ReportingRestriction current) {
        return String.format("%s-%s", current.getLabel(), current.getJudicialResultId() == null ? "" : current.getJudicialResultId().toString());
    }

    private static ReportingRestriction oldestOf(final ReportingRestriction reportingRestriction1, final ReportingRestriction reportingRestriction2) {
        if (reportingRestriction1.getOrderedDate() == null) {
            return reportingRestriction2;
        }

        if (reportingRestriction2.getOrderedDate() == null) {
            return reportingRestriction1;
        }

        if (reportingRestriction2.getOrderedDate().isBefore(reportingRestriction1.getOrderedDate())) {
            return reportingRestriction2;
        }

        return reportingRestriction1;
    }
}
