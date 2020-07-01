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
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPrompt;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    /**
     * See https://tools.hmcts.net/jira/browse/GPE-14178 Next hearing in Crown Court Result Definition with dateToBeFixed prompt is excluded from Adjourning
     */
    private static final UUID ADJOURN_EXCLUDED_RESULT_DEFINITION_ID = UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8");
    private static final String ADJOURN_EXCLUDED_RESULT_PROMPT_REFERENCE = "dateToBeFixed";

    private ResultsSharedFilter resultsSharedFilter = new ResultsSharedFilter();

    public boolean validateProsecutionCase(final ResultsShared resultsSharedUnfiltered, final List<UUID> withdrawnResultDefinitionUuid, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final ResultsShared resultsShared = resultsSharedFilter.filterTargets(resultsSharedUnfiltered, t -> t.getApplicationId() == null);

        return !resultsShared.getTargets().isEmpty()
                && checkSharedResultHaveNextHearingOrWithdrawnOffenceResult(resultsShared, withdrawnResultDefinitionUuid, nextHearingResultDefinitions)
                && checkSharedResultHaveAnyNextHearingResults(getCompletedResultLines(resultsShared), nextHearingResultDefinitions);
    }

    private boolean checkSharedResultHaveAnyNextHearingResults(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return completedResultLines.stream().anyMatch(
                completedResultLine -> nextHearingResultDefinitions.containsKey(completedResultLine.getResultDefinitionId())
                        && !isExcludedFromAdjourn(completedResultLine, nextHearingResultDefinitions));
    }

    private boolean isExcludedFromAdjourn(final ResultLine completedResultLine, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        if (!ADJOURN_EXCLUDED_RESULT_DEFINITION_ID.equals(completedResultLine.getResultDefinitionId())) {
            return false;
        }

        final NextHearingResultDefinition nextHearingResultDefinition = nextHearingResultDefinitions.get(ADJOURN_EXCLUDED_RESULT_DEFINITION_ID);
        if (nextHearingResultDefinition == null) {
            LOGGER.info("ADJOURN_EXCLUDED_RESULT_DEFINITION_ID is not inside nextHearingResultDefinitions: {}", nextHearingResultDefinitions);
            return false;
        }
        final NextHearingPrompt dateToBeFixedPrompt = nextHearingResultDefinition.getNextHearingPrompts().stream().filter(
                nextHearingPrompt -> ADJOURN_EXCLUDED_RESULT_PROMPT_REFERENCE.equalsIgnoreCase(nextHearingPrompt.getPromptReference())).findFirst().orElse(null);
        if (dateToBeFixedPrompt == null) {
            LOGGER.info("dateToBeFixed is not inside nextHearingResultDefinitions: {}", nextHearingResultDefinitions);
            return false;
        }
        return completedResultLine.getPrompts().stream().anyMatch(prompt -> Objects.equals(dateToBeFixedPrompt.getId(), prompt.getId()));

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

    boolean checkSharedResultHaveNextHearingOrWithdrawnOffenceResult(final ResultsShared resultsShared, final List<UUID> withdrawnResultDefinitionUuid, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
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
                    getCompletedResultLines(resultsShared), app, nextHearingResultDefinitions
            )).count() > 0;
        }
    }


    private Optional<Target> getTargetByResultLine(final List<Target> targets, final ResultLine resultLine) {
        return targets.stream().filter(target -> target.getResultLines().contains(resultLine)).findFirst();
    }

    private boolean isSharedResultHaveNextHearingOrWithdrawnOffenceResults(final List<Target> targets, final List<ResultLine> completedResultLines, final Offence offence, final List<UUID> nextHearingAndWithdrawnIds) {
        return completedResultLines.stream()
                .filter(completedResultLine -> getTargetByResultLine(targets, completedResultLine).isPresent())
                .filter(completedResultLine -> Objects.equals(offence.getId(), getTargetByResultLine(targets, completedResultLine).get().getOffenceId()))
                .filter(completedResultLine -> nextHearingAndWithdrawnIds.contains(completedResultLine.getResultDefinitionId()))
                .count() > ZERO;
    }

    private boolean isSharedResultHaveNextHearingOrWithdrawnApplicationResults(final List<Target> targets, final List<ResultLine> completedResultLines, final CourtApplication application,
                                                                               final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final long result = completedResultLines.stream()
                .filter(completedResultLine -> getTargetByResultLine(targets, completedResultLine).isPresent())
                .filter(completedResultLine -> Objects.equals(application.getId(), getTargetByResultLine(targets, completedResultLine).get().getApplicationId()))
                .filter(completedResultLine -> nextHearingResultDefinitions.containsKey(completedResultLine.getResultDefinitionId())
                        && !isExcludedFromAdjourn(completedResultLine, nextHearingResultDefinitions))
                .count();
        return result > ZERO;
    }


    private List<ResultLine> getCompletedResultLines(final ResultsShared resultsShared) {
        return resultsShared.getTargets().stream().flatMap(target -> target.getResultLines().stream()).collect(Collectors.toList());
    }

}
