package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier.SEPARATOR;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.CategoryEnumUtils.getCategory;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.NO_PROMPT_DEFINITION_FOUND_EXCEPTION_FORMAT;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.REPLACEMENT_COMMA;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.RESULT_DEFINITION_NOT_FOUND_EXCEPTION_FORMAT;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.JudicialResultPromptMapper.findJudicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getBooleanValue;

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
import uk.gov.moj.cpp.hearing.event.delegates.exception.ResultDefinitionNotFoundException;
import uk.gov.moj.cpp.hearing.event.delegates.helper.JudicialResultPromptDurationHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.NextHearingHelper;
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

public class ResultTreeBuilder {
    private final ReferenceDataService referenceDataService;
    private final NextHearingHelper nextHearingHelper;

    @Inject
    public ResultTreeBuilder(final ReferenceDataService referenceDataService, final NextHearingHelper nextHearingHelper) {
        this.referenceDataService = referenceDataService;
        this.nextHearingHelper = nextHearingHelper;
    }

    public List<TreeNode<ResultLine>> build(final JsonEnvelope envelope, final ResultsShared resultsShared) {
        final Map<UUID, TreeNode<ResultLine>> resultLinesMap = getTreeNodeMap(envelope, resultsShared);
        return new ArrayList<>(mapTreeNodeRelations(resultLinesMap).values());
    }

    private Map<UUID, TreeNode<ResultLine>> mapTreeNodeRelations(final Map<UUID, TreeNode<ResultLine>> resultLinesMap) {
        resultLinesMap.values().forEach(treeNode -> {
            final ResultLine resultLine = treeNode.getData();
            final TreeNode<ResultLine> parentTreeNode = resultLinesMap.get(treeNode.getId());
            final List<UUID> childResultLineIds = resultLine.getChildResultLineIds();
            if (!isEmpty(childResultLineIds)) {
                childResultLineIds.forEach(childId -> {
                            final TreeNode<ResultLine> childTreeNode = resultLinesMap.get(childId);
                            parentTreeNode.addChild(childTreeNode);
                            childTreeNode.addParent(parentTreeNode);
                        }
                );
            }
        });
        return resultLinesMap;
    }

