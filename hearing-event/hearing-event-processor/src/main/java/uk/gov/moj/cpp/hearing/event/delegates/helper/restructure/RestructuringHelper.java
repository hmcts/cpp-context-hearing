package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AlwaysPublishHelper.processAlwaysPublishResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DeDupeNextHearingHelper.deDupNextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DurationElementHelper.setDurationElements;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ExcludeResultsHelper.removeExcludedResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.processPublishAsPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNonPublishableLinesHelper.removeNonPublishableResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructureNextHearingHelper.restructureNextHearing;

import uk.gov.justice.core.courts.Category;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultUtil;
import uk.gov.moj.cpp.hearing.event.delegates.exception.ResultDefinitionNotFoundException;
import uk.gov.moj.cpp.hearing.event.delegates.helper.FinancialImpositionHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.JudicialResultPromptDurationHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.NextHearingHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.PenaltyPoint;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultTextHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


public class RestructuringHelper {

    protected static final String RESULT_DEFINITION_NOT_FOUND_FOR_RESULT_LINE_ID_S_RESULT_DEFINITION_ID_S_HEARING_ID_S_ORDERED_DATE_S = "resultDefinition not found for resultLineId: %s, resultDefinitionId: %s, hearingId: %s orderedDate: %s";
    private final ReferenceDataService referenceDataService;

    private final NextHearingHelper nextHearingHelper;

    @Inject
    public RestructuringHelper(final ReferenceDataService referenceDataService, final NextHearingHelper nextHearingHelper) {
        this.referenceDataService = referenceDataService;
        this.nextHearingHelper = nextHearingHelper;
    }

    public List<TreeNode<ResultLine>> restructure(final JsonEnvelope context, final ResultsShared resultsShared) {
        final List<TreeNode<ResultLine>> treeNodes = buildResultTreeFromPayload(context, resultsShared);

        updateResultText(
                removeNonPublishableResults(
                        restructureNextHearing(
                                processAlwaysPublishResults(
                                        deDupNextHearing(
                                                processPublishAsPrompt(
                                                        removeExcludedResults(treeNodes))
                                        )
                                )
                        )
                )
        );

        setDurationElements(treeNodes, resultsShared.getHearing());

        return treeNodes;
    }

    public List<TreeNode<ResultLine>> buildResultTreeFromPayload(final JsonEnvelope context, final ResultsShared resultsShared) {
        return mapTreeNodeRelations(mapToTreeNode(context, resultsShared)).values().stream().collect(toList());
    }

    private List<TreeNode<ResultLine>> updateResultText(final List<TreeNode<ResultLine>> treeNodeList) {
        treeNodeList.stream().forEach(treeNode -> {
            if (nonNull(treeNode.getJudicialResult()) && isNotEmpty(treeNode.getJudicialResult().getJudicialResultPrompts())) {
                final String sortedPrompts = treeNode.getJudicialResult().getJudicialResultPrompts()
                        .stream()
                        .map(p -> format("%s %s", p.getLabel(), p.getValue()))
                        .collect(joining(lineSeparator()));

                final String resultText = ResultTextHelper.getResultText(treeNode.getJudicialResult().getLabel(), sortedPrompts);
                treeNode.getJudicialResult().setResultText(resultText);
            }
        });
        return treeNodeList;
    }

    private Map<UUID, TreeNode<ResultLine>> mapTreeNodeRelations(final Map<UUID, TreeNode<ResultLine>> resultLinesMap) {
        resultLinesMap.values().stream().forEach(treeNode -> {
            final ResultLine resultLine = treeNode.getData();
            final TreeNode parentTreeNode = resultLinesMap.get(treeNode.getId());
            final List<UUID> childResultLineIds = resultLine.getChildResultLineIds();
            if (!isEmpty(childResultLineIds)) {
                childResultLineIds.stream().forEach(childId -> {
                            final TreeNode childTreeNode = resultLinesMap.get(childId);
                            parentTreeNode.addChild(childTreeNode);
                            childTreeNode.addParent(parentTreeNode);
                        }
                );
            }
        });
        return resultLinesMap;
    }

