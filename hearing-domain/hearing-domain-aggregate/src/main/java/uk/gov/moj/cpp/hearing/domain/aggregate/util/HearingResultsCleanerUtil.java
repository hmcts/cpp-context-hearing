package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.util.List;
import java.util.Objects;

/**
 * Used to sanitise hearing state prior to initiation of hearing
 */
public class HearingResultsCleanerUtil {

    private HearingResultsCleanerUtil() {
    }

    public static Hearing removeResultsFromHearing(final Hearing hearingFromPayload) {

        if (Objects.isNull(hearingFromPayload)) {
            return hearingFromPayload;
        }

        final List<ProsecutionCase> prosecutionCasesFromPayload = hearingFromPayload.getProsecutionCases();
        final List<ProsecutionCase> casesWithoutResults = isEmpty(prosecutionCasesFromPayload) ? prosecutionCasesFromPayload :
                prosecutionCasesFromPayload
                        .stream()
                        .map(HearingResultsCleanerUtil::processCase)
                        .collect(toList());

        return Hearing.hearing()
                .withValuesFrom(hearingFromPayload)
                .withDefendantJudicialResults(null)
                .withProsecutionCases(casesWithoutResults)
                .build();
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