    private Map<UUID, TreeNode<ResultLine>> getTreeNodeMap(final JsonEnvelope context, final ResultsShared resultsShared) {
        final Map<UUID, TreeNode<ResultLine>> result = new HashMap<>();
        resultsShared.getTargets().forEach(target -> {
            final List<ResultLine> resultLines = target.getResultLines();
            resultLines
                    .stream()
                    .filter(resultLine -> !getBooleanValue(resultLine.getIsDeleted(), false))
                    .forEach(resultLine -> {
                        final TreeNode<ResultDefinition> resultDefinitionNode = referenceDataService.getResultDefinitionTreeNodeById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());

                        if (isNull(resultDefinitionNode)) {
                            throw new ResultDefinitionNotFoundException(format(RESULT_DEFINITION_NOT_FOUND_EXCEPTION_FORMAT,
                                    resultLine.getResultLineId(), resultLine.getResultDefinitionId(), resultsShared.getHearingId(), resultLine.getOrderedDate()));
                        }

                        final JudicialResult judicialResult = getResultLineJudicialResult(context, resultLine, resultLines, resultsShared);
                        final TreeNode<ResultLine> treeNode = getResultLineTreeNode(target, resultLine, resultDefinitionNode, judicialResult);
                        result.put(treeNode.getId(), treeNode);
                    });
        });
        return result;
    }

    @SuppressWarnings("squid:S3776")
    private JudicialResult getResultLineJudicialResult(final JsonEnvelope context, final ResultLine resultLine, final List<ResultLine> resultLines, final ResultsShared resultsShared) {
        final Hearing hearing = resultsShared.getHearing();
        final DelegatedPowers courtClerk = resultsShared.getCourtClerk();
        final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus = resultsShared.getCompletedResultLinesStatus();
        final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());

        if (isNull(resultDefinition)) {
            throw new ResultDefinitionNotFoundException(format(
                    RESULT_DEFINITION_NOT_FOUND_EXCEPTION_FORMAT,
                    resultLine.getResultLineId(), resultLine.getResultDefinitionId(), hearing.getId(), resultLine.getOrderedDate()));
        }

        final JudicialResult.Builder builder = JudicialResult.judicialResult()
                .withJudicialResultId(resultLine.getResultLineId())
                .withJudicialResultTypeId(resultDefinition.getId())
                .withAmendmentDate(resultLine.getAmendmentDate())
                .withAmendmentReason(resultLine.getAmendmentReason())
                .withAmendmentReasonId(resultLine.getAmendmentReasonId())
                .withApprovedDate(resultLine.getApprovedDate())
                .withCategory(getCategory(resultDefinition.getCategory()))
                .withCjsCode(resultDefinition.getCjsCode())
                .withCourtClerk(getOrDefaultCourtClerkAsDelegatePowers(completedResultLinesStatus, courtClerk, resultLine.getResultLineId()))
                .withDelegatedPowers(resultLine.getDelegatedPowers())
                .withFourEyesApproval(resultLine.getFourEyesApproval())
                .withIsAdjournmentResult(resultDefinition.isAdjournment())
                .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())
                .withIsConvictedResult(resultDefinition.isConvicted())
                .withIsFinancialResult(ResultDefinition.YES.equalsIgnoreCase(resultDefinition.getFinancial()))
                .withLabel(resultDefinition.getLabel())
                .withIsUnscheduled(resultDefinition.getUnscheduled())
                .withLastSharedDateTime(nonNull(resultLine.getSharedDate()) ? resultLine.getSharedDate().toString() : LocalDate.now().toString())
                .withOrderedDate(resultLine.getOrderedDate())
                .withOrderedHearingId(hearing.getId())
                .withRank(isNull(resultDefinition.getRank()) ? BigDecimal.ZERO : new BigDecimal(resultDefinition.getRank()))
                .withUsergroups(resultDefinition.getUserGroups())
                .withWelshLabel(resultDefinition.getWelshLabel())
                .withIsDeleted(resultLine.getIsDeleted())
                .withPostHearingCustodyStatus(resultDefinition.getPostHearingCustodyStatus())
                .withResultText(ResultTextHelper.getResultText(resultDefinition, resultLine))
                .withLifeDuration(getBooleanValue(resultDefinition.getLifeDuration(), false))
                .withResultDefinitionGroup(resultDefinition.getResultDefinitionGroup())
                .withTerminatesOffenceProceedings(getBooleanValue(resultDefinition.getTerminatesOffenceProceedings(), false))
                .withPublishedAsAPrompt(getBooleanValue(resultDefinition.getPublishedAsAPrompt(), false))
                .withExcludedFromResults(getBooleanValue(resultDefinition.getExcludedFromResults(), false))
                .withAlwaysPublished(getBooleanValue(resultDefinition.getAlwaysPublished(), false))
                .withUrgent(getBooleanValue(resultDefinition.getUrgent(), false))
                .withD20(getBooleanValue(resultDefinition.getD20(), false))
                .withRollUpPrompts(getBooleanValue(resultDefinition.getRollUpPrompts(), false))
                .withPublishedForNows(getBooleanValue(resultDefinition.getPublishedForNows(), false))
                .withResultWording(resultDefinition.getResultWording())
                .withWelshResultWording(resultDefinition.getWelshResultWording());

        //Set Parent Judicial Result Id and Judicial Result Type Id
        if (!isEmpty(resultLine.getParentResultLineIds())) {
            final List<UUID> parentResultLineIds = resultLine.getParentResultLineIds();
            parentResultLineIds.forEach(parentResultLineId -> {
                final ResultLine parentResultLine = getResultLine(resultLines, parentResultLineId);
                if(nonNull(parentResultLine)) {
                    builder.withParentJudicialResultId(parentResultLine.getResultLineId());
                    builder.withParentJudicialResultTypeId(parentResultLine.getResultDefinitionId());
                }
            });
        }

        final ResultLine rootResultLine = getRootResultLine(resultLines, resultLine);
        if (nonNull(rootResultLine)) {
            builder.withRootJudicialResultId(rootResultLine.getResultLineId());
            builder.withRootJudicialResultTypeId(rootResultLine.getResultDefinitionId());
        }

        final List<JudicialResultPrompt> judicialResultPrompts = buildJudicialResultPrompt(resultDefinition, resultLine.getPrompts());

        if (nonNull(judicialResultPrompts) && !judicialResultPrompts.isEmpty()) {
            final Optional<NextHearing> nextHearing = nextHearingHelper.getNextHearing(context, resultDefinition, resultLines, judicialResultPrompts);
            final Optional<JudicialResultPromptDurationElement> judicialResultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(judicialResultPrompts, hearing, resultDefinition);
            final Optional<String> qualifier = new ResultQualifier().populate(resultDefinition.getQualifier(), judicialResultPrompts, this.referenceDataService, context, resultLine.getOrderedDate());

            qualifier.ifPresent(builder::withQualifier);
            judicialResultPromptDurationElement.ifPresent(builder::withDurationElement);
            nextHearing.ifPresent(builder::withNextHearing);

            final List<JudicialResultPrompt> judicialResultPromptList = judicialResultPrompts.stream().map(prompt -> {
                if (nonNull(prompt.getValue())) {
                    prompt.setValue(prompt.getValue().replace(SEPARATOR, REPLACEMENT_COMMA));
                }
                if (nonNull(prompt.getWelshValue())) {
                    prompt.setWelshValue(prompt.getWelshValue().replace(SEPARATOR, REPLACEMENT_COMMA));
                }
                return prompt;
            }).collect(toList());

            if (!judicialResultPromptList.isEmpty()) {
                builder.withJudicialResultPrompts(judicialResultPromptList);
            }
        }

        return builder.build();
    }

    ResultLine getRootResultLine(final List<ResultLine> resultLines, final ResultLine currentResultLine) {
        if(isNull(currentResultLine) || isEmpty(currentResultLine.getParentResultLineIds())) {
            return currentResultLine;
        }

        ResultLine resultLine = null;
        final List<UUID> parentResultLineIds = currentResultLine.getParentResultLineIds();
        for (final UUID parentResultLineId : parentResultLineIds) {
            final ResultLine parentResultLine = getResultLine(resultLines, parentResultLineId);
            resultLine = getRootResultLine(resultLines, parentResultLine);
        }

        return resultLine;
    }

    private ResultLine getResultLine(final List<ResultLine> resultLines, final UUID resultLineId) {
        return resultLines.stream().filter(resultLine -> resultLineId.equals(resultLine.getResultLineId())).findFirst().orElse(null);
    }

    private TreeNode<ResultLine> getResultLineTreeNode(final Target target, final ResultLine resultLine, final TreeNode<ResultDefinition> resultDefinitionNode, final JudicialResult judicialResult) {
        final TreeNode<ResultLine> treeNode = new TreeNode<>(resultLine.getResultLineId(), resultLine);
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

    private DelegatedPowers getOrDefaultCourtClerkAsDelegatePowers(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatusMap, final DelegatedPowers defaultCourtClerk, final UUID resultLineId) {
        final CompletedResultLineStatus completedResultLineStatus = completedResultLinesStatusMap.getOrDefault(resultLineId, null);
        final DelegatedPowers courtClerk = nonNull(completedResultLineStatus) ? completedResultLineStatus.getCourtClerk() : defaultCourtClerk;
        return DelegatedPowers.delegatedPowers()
                .withUserId(courtClerk.getUserId())
                .withFirstName(courtClerk.getFirstName())
                .withLastName(courtClerk.getLastName())
                .build();
    }

    private List<JudicialResultPrompt> buildJudicialResultPrompt(final ResultDefinition resultDefinition, final List<Prompt> prompts) {
        return prompts.stream()
                .map(prompt -> {
                            final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition = resultDefinition.getPrompts().stream()
                                    .filter(promptDef -> promptDef.getId().equals(prompt.getId()) && (isNull(prompt.getPromptRef()) || prompt.getPromptRef().equals(promptDef.getReference())))
                                    .findFirst().orElseThrow(() -> new RuntimeException(format(NO_PROMPT_DEFINITION_FOUND_EXCEPTION_FORMAT, prompt.getId(), prompt.getPromptRef(), prompt.getLabel(), prompt.getValue(), resultDefinition.getId())));
                            return findJudicialResultPrompt(prompt, promptDefinition);
                        }
                )
                .collect(toList());
    }
}
