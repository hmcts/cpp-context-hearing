package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static uk.gov.justice.core.courts.CourtApplication.courtApplication;
import static uk.gov.justice.core.courts.CourtApplicationCase.courtApplicationCase;
import static uk.gov.justice.core.courts.CourtOrder.courtOrder;
import static uk.gov.justice.core.courts.CourtOrderOffence.courtOrderOffence;
import static uk.gov.justice.core.courts.Hearing.hearing;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to sanitise hearing state prior to initiation of hearing
 */
public class HearingResultsCleanerUtil {

    private HearingResultsCleanerUtil() {
    }

    public static Hearing removeResultsFromHearing(final Hearing hearingFromPayload) {

        if (isNull(hearingFromPayload)) {
            return hearingFromPayload;
        }
        final Hearing.Builder hearingBuilder = hearing()
                .withValuesFrom(hearingFromPayload)
                .withDefendantJudicialResults(null);

        final List<ProsecutionCase> prosecutionCasesFromPayload = hearingFromPayload.getProsecutionCases();
        final List<ProsecutionCase> casesWithoutResults = isEmpty(prosecutionCasesFromPayload) ? prosecutionCasesFromPayload :
                prosecutionCasesFromPayload
                        .stream()
                        .map(HearingResultsCleanerUtil::processCase)
                        .collect(toList());

        if (isNotEmpty(hearingFromPayload.getCourtApplications())) {
            hearingBuilder.withCourtApplications(removeResultsFromCourtApplications(hearingFromPayload.getCourtApplications(), hearingFromPayload.getIsBoxHearing()));
        }

        return hearingBuilder
                .withProsecutionCases(casesWithoutResults)
                .build();
    }

    private static List<CourtApplication> removeResultsFromCourtApplications(List<CourtApplication> courtApplications, Boolean boxHearing) {
        return courtApplications.stream().map(courtApplication -> buildCourtApplication(courtApplication, boxHearing))
                .collect(Collectors.toList());
    }

    private static CourtApplication buildCourtApplication(CourtApplication courtApplication, Boolean boxHearing) {
        final CourtApplication.Builder builder = courtApplication()
                .withValuesFrom(courtApplication)
                .withJudicialResults(null)
                .withCourtOrder(buildCourtOrder(courtApplication.getCourtOrder()))
                .withCourtApplicationCases(buildCourtApplicationCases(courtApplication.getCourtApplicationCases()));

        if(isNotTrue(boxHearing)) {
            builder.withFutureSummonsHearing(null);
        }

        return builder.build();
    }

    private static List<CourtApplicationCase> buildCourtApplicationCases(List<CourtApplicationCase> courtApplicationCases) {
        return ofNullable(courtApplicationCases).map(Collection::stream).orElseGet(Stream::empty)
                .map(courtApplicationCase -> courtApplicationCase()
                        .withValuesFrom(courtApplicationCase)
                        .withOffences(ofNullable(courtApplicationCase.getOffences()).map(Collection::stream).orElseGet(Stream::empty)
                                .map(HearingResultsCleanerUtil::buildOffence)
                                .collect(collectingAndThen(toList(), getListOrNull())))
                        .build())
                .collect(collectingAndThen(toList(), getListOrNull()));
    }

    private static <T> UnaryOperator<List<T>> getListOrNull() {
        return list -> list.isEmpty() ? null : list;
    }

    private static CourtOrder buildCourtOrder(CourtOrder courtOrder) {
        return ofNullable(courtOrder)
                .map(courtOrder2 -> courtOrder().withValuesFrom(courtOrder2).withCourtOrderOffences(buildCourtOrderOffence(courtOrder2)).build())
                .orElse(courtOrder);
    }

    private static List<CourtOrderOffence> buildCourtOrderOffence(CourtOrder courtOrder) {
        return ofNullable(courtOrder.getCourtOrderOffences())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(courtOrderOffence -> courtOrderOffence().withValuesFrom(courtOrderOffence).withOffence(buildOffence(courtOrderOffence.getOffence())).build())
                .collect(toList());
    }

    private static Offence buildOffence(Offence offence) {
        return Offence.offence().withValuesFrom(offence).withJudicialResults(null).build();
    }

    private static ProsecutionCase processCase(final ProsecutionCase prosecutionCaseFromPayload) {
        if (isEmpty(prosecutionCaseFromPayload.getDefendants())) {
            return prosecutionCaseFromPayload;
        }

        final List<Defendant> defendantsWithoutResults = prosecutionCaseFromPayload.getDefendants()
                .stream()
                .map(HearingResultsCleanerUtil::processDefendants)
                .collect(toList());

        return ProsecutionCase.prosecutionCase()
                .withValuesFrom(prosecutionCaseFromPayload)
                .withDefendants(defendantsWithoutResults)
                .build();
    }

    private static Defendant processDefendants(final Defendant defendantFromPayload) {
        if (isEmpty(defendantFromPayload.getOffences())) {
            return defendantFromPayload;
        }

        final List<Offence> offencesWithoutResults = defendantFromPayload.getOffences()
                .stream()
                .map(HearingResultsCleanerUtil::processOffences)
                .collect(toList());

        return Defendant.defendant()
                .withValuesFrom(defendantFromPayload)
                .withDefendantCaseJudicialResults(null)
                .withOffences(offencesWithoutResults)
                .build();
    }

    private static Offence processOffences(final Offence offenceFromPayload) {
        if (isEmpty(offenceFromPayload.getJudicialResults())) {
            return offenceFromPayload;
        }

        return Offence.offence()
                .withValuesFrom(offenceFromPayload)
                .withJudicialResults(null)
                .build();
    }
}