    @SuppressWarnings({"squid:S4973"})
    // suppress warning for using Boolean comparision-  when equals comparision is not intended.
    private Map<UUID, TreeNode<ResultLine>> mapToTreeNode(final JsonEnvelope context, final ResultsShared resultsShared) {
        final Map<UUID, TreeNode<ResultLine>> result = new HashMap<>();
        resultsShared.getTargets().stream().forEach(target -> {
            final List<ResultLine> resultLines = target.getResultLines();
            resultLines
                    .stream()
                    .filter(r1 -> r1.getIsDeleted() != Boolean.TRUE)
                    .forEach(resultLine -> {
                        final TreeNode<ResultDefinition> resultDefinitionNode = this.referenceDataService.getResultDefinitionTreeNodeById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());

                        if (resultDefinitionNode == null) {
                            throw new ResultDefinitionNotFoundException(format(
                                    RESULT_DEFINITION_NOT_FOUND_FOR_RESULT_LINE_ID_S_RESULT_DEFINITION_ID_S_HEARING_ID_S_ORDERED_DATE_S,
                                    resultLine.getResultLineId(), resultLine.getResultDefinitionId(), resultsShared.getHearingId(), resultLine.getOrderedDate()));
                        }

                        final JudicialResult judicialResult = getResultLineJudicialResult(context, resultLine, resultLines, resultsShared);
                        final TreeNode<ResultLine> treeNode = getResultLineTreeNode(target, resultLine, resultDefinitionNode, judicialResult);
                        result.put(treeNode.getId(), treeNode);
                    });
        });
        return result;
    }

    private TreeNode<ResultLine> getResultLineTreeNode(final Target target, final ResultLine resultLine, final TreeNode<ResultDefinition> resultDefinitionNode, final JudicialResult judicialResult) {
        final TreeNode<ResultLine> treeNode = new TreeNode(resultLine.getResultLineId(), resultLine);
        treeNode.setResultDefinition(resultDefinitionNode);
        treeNode.setJudicialResult(judicialResult);
        treeNode.setTargetId(target.getTargetId());
        treeNode.setResultDefinitionId(resultDefinitionNode.getId());
        treeNode.setApplicationId(target.getApplicationId());
        treeNode.setDefendantId(target.getDefendantId());
        treeNode.setOffenceId(target.getOffenceId());
        treeNode.setLevel(resultLine.getLevel());
        return treeNode;
    }

    private JudicialResult getResultLineJudicialResult(final JsonEnvelope context, final ResultLine resultLine, final List<ResultLine> resultLines, final ResultsShared resultsShared) {
        final Hearing hearing = resultsShared.getHearing();
        final DelegatedPowers courtClerk = resultsShared.getCourtClerk();
        final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus = resultsShared.getCompletedResultLinesStatus();
        final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());

        if (resultDefinition == null) {
            throw new ResultDefinitionNotFoundException(format(
                    RESULT_DEFINITION_NOT_FOUND_FOR_RESULT_LINE_ID_S_RESULT_DEFINITION_ID_S_HEARING_ID_S_ORDERED_DATE_S,
                    resultLine.getResultLineId(), resultLine.getResultDefinitionId(), hearing.getId(), resultLine.getOrderedDate()));
        }

        Optional<NextHearing> nextHearing = empty();

        Optional<JudicialResultPromptDurationElement> judicialResultPromptDurationElement = empty();
        Optional<String> qualifiers = empty();
        final List<JudicialResultPrompt> judicialResultPrompts = buildJudicialResultPrompt(resultDefinition, resultLine.getPrompts());

        if (null != judicialResultPrompts) {
            nextHearing = nextHearingHelper.getNextHearing(context, resultDefinition, resultLines, judicialResultPrompts);
            judicialResultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(judicialResultPrompts, hearing, resultDefinition);
            qualifiers = new ResultQualifier().populate(resultDefinition.getQualifier(), judicialResultPrompts, this.referenceDataService, context, resultLine.getOrderedDate());
        }

        final JudicialResult.Builder builder = JudicialResult.judicialResult()
                .withJudicialResultId(resultLine.getResultLineId())
                .withJudicialResultTypeId(resultDefinition.getId())
                .withAmendmentDate(resultLine.getAmendmentDate())
                .withAmendmentReason(resultLine.getAmendmentReason())
                .withAmendmentReasonId(resultLine.getAmendmentReasonId())
                .withApprovedDate(resultLine.getApprovedDate())
                .withCategory(getCategory(resultDefinition))
                .withCjsCode(resultDefinition.getCjsCode())
                .withCourtClerk(getOrDefaultCourtClerkAsDelegatePowers(completedResultLinesStatus, courtClerk, resultLine.getResultLineId()))
                .withDelegatedPowers(resultLine.getDelegatedPowers())
                .withFourEyesApproval(resultLine.getFourEyesApproval())
                .withIsAdjournmentResult(resultDefinition.isAdjournment())
                .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())
                .withIsConvictedResult(resultDefinition.isConvicted())
                .withIsFinancialResult(ResultDefinition.YES.equalsIgnoreCase(resultDefinition.getFinancial()))
                .withLabel(resultDefinition.getLabel())
                .withLastSharedDateTime(resultLine.getSharedDate() != null ? resultLine.getSharedDate().toString() : LocalDate.now().toString())
                .withOrderedDate(resultLine.getOrderedDate())
                .withOrderedHearingId(hearing.getId())
                .withRank(isNull(resultDefinition.getRank()) ? BigDecimal.ZERO : new BigDecimal(resultDefinition.getRank()))
                .withUsergroups(resultDefinition.getUserGroups())
                .withWelshLabel(resultDefinition.getWelshLabel())
                .withIsDeleted(resultLine.getIsDeleted())
                .withPostHearingCustodyStatus(resultDefinition.getPostHearingCustodyStatus())
                .withResultText(ResultTextHelper.getResultText(resultDefinition, resultLine))
                .withLifeDuration(getBooleanOrDefaultValue(resultDefinition.getLifeDuration()))
                .withResultDefinitionGroup(resultDefinition.getResultDefinitionGroup())
                .withTerminatesOffenceProceedings(getBooleanOrDefaultValue(resultDefinition.getTerminatesOffenceProceedings()))
                .withPublishedAsAPrompt(getBooleanOrDefaultValue(resultDefinition.getPublishedAsAPrompt()))
                .withExcludedFromResults(getBooleanOrDefaultValue(resultDefinition.getExcludedFromResults()))
                .withAlwaysPublished(getBooleanOrDefaultValue(resultDefinition.getAlwaysPublished()))
                .withUrgent(getBooleanOrDefaultValue(resultDefinition.getUrgent()))
                .withD20(getBooleanOrDefaultValue(resultDefinition.getD20()));

        if (CollectionUtils.isNotEmpty(judicialResultPrompts)) {
            final List<JudicialResultPrompt> updatedJudicialResultPrompt = new ArrayList<>();
            judicialResultPrompts.forEach(jc -> {
                jc.setValue(nonNull(jc.getValue())
                        ? jc.getValue().replaceAll(ResultQualifier.SEPARATOR, ",")
                        : jc.getValue());
                updatedJudicialResultPrompt.add(jc);
            });
            builder.withJudicialResultPrompts(updatedJudicialResultPrompt);
        }

        qualifiers.ifPresent(builder::withQualifier);
        judicialResultPromptDurationElement.ifPresent(builder::withDurationElement);
        nextHearing.ifPresent(builder::withNextHearing);

        return builder.build();
    }

    private Boolean getBooleanOrDefaultValue(final Boolean originalValue) {
        if (null == originalValue) {
            return false;
        }
        return originalValue;
    }

    private DelegatedPowers getOrDefaultCourtClerk(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final DelegatedPowers defaultCourtClerk, final UUID resultLineId) {
        if (completedResultLinesStatus.containsKey(resultLineId)) {
            return completedResultLinesStatus.get(resultLineId).getCourtClerk();
        } else {
            return defaultCourtClerk;
        }
    }

    private DelegatedPowers getOrDefaultCourtClerkAsDelegatePowers(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final DelegatedPowers defaultCourtClerk, final UUID resultLineId) {
        final DelegatedPowers courtClerk = getOrDefaultCourtClerk(completedResultLinesStatus, defaultCourtClerk, resultLineId);
        return DelegatedPowers.delegatedPowers()
                .withUserId(courtClerk.getUserId())
                .withFirstName(courtClerk.getFirstName())
                .withLastName(courtClerk.getLastName())
                .build();
    }

    private List<JudicialResultPrompt> buildJudicialResultPrompt(final ResultDefinition resultDefinition, final List<Prompt> prompts) {

        final List<JudicialResultPrompt> promptList = prompts.stream()
                .map(prompt -> {
                            final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition = resultDefinition.getPrompts().stream().filter(
                                    promptDef -> promptDef.getId().equals(prompt.getId()))
                                    .findFirst().orElseThrow(() -> new RuntimeException(format("no prompt definition found for prompt id: %s label: %s value: %s resultDefinitionId: %s", prompt.getId(), prompt.getLabel(), prompt.getValue(), resultDefinition.getId())));

                            return getJudicialResultPrompt(prompt, promptDefinition);
                        }
                )
                .collect(toList());

        return promptList.isEmpty() ? null : promptList;
    }

    private JudicialResultPrompt getJudicialResultPrompt(final Prompt prompt, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition) {
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptDefinition, prompt);

        return JudicialResultPrompt.judicialResultPrompt()
                .withJudicialResultPromptTypeId(prompt.getId())
                .withCourtExtract(getCalculatedCourtExtract(promptDefinition))
                .withLabel(prompt.getLabel())
                .withPromptReference(promptDefinition.getReference())
                .withPromptSequence(promptDefinition.getSequence() == null ? null : BigDecimal.valueOf(promptDefinition.getSequence()))
                .withUsergroups(promptDefinition.getUserGroups())
                .withValue(PublishResultUtil.reformatValue(prompt.getValue(), promptDefinition))
                .withType(promptDefinition.getType())
                .withQualifier(promptDefinition.getQual())
                .withTotalPenaltyPoints(new PenaltyPoint().getPenaltyPointFromResults(promptDefinition, prompt))
                .withIsFinancialImposition(financialImpositionHelper.isFinancialImposition())
                .withWelshLabel(prompt.getWelshValue())
                .withDurationSequence(promptDefinition.getDurationSequence())
                .build();
    }

    private String getCalculatedCourtExtract(final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition) {
        final String courtExtract = promptDefinition.getCourtExtract();
        if (StringUtils.isNotBlank(courtExtract)) {
            return courtExtract;
        }

        return promptDefinition.isAvailableForCourtExtract() ? "Y" : "N";
    }

    private Category getCategory(final ResultDefinition resultDefinition) {
        Category category = null;

        if (nonNull(resultDefinition) && nonNull(resultDefinition.getCategory())) {

            switch (resultDefinition.getCategory()) {
                case "A":
                    category = Category.ANCILLARY;
                    break;
                case "F":
                    category = Category.FINAL;
                    break;
                case "I":
                    category = Category.INTERMEDIARY;
                    break;
                default:
                    break;
            }
        }
        return category;
    }
}
