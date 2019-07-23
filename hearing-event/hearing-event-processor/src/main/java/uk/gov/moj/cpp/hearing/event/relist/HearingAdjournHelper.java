package uk.gov.moj.cpp.hearing.event.relist;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
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

    public static Set<String> getDistinctPromptValue(final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions, final List<UUID> dateOfHearingPromptIds) {
        return completedResultLines.stream().filter(completedResultLine -> nextHearingResultDefinitions.containsKey(completedResultLine.getResultDefinitionId()))
                .map(ResultLine::getPrompts).flatMap(List::stream)
                .filter(resultPrompt -> dateOfHearingPromptIds.contains(resultPrompt.getId()))
                .map(Prompt::getValue)
                .collect(Collectors.toSet());
    }

    public static List<Offence> getOffencesHaveResultNextHearing(final Defendant defendant, final List<Target> targets, final List<ResultLine> completedResultLines, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        return defendant.getOffences().stream().filter(off -> isSharedResultHaveNextHearingResults(targets, completedResultLines, off, nextHearingResultDefinitions.keySet())).collect(Collectors.toList());
    }

    private static Target findTargetByResultLine(final List<Target> targets, final ResultLine resultLine) {
        return targets.stream().filter(target -> target.getResultLines().contains(resultLine)).findFirst().orElse(null);
    }

    private static boolean isSharedResultHaveNextHearingResults(final List<Target> targets, final List<ResultLine> completedResultLines, final Offence offence, final Set<UUID> nextHearingResultIds) {
        return completedResultLines.stream()
                .filter(completedResultLine -> nextHearingResultIds.contains(completedResultLine.getResultDefinitionId()))
                .filter(completedResultLine ->
                {
                    final Target target = findTargetByResultLine(targets, completedResultLine);
                    return target != null && target.getOffenceId() != null && offence != null && target.getOffenceId().equals(offence.getId());
                })
                .count() > 0;
    }
}
