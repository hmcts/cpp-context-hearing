package uk.gov.moj.cpp.hearing.event.relist;


import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getAllPromptUuidsByPromptReference;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getDistinctPromptValue;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HCHOUSE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HDATE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTIME;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1067"})
public class HearingAdjournValidator {
    private static final int ONE = 1;
    private static final int ZERO = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingAdjournValidator.class);

    private ResultsSharedFilter resultsSharedFilter = new ResultsSharedFilter();

    public boolean validateProsecutionCase(final ResultsShared resultsSharedUnfiltered, final List<UUID> withdrawnResultDefinitionUuid, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final ResultsShared resultsShared = resultsSharedFilter.filterTargets(resultsSharedUnfiltered, t -> t.getApplicationId() == null);

        return !resultsShared.getTargets().isEmpty()
                && checkSharedResultHaveNextHearingOffenceResult(resultsShared, withdrawnResultDefinitionUuid, nextHearingResultDefinitions)
                && checkNextHearingDateOfHearingIsSameForAllOffences(getCompletedResultLines(resultsShared), nextHearingResultDefinitions)
                && checkNextHearingTypeIsSameForAllOffences(getCompletedResultLines(resultsShared), nextHearingResultDefinitions)
                && checkNextHearingEstimatedDurationIsSameForAllOffences(getCompletedResultLines(resultsShared), nextHearingResultDefinitions)
                && checkNextHearingCourtHouseIsSameForAllOffences(getCompletedResultLines(resultsShared), nextHearingResultDefinitions)
                && checkNextHearingTimeOfHearingIsSameForAllOffences(getCompletedResultLines(resultsShared), nextHearingResultDefinitions)
                ;
    }

    public boolean validateApplication(final ResultsShared resultsSharedUnfiltered, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final ResultsShared resultsShared = resultsSharedFilter.filterTargets(resultsSharedUnfiltered, t -> t.getApplicationId() != null);
        return !resultsShared.getTargets().isEmpty() && checkSharedResultHaveNextHearingApplicationResult(resultsShared, nextHearingResultDefinitions);
    }

    boolean checkNextHearingEstimatedDurationIsSameForAllOffences(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return checkIsSameForAllOffences(completedResultLines, nextHearingResultDefinitions, HEST);
    }

    boolean checkNextHearingTypeIsSameForAllOffences(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return checkIsSameForAllOffences(completedResultLines, nextHearingResultDefinitions, HTYPE);
    }

    boolean checkNextHearingDateOfHearingIsSameForAllOffences(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return checkIsSameForAllOffences(completedResultLines, nextHearingResultDefinitions, HDATE);
    }

    boolean checkNextHearingTimeOfHearingIsSameForAllOffences(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return checkIsSameForAllOffences(completedResultLines, nextHearingResultDefinitions, HTIME);
    }

    boolean checkNextHearingCourtHouseIsSameForAllOffences(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return checkIsSameForAllOffences(completedResultLines, nextHearingResultDefinitions, HCHOUSE);
    }

    boolean checkIsSameForAllOffences(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions, final NextHearingPromptReference promptRef) {
        final List<UUID> promptIdsToCheck = getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, promptRef);
        final Set<String> distinctValuesByOffence = getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, promptIdsToCheck);
        if (distinctValuesByOffence.size() > ONE && LOGGER.isErrorEnabled()) {
            LOGGER.error(String.format("Adjourn Hearing Rejected as %s provided for each offence is different, provided values are %s ", promptRef.name(), distinctValuesByOffence));
        }
        return distinctValuesByOffence.size() == ONE;
    }

    boolean checkSharedResultHaveNextHearingOffenceResult(final ResultsShared resultsShared, final List<UUID> withdrawnResultDefinitionUuid, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<Offence> flatOffences = resultsShared.getHearing().getProsecutionCases().stream().flatMap(pc -> pc.getDefendants().stream()).map(Defendant::getOffences).flatMap(List::stream)
                .collect(Collectors.toList());

        return flatOffences.size() == flatOffences.stream().filter(off -> isSharedResultHaveNextHearingOrWithdrawnOffenceResults(resultsShared.getTargets(),
                getCompletedResultLines(resultsShared), off,
                Stream.concat(withdrawnResultDefinitionUuid.stream(), nextHearingResultDefinitions.keySet().stream()).collect(Collectors.toList())
        )).count();
    }

    boolean checkSharedResultHaveNextHearingApplicationResult(final ResultsShared resultsShared, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<CourtApplication> applications = resultsShared.getHearing().getCourtApplications();
        if (applications == null) {
            return false;
        } else {
            return applications.stream().filter(app -> isSharedResultHaveNextHearingOrWithdrawnApplicationResults(resultsShared.getTargets(),
                    getCompletedResultLines(resultsShared), app,
                    nextHearingResultDefinitions.keySet().stream().collect(Collectors.toList())
            )).count() > 0;
        }
    }


    private Target getTargetByResultLine(final List<Target> targets, final ResultLine resultLine) {
        Target result;
        result = targets.stream().filter(target -> target.getResultLines().contains(resultLine)).findFirst().orElse(null);
        return result;
    }

    private boolean isSharedResultHaveNextHearingOrWithdrawnOffenceResults(final List<Target> targets, final List<ResultLine> completedResultLines, final Offence offence, final List<UUID> nextHearingAndWithdrawnIds) {
        return completedResultLines.stream()
                .filter(completedResultLine -> getTargetByResultLine(targets, completedResultLine).getOffenceId().equals(offence.getId()))
                .filter(completedResultLine -> nextHearingAndWithdrawnIds.contains(completedResultLine.getResultDefinitionId()))
                .count() > ZERO;
    }

    private boolean isSharedResultHaveNextHearingOrWithdrawnApplicationResults(final List<Target> targets, final List<ResultLine> completedResultLines, final CourtApplication application, final List<UUID> nextHearingAndWithdrawnIds) {
        final long result = completedResultLines.stream()
                .filter(completedResultLine -> application.getId().equals(getTargetByResultLine(targets, completedResultLine).getApplicationId()))
                .filter(completedResultLine -> nextHearingAndWithdrawnIds.contains(completedResultLine.getResultDefinitionId()))
                .count();
        return result > ZERO;
    }


    private List<ResultLine> getCompletedResultLines(final ResultsShared resultsShared) {
        return resultsShared.getTargets().stream().flatMap(target -> target.getResultLines().stream()).collect(Collectors.toList());
    }

}
