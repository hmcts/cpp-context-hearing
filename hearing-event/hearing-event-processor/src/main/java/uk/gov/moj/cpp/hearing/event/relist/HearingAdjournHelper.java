package uk.gov.moj.cpp.hearing.event.relist;

import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPrompt;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;


public final class HearingAdjournHelper {
    private HearingAdjournHelper() {
    }

    public static List<UUID> getAllPromptUuidsByPromptReference(final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions, NextHearingPromptReference nextHearingPromptReference) {
        return nextHearingResultDefinitions.values().stream().map(NextHearingResultDefinition::getNextHearingPrompts).flatMap(List::stream).filter(nextHearingPrompt -> StringUtils.isNotBlank(nextHearingPrompt.getPromptReference()))
                .filter(promptRef -> nextHearingPromptReference.name().equalsIgnoreCase(promptRef.getPromptReference()))
                .map(NextHearingPrompt::getId).collect(Collectors.toList());
    }

    public static Set<String> getDistinctPromptValue(final List<CompletedResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions, final List<UUID> dateOfHearingPromptIds) {
        return completedResultLines.stream().filter(completedResultLine -> nextHearingResultDefinitions.containsKey(completedResultLine.getResultDefinitionId()))
                .map(CompletedResultLine::getPrompts).flatMap(List::stream)
                .filter(resultPrompt -> dateOfHearingPromptIds.contains(resultPrompt.getId()))
                .map(ResultPrompt::getValue)
                .collect(Collectors.toSet());
    }

    public static List<Offence> getOffencesHaveResultNextHearing(final Defendant defendant, final List<CompletedResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return defendant.getOffences().stream().filter(off -> isSharedResultHaveNextHearingResults(completedResultLines, off, nextHearingResultDefinitions.keySet())).collect(Collectors.toList());
    }


    private static boolean isSharedResultHaveNextHearingResults(final List<CompletedResultLine> completedResultLines, final Offence offence, final Set<UUID> nextHearingResultIds) {
        return completedResultLines.stream()
                .filter(completedResultLine -> completedResultLine.getOffenceId().equals(offence.getId()))
                .filter(completedResultLine -> nextHearingResultIds.contains(completedResultLine.getResultDefinitionId()))
                .count() > 0;
    }
}
