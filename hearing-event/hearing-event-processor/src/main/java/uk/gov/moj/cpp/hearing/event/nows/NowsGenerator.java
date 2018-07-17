package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188", "squid:S2384"})
public class NowsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsGenerator.class.getName());

    private final ReferenceDataService referenceDataService;

    @Inject
    public NowsGenerator(final ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public void setContext(final JsonEnvelope context) {
        referenceDataService.setContext(context);
    }

    public List<Nows> createNows(final ResultsShared resultsShared) {

        final GenerateVariantDecisionMakerFactory generateVariantDecisionMakerFactory = new GenerateVariantDecisionMakerFactory()
                .setVariantDirectory(resultsShared.getVariantDirectory())
                .setCompletedResultLineStatuses(resultsShared.getCompletedResultLinesStatus())
                .setCompletedResultLines(resultsShared.getCompletedResultLines());

        final List<Nows> nows = new ArrayList<>();

        resultsShared.getHearing().getDefendants().forEach(defendant -> {

                    if (anyUncompletedResultLinesForDefendant(resultsShared, defendant)) {
                        LOGGER.info("aborting NOWs generation for defendant {} as there are uncompleted result lines", defendant.getId());
                        return; //we don't generate any NOW for the defendant if they have any uncompleted result lines.
                    }

                    final List<CompletedResultLine> completedResultLines4Defendant = resultsShared.getCompletedResultLines().stream()
                            .filter(resultLine -> resultLine.getDefendantId().equals(defendant.getId()))
                            .collect(toList());

                    nows.addAll(createNowsForDefendant(defendant, completedResultLines4Defendant, generateVariantDecisionMakerFactory));
                }
        );

        return nows;
    }

    private List<Nows> createNowsForDefendant(final Defendant defendant,
                                              final List<CompletedResultLine> resultLines,
                                              final GenerateVariantDecisionMakerFactory generateVariantDecisionMakerFactory) {

        final Set<UUID> completedResultDefinitionIds = resultLines.stream()
                .map(CompletedResultLine::getResultDefinitionId)
                .collect(toSet());

        final List<Nows> results = new ArrayList<>();

        for (final NowDefinition nowDefinition : findNowDefinitions(resultLines)) {

            if (anyMandatoryResultLineNotPresent(completedResultDefinitionIds, nowDefinition)) {
                LOGGER.info("aborting NOW generation {} for defendant {} as not all mandatory results are present", defendant.getId(), nowDefinition.getId());
                return Collections.emptyList(); //This will suspend any now generation for the defendant.
            }

            final Set<UUID> resultDefinitionIds4Now = nowDefinition.getResultDefinitions().stream()
                    .map(ResultDefinitions::getId)
                    .collect(toSet());

            final List<CompletedResultLine> resultLines4Now = resultLines.stream()
                    .filter(l -> resultDefinitionIds4Now.contains(l.getResultDefinitionId()))
                    .collect(toList());
            final Nows nows = createNow(nowDefinition, resultLines4Now, defendant.getId(),
                    generateVariantDecisionMakerFactory.buildFor(defendant.getId(),
                            nowDefinition));
            if (!nows.getMaterials().isEmpty()) {
                results.add(nows);
            }
        }
        return results;
    }

    private Nows createNow(final NowDefinition nowDefinition,
                           final List<CompletedResultLine> resultLines4Now, final UUID defendantId,
                           final GenerateVariantDecisionMaker generateVariantDecisionMaker) {

        //The userGroups of the prompts are not bounded by the userGroups of the resultDefinition.  I'm told that the userGroups
        //of the resultDefinition is more of a default for when no prompts are present.

        final Map<NowVariant, List<String>> variantToUserGroupsMappings = calculateVariants(resultLines4Now);

        final List<Material> materials = new ArrayList<>();

        variantToUserGroupsMappings.forEach((variant, userGroups) -> {

            final GenerateVariantDecisionMaker.Decision decision = generateVariantDecisionMaker.decide(userGroups);

            if (!decision.isShouldGenerate()) {
                LOGGER.info("NOW variant is not generated on direction of decision maker");
                return;
            }

            final Set<CompletedResultLine> resultLines4Variant = resultLines4Now.stream()
                    .filter(resultLine -> variant.getResultDefinitionsIds().contains(resultLine.getResultDefinitionId()))
                    .collect(toSet());

            final List<NowResult> nowResults = resultLines4Variant.stream()
                    .map(resultLine -> {
                        final ResultDefinition resultDefinition = referenceDataService.getResultDefinitionById(resultLine.getOrderedDate(),
                                resultLine.getResultDefinitionId());

                        final List<PromptRef> promptRefs = resultDefinition.getPrompts().stream()
                                .filter(prompt -> variant.getResultPromptIds().contains(prompt.getId())) //filter out prompts that this variant should not have.
                                .map(prompt -> PromptRef.promptRef().setLabel(prompt.getLabel()).setId(prompt.getId()))
                                .collect(toList());

                        return NowResult.nowResult()
                                .setSharedResultId(resultLine.getId())
                                .setSequence(resultDefinition.getRank())
                                .setPrompts(promptRefs);
                    })
                    .collect(toList());

            materials.add(Material.material()
                    .setId(UUID.randomUUID())
                    .setNowResult(nowResults)
                    .setUserGroups(userGroups.stream()
                            .map(userGroup -> UserGroups.userGroups().setGroup(userGroup))
                            .collect(toList())
                    )
                    .setAmended(decision.isAmended())
            );
        });

        //lets make the materials always return in a fixed order
        materials.sort((m1, m2) -> {
            final String ug1 = m1.getUserGroups().stream().map(UserGroups::getGroup).sorted().collect(Collectors.joining(""));
            final String ug2 = m2.getUserGroups().stream().map(UserGroups::getGroup).sorted().collect(Collectors.joining(""));
            return ug1.compareTo(ug2);
        });

        return Nows.nows()
                .setId(UUID.randomUUID())
                .setDefendantId(defendantId)
                .setNowsTypeId(nowDefinition.getId())
                .setMaterials(materials)
                .setReferenceDate(nowDefinition.getReferenceDate());
    }

    private Map<NowVariant, List<String>> calculateVariants(final List<CompletedResultLine> resultLines4Now) {
        final Map<NowVariant, List<String>> variantToUserGroupsMappings = new HashMap<>();

        for (final String userGroup : extractUserGroupsFromResultLinesAndPrompts(resultLines4Now)) {

            final Set<UUID> resultDefinitionsIds4UserGroup = resultLines4Now.stream().map(
                    resultLine -> referenceDataService.getResultDefinitionById(resultLine.getOrderedDate(), resultLine.getResultDefinitionId()))
                    .filter(resultDefinition -> resultDefinition.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)) ||
                            resultDefinition.getPrompts().stream().anyMatch(p -> p.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)))
                    )
                    .map(ResultDefinition::getId)
                    .collect(toSet());

            final Set<UUID> resultPromptIds4UserGroup = resultLines4Now.stream()
                    .map(resultLine -> referenceDataService.getResultDefinitionById(resultLine.getOrderedDate(), resultLine.getResultDefinitionId()))
                    .flatMap(resultDefinition -> resultDefinition.getPrompts().stream())
                    .filter(resultPrompt -> resultPrompt.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)))
                    .map(Prompt::getId)
                    .collect(toSet());

            variantToUserGroupsMappings.computeIfAbsent(new NowVariant(resultDefinitionsIds4UserGroup, resultPromptIds4UserGroup), v -> new ArrayList<>())
                    .add(userGroup);
        }
        return variantToUserGroupsMappings;
    }

    private Set<String> extractUserGroupsFromResultLinesAndPrompts(
                    final List<CompletedResultLine> resultLines) {

        return resultLines.stream()
                .flatMap(resultLine -> {
                            final ResultDefinition resultDefinition = referenceDataService
                                    .getResultDefinitionById(resultLine.getOrderedDate(),
                                            resultLine.getResultDefinitionId());

                            return Stream.concat(
                                    resultDefinition.getUserGroups().stream(),
                                    resultDefinition.getPrompts().stream().flatMap(prompt -> prompt.getUserGroups().stream())

                            );
                        }
                ).collect(toSet());
    }

    private Set<NowDefinition> findNowDefinitions(final List<CompletedResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService
                        .getNowDefinitionByPrimaryResultDefinitionId(
                                resultLine.getOrderedDate(),
                                resultLine.getResultDefinitionId()))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private static boolean anyUncompletedResultLinesForDefendant(final ResultsShared resultsShared, final Defendant defendant) {
        return resultsShared.getUncompletedResultLines().stream().anyMatch(l -> l.getDefendantId().equals(defendant.getId()));
    }

    private static boolean anyMandatoryResultLineNotPresent(final Set<UUID> completedResultDefinitionIds, final NowDefinition nowDefinition) {
        return nowDefinition.getResultDefinitions().stream().anyMatch(resultDefinition -> resultDefinition.getMandatory() &&
                !completedResultDefinitionIds.contains(resultDefinition.getId()));
    }

    private static class NowVariant {
        private final Set<UUID> resultDefinitionsIds;
        private final Set<UUID> resultPromptIds;

        NowVariant(final Set<UUID> resultDefinitionsIds, final Set<UUID> resultPromptIds) {
            this.resultDefinitionsIds = new HashSet<>(resultDefinitionsIds);
            this.resultPromptIds = new HashSet<>(resultPromptIds);
        }

        Set<UUID> getResultDefinitionsIds() {
            return resultDefinitionsIds;
        }

        Set<UUID> getResultPromptIds() {
            return resultPromptIds;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final NowVariant that = (NowVariant) o;
            return Objects.equals(resultDefinitionsIds, that.resultDefinitionsIds) &&
                    Objects.equals(resultPromptIds, that.resultPromptIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resultDefinitionsIds, resultPromptIds);
        }
    }
}
