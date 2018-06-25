package uk.gov.moj.cpp.hearing.event.nows;

import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class GenerateVariantDecisionMakerFactory {

    private List<Variant> variantDirectory;
    private Map<UUID, CompletedResultLineStatus> completedResultLineStatuses;
    private List<CompletedResultLine> completedResultLines;

    public GenerateVariantDecisionMakerFactory setVariantDirectory(List<Variant> variantDirectory) {
        this.variantDirectory = new ArrayList<>(variantDirectory);
        return this;
    }

    public GenerateVariantDecisionMakerFactory setCompletedResultLineStatuses(Map<UUID, CompletedResultLineStatus> completedResultLineStatuses) {
        this.completedResultLineStatuses = completedResultLineStatuses;
        return this;
    }

    public GenerateVariantDecisionMakerFactory setCompletedResultLines(List<CompletedResultLine> completedResultLines) {
        this.completedResultLines = new ArrayList<>(completedResultLines);
        return this;
    }

    public GenerateVariantDecisionMaker buildFor(UUID defendantId, NowDefinition nowDefinition){

        final Set<UUID> resultDefinitionIds4Now = nowDefinition.getResultDefinitions().stream()
                .map(ResultDefinitions::getId)
                .collect(toSet());

        final List<CompletedResultLine> completedResultLines4NowAndDefendant = completedResultLines.stream()
                .filter(resultLine -> resultLine.getDefendantId().equals(defendantId))
                .filter(l -> resultDefinitionIds4Now.contains(l.getResultDefinitionId()))
                .collect(toList());

        final Set<UUID> resultLineIds4NowAndDefendant = completedResultLines4NowAndDefendant.stream()
                .map(CompletedResultLine::getId)
                .collect(toSet());

        final Map<UUID, CompletedResultLineStatus> completedResultLineStatuses4NowAndDefendant = completedResultLineStatuses
                .entrySet()
                .stream()
                .filter(e -> resultLineIds4NowAndDefendant.contains(e.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final List<Variant> variantDirectory4NowAndDefendant = variantDirectory.stream()
                .filter(v -> v.getKey().getDefendantId().equals(defendantId))
                .filter(v -> v.getKey().getNowsTypeId().equals(nowDefinition.getId()))
                .collect(toList());

        return new GenerateVariantDecisionMaker(variantDirectory4NowAndDefendant, completedResultLineStatuses4NowAndDefendant, completedResultLines4NowAndDefendant);
    }
}
