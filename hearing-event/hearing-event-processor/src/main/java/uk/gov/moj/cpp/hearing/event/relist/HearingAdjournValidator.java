package uk.gov.moj.cpp.hearing.event.relist;


import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getAllPromptUuidsByPromptReference;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getDistinctPromptValue;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HDATE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;

import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearingAdjournValidator {
    private static final int ONE = 1;
    private static final int ZERO = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingAdjournValidator.class);

    public boolean validate(final ResultsShared resultsShared, final List<UUID> withdrawnResultDefinitionUuid, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {

        return checkSharedResultHaveNextHearingResult(resultsShared, withdrawnResultDefinitionUuid, nextHearingResultDefinitions)
                && checkNextHearingDateOfHearingIsSameForAllOffences(resultsShared.getCompletedResultLines(), nextHearingResultDefinitions)
                && checkNextHearingTypeIsSameForAllOffences(resultsShared.getCompletedResultLines(), nextHearingResultDefinitions)
                && checkNextHearingEstimatedDurationIsSameForAllOffences(resultsShared.getCompletedResultLines(), nextHearingResultDefinitions);

    }

    boolean checkNextHearingEstimatedDurationIsSameForAllOffences(final List<CompletedResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<UUID> estimatedDurationPromptIds = getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HEST);
        final Set<String> distinctEstimatedDurationValuesByOffence = getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, estimatedDurationPromptIds);
        if (distinctEstimatedDurationValuesByOffence.size() > ONE) {
            LOGGER.error("Adjourn Hearing Rejected as hearing estimated duration provided for each offence is different, provided values are {} ", distinctEstimatedDurationValuesByOffence);
        }
        return distinctEstimatedDurationValuesByOffence.size() == ONE;
    }

    boolean checkNextHearingTypeIsSameForAllOffences(final List<CompletedResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<UUID> typePromptIds = getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HTYPE);
        final Set<String> distinctHearingTypeValuesByOffence = getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, typePromptIds);
        if (distinctHearingTypeValuesByOffence.size() > ONE) {
            LOGGER.error("Adjourn Hearing Rejected as hearing type provided for each offence is different, provided values are{} ", distinctHearingTypeValuesByOffence);
        }
        return distinctHearingTypeValuesByOffence.size() == ONE;
    }

    boolean checkNextHearingDateOfHearingIsSameForAllOffences(final List<CompletedResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<UUID> dateOfHearingPromptIds = getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HDATE);
        final Set<String> distinctDateOfHearingValuesByOffence = getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, dateOfHearingPromptIds);
        if (distinctDateOfHearingValuesByOffence.size() > ONE) {
            LOGGER.error("Adjourn Hearing Rejected as date of hearing provided for each offence is different, provided values are {} ", distinctDateOfHearingValuesByOffence);
        }
        return distinctDateOfHearingValuesByOffence.size() == ONE;

    }

    boolean checkSharedResultHaveNextHearingResult(final ResultsShared resultsShared, final List<UUID> withdrawnResultDefinitionUuid, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<Offence> flatOffences = resultsShared.getHearing().getDefendants().stream().map(Defendant::getOffences).flatMap(List::stream)
                .collect(Collectors.toList());
        return flatOffences.size() == flatOffences.stream().filter(off -> isSharedResultHaveNextHearingOrWithdrawnResults(resultsShared.getCompletedResultLines(), off, Stream.concat(withdrawnResultDefinitionUuid.stream(), nextHearingResultDefinitions.keySet().stream()).collect(Collectors.toList()))).count();
    }


    private boolean isSharedResultHaveNextHearingOrWithdrawnResults(final List<CompletedResultLine> completedResultLines, final Offence offence, final List<UUID> nextHearingAndWithdrawnIds) {
        return completedResultLines.stream()
                .filter(completedResultLine -> completedResultLine.getOffenceId().equals(offence.getId()))
                .filter(completedResultLine -> nextHearingAndWithdrawnIds.contains(completedResultLine.getResultDefinitionId()))
                .count() > ZERO;
    }

}
