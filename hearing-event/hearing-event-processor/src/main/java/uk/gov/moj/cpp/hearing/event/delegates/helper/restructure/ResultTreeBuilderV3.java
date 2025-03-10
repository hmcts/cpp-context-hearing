package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.SecondaryCJSCode.secondaryCJSCode;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier.SEPARATOR;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.CategoryEnumUtils.getCategory;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.NO_PROMPT_DEFINITION_FOUND_EXCEPTION_FORMAT;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.REPLACEMENT_COMMA;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.RESULT_DEFINITION_NOT_FOUND_EXCEPTION_FORMAT;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.JudicialResultPromptMapper.findJudicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getBooleanValue;


import java.util.Arrays;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResult.Builder;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.delegates.exception.ResultDefinitionNotFoundException;
import uk.gov.moj.cpp.hearing.event.delegates.helper.JudicialResultPromptDurationHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.NextHearingHelperV3;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultLineHelperV3;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultTextHelperV3;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.SecondaryCJSCode;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressWarnings("pmd:NullAssignment")
public class ResultTreeBuilderV3 {
    private final ReferenceDataService referenceDataService;
    private final NextHearingHelperV3 nextHearingHelper;
    private final ResultLineHelperV3 resultLineHelper;
    private ResultTextConfHelper resultTextConfHelper;


    @Inject
    public ResultTreeBuilderV3(final ReferenceDataService referenceDataService, final NextHearingHelperV3 nextHearingHelper, final ResultLineHelperV3 resultLineHelper, final ResultTextConfHelper resultTextConfHelper) {
        this.referenceDataService = referenceDataService;
        this.nextHearingHelper = nextHearingHelper;
        this.resultLineHelper = resultLineHelper;
        this.resultTextConfHelper = resultTextConfHelper;
    }

    public List<TreeNode<ResultLine2>> build(final JsonEnvelope envelope, final ResultsSharedV3 resultsShared, final List<TreeNode<ResultDefinition>> resultDefinitionTreeNods ) {
        final Map<UUID, TreeNode<ResultLine2>> resultLinesMap = getTreeNodeMap(envelope, resultsShared,resultDefinitionTreeNods);
        final Map<UUID, TreeNode<ResultLine2>> resultLinesMapWithRelations = mapTreeNodeRelations(resultLinesMap);
        return new ArrayList<>(orderResult(resultLinesMapWithRelations));
    }

    private Map<UUID, TreeNode<ResultLine2>> mapTreeNodeRelations(final Map<UUID, TreeNode<ResultLine2>> resultLinesMap) {
        resultLinesMap.values().forEach(treeNode -> {
            final ResultLine2 resultLine = treeNode.getData();
            final TreeNode<ResultLine2> parentTreeNode = resultLinesMap.get(treeNode.getId());
            final List<UUID> childResultLineIds = resultLine.getChildResultLineIds();
            if (!isEmpty(childResultLineIds)) {
                childResultLineIds.forEach(childId -> {
                            final TreeNode<ResultLine2> childTreeNode = resultLinesMap.get(childId);
                            parentTreeNode.addChild(childTreeNode);
                            childTreeNode.addParent(parentTreeNode);
                        }
                );
            }
        });
        return resultLinesMap;
    }

    private Map<UUID, TreeNode<ResultLine2>> getTreeNodeMap(final JsonEnvelope context, final ResultsSharedV3 resultsShared, final List<TreeNode<ResultDefinition>> resultDefinitionNodes) {
        final Map<UUID, TreeNode<ResultLine2>> result = new HashMap<>();
        final List<ResultLine2> allResultLines = resultsShared.getTargets().stream()
                .flatMap(t -> t.getResultLines().stream())
                .collect(toList());

       final List<ResultDefinition> resultDefinitions = resultDefinitionNodes.stream()
                .map(TreeNode::getData)
                .collect(toList());
        resultsShared.getTargets().forEach(target -> {
            final List<ResultLine2> resultLines = target.getResultLines();
            resultLines
                    .stream()
                    .filter(resultLine -> !getBooleanValue(resultLine.getIsDeleted(), false))
                    .forEach(resultLine -> {
                        final TreeNode<ResultDefinition> resultDefinitionNode = resultDefinitionNodes.stream()
                                .filter(rdt -> rdt.getData().getId().equals(resultLine.getResultDefinitionId()))
                                .findFirst()
                                .orElse(null);

                        if (isNull(resultDefinitionNode)) {
                            throw new ResultDefinitionNotFoundException(format(RESULT_DEFINITION_NOT_FOUND_EXCEPTION_FORMAT,
                                    resultLine.getResultLineId(), resultLine.getResultDefinitionId(), resultsShared.getHearingId(), resultLine.getOrderedDate()));
                        }

                        final ResultDefinition resultDefinition = resultDefinitionNode.getData();
                        final JudicialResult judicialResult = getResultLineJudicialResult(context, resultLine, allResultLines, resultsShared, resultDefinition, resultDefinitions);
                        final TreeNode<ResultLine2> treeNode = resultLineHelper.getResultLineTreeNode(target, resultLine, resultDefinitionNode, judicialResult);
                        result.put(treeNode.getId(), treeNode);
                    });
        });
        return result;
    }

    @SuppressWarnings({"squid:S3776", "squid:MethodCyclomaticComplexity"})
    private JudicialResult getResultLineJudicialResult(final JsonEnvelope context, final ResultLine2 resultLine, final List<ResultLine2> resultLines, final ResultsSharedV3 resultsShared, final ResultDefinition resultDefinition, final List<ResultDefinition> resultDefinitions) {
        final Hearing hearing = resultsShared.getHearing();
        final DelegatedPowers courtClerk = resultsShared.getCourtClerk();
        final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus = resultsShared.getCompletedResultLinesStatus();
        final Set<UUID> newAmendmentResultIds = resultsShared.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(Collectors.toSet());
        checkResultDefinition(resultLine, hearing, resultDefinition);

        final Builder builder = getJudicialBuilder(resultLine, hearing, courtClerk, completedResultLinesStatus, resultDefinition);

        setIsNewAmendmentResults(newAmendmentResultIds, resultLine, builder);

        setSecondaryCJSCodes(resultDefinition, builder);

        setDrivingTestStipulation(resultDefinition, builder);

        setPointsDisqualificationCode(resultDefinition, builder);

        setParentJudicialResult(resultLine, resultLines, builder);

        setRootJudicialResult(resultLine, resultLines, builder);

        setJudicialResultPrompts(context, resultLine, resultLines, resultDefinition, builder, resultsShared, resultDefinitions);

        return builder.build();
    }

    private void setIsNewAmendmentResults(final Set<UUID> newAmendmentResultIds, final ResultLine2 resultLine, final Builder builder) {
        builder.withIsNewAmendment(newAmendmentResultIds.contains(resultLine.getResultLineId()));

    }
    @SuppressWarnings("squid:S3776")


    private Builder getJudicialBuilder(final ResultLine2 resultLine, final Hearing hearing, final DelegatedPowers courtClerk, final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final ResultDefinition resultDefinition) {
        final JudicialResult.Builder judicialResult =  judicialResult()
                .withJudicialResultId(resultLine.getResultLineId())
                .withJudicialResultTypeId(resultDefinition.getId())
                .withAmendmentDate(nonNull(resultLine.getAmendmentDate())?resultLine.getAmendmentDate().toLocalDate():null)
                .withAmendmentReason(resultLine.getAmendmentReason())
                .withAmendmentReasonId(resultLine.getAmendmentReasonId())
                .withApprovedDate(resultLine.getApprovedDate())
                .withCategory(getCategory(resultDefinition.getCategory()))
                .withCjsCode(resultDefinition.getCjsCode())
                .withCourtClerk(getOrDefaultCourtClerkAsDelegatePowers(completedResultLinesStatus, courtClerk, resultLine.getResultLineId()))
                .withDelegatedPowers(resultLine.getDelegatedPowers())
                .withFourEyesApproval(resultLine.getFourEyesApproval())
                .withIsAdjournmentResult(resultDefinition.isAdjournment())
                .withPoliceSubjectLineTitle(resultDefinition.getPoliceSubjectLineTitle())
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
                .withWelshResultWording(resultDefinition.getWelshResultWording())
                .withCanBeSubjectOfBreach(resultDefinition.getCanBeSubjectOfBreach())
                .withCanBeSubjectOfVariation(resultDefinition.getCanBeSubjectOfVariation())
                .withDvlaCode(resultDefinition.getDvlaCode())
                .withLevel(resultDefinition.getLevel())
                .withPoliceSubjectLineTitle(resultDefinition.getPoliceSubjectLineTitle())
                .withPreserveActiveOrder(getBooleanValue(resultDefinition.getPreserveActiveOrder(), false))
                .withCanExtendActiveOrder(getBooleanValue(resultDefinition.getCanExtendActiveOrder(), false));;
        if(resultTextConfHelper.isOldResultDefinition(resultLine.getOrderedDate())) {
            judicialResult.withResultText(ResultTextHelperV3.getResultText(resultDefinition, resultLine));
        }
        return judicialResult;
    }

    private void checkResultDefinition(final ResultLine2 resultLine, final Hearing hearing, final ResultDefinition resultDefinition) {
        if (isNull(resultDefinition)) {
            throw new ResultDefinitionNotFoundException(format(
                    RESULT_DEFINITION_NOT_FOUND_EXCEPTION_FORMAT,
                    resultLine.getResultLineId(), resultLine.getResultDefinitionId(), hearing.getId(), resultLine.getOrderedDate()));
        }
    }

    private void setJudicialResultPrompts(final JsonEnvelope context, final ResultLine2 resultLine, final List<ResultLine2> resultLines, final ResultDefinition resultDefinition, final Builder builder, final ResultsSharedV3 resultsSharedV3, final List<ResultDefinition> resultDefinitions) {
        final List<JudicialResultPrompt> judicialResultPrompts = buildJudicialResultPrompt(resultDefinition, resultLine.getPrompts());

        if (nonNull(judicialResultPrompts) && !judicialResultPrompts.isEmpty()) {
            final Optional<NextHearing> nextHearing = nextHearingHelper.getNextHearing(context, resultDefinition, resultLines, resultLine, judicialResultPrompts, resultsSharedV3, resultDefinitions);
            final Optional<JudicialResultPromptDurationElement> judicialResultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(judicialResultPrompts, resultDefinition, resultLine.getOrderedDate());
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
    }

    private void setSecondaryCJSCodes(final ResultDefinition resultDefinition, final Builder builder) {
        if (!isEmpty(resultDefinition.getSecondaryCJSCodes())) {
            builder.withSecondaryCJSCodes(getSecondaryCjsCodeList(resultDefinition.getSecondaryCJSCodes()));
        }
        if(!isNull(resultDefinition.getDrivingTestStipulation())){
            builder.withDrivingTestStipulation(resultDefinition.getDrivingTestStipulation());
        }
        if(!isNull(resultDefinition.getPointsDisqualificationCode())){
            builder.withPointsDisqualificationCode(resultDefinition.getPointsDisqualificationCode());
        }
    }

    private void setDrivingTestStipulation(final ResultDefinition resultDefinition, final Builder builder) {

        if (!isNull(resultDefinition.getDrivingTestStipulation())) {
            builder.withDrivingTestStipulation(resultDefinition.getDrivingTestStipulation());
        }

    }

    private void setPointsDisqualificationCode(final ResultDefinition resultDefinition, final Builder builder) {

        if (!isNull(resultDefinition.getPointsDisqualificationCode())) {
            builder.withPointsDisqualificationCode(resultDefinition.getPointsDisqualificationCode());
        }

    }

    private void setRootJudicialResult(final ResultLine2 resultLine, final List<ResultLine2> resultLines, final Builder builder) {
        final ResultLine2 rootResultLine = resultLineHelper.getResultLine(resultLines, resultLine);
        if (nonNull(rootResultLine)) {
            builder.withRootJudicialResultId(rootResultLine.getResultLineId());
            builder.withRootJudicialResultTypeId(rootResultLine.getResultDefinitionId());
        }
    }

    private void setParentJudicialResult(final ResultLine2 resultLine, final List<ResultLine2> resultLines, final Builder builder) {
        //Set Parent Judicial Result Id and Judicial Result Type Id
        if (!isEmpty(resultLine.getParentResultLineIds())) {
            final List<UUID> parentResultLineIds = resultLine.getParentResultLineIds();
            parentResultLineIds.forEach(parentResultLineId -> {
                final ResultLine2 parentResultLine = resultLineHelper.findResultLine(resultLines, parentResultLineId);
                if (nonNull(parentResultLine)) {
                    builder.withParentJudicialResultId(parentResultLine.getResultLineId());
                    builder.withParentJudicialResultTypeId(parentResultLine.getResultDefinitionId());
                }
            });
        }
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

    private List<uk.gov.justice.core.courts.SecondaryCJSCode> getSecondaryCjsCodeList(final List<SecondaryCJSCode> secondaryCJSCodes){
        final List<uk.gov.justice.core.courts.SecondaryCJSCode> secondaryCJSCodeList = new ArrayList<>();
        for (final SecondaryCJSCode secondaryCJSCode: secondaryCJSCodes) {
            secondaryCJSCodeList.add(secondaryCJSCode()
                    .withCjsCode(secondaryCJSCode.getCjsCode())
                    .withText(secondaryCJSCode.getText())
                    .build());
        }
        return secondaryCJSCodeList;
    }

    private List<TreeNode<ResultLine2>> orderResult(final Map<UUID, TreeNode<ResultLine2>> resultLinesMap){
        final List<TreeNode<ResultLine2>> orderedInputList = new ArrayList(resultLinesMap.values());
        if(resultTextConfHelper.isOldResultDefinitionV2(orderedInputList)){
            return orderedInputList;
        }
        orderedInputList.sort(Comparator.comparing(o -> o.getResultDefinition().getData().getShortCode()));

        final List<TreeNode<ResultLine2>> parents = orderedInputList.stream().filter(node -> isEmpty(node.getParents()))
                .filter(node -> !isNull(node.getResultDefinition().getData().getDependantResultDefinitionGroup()))
                .collect(toList());
        final Set<UUID> parentIds = parents.stream().map(TreeNode::getId).collect(Collectors.toSet());
        final List<TreeNode<ResultLine2>> orderedResults = parents.stream()
                .map(parent -> orderedInputList.stream()
                        .filter(result -> !parentIds.contains(result.getId()))
                        .filter(result -> ofNullable(result.getOffenceId()).map(offenceId -> offenceId.equals(parent.getOffenceId())).orElse(true))
                        .filter(result -> ofNullable(result.getApplicationId()).map(applicationId -> applicationId.equals(parent.getApplicationId())).orElse(true))
                        .filter(result -> ! isNull(result.getResultDefinition().getData().getResultDefinitionGroup()))
                        .filter(result -> Arrays.stream(result.getResultDefinition().getData().getResultDefinitionGroup().split(","))
                                .map(String::trim)
                                .collect(Collectors.toSet()).contains(parent.getResultDefinition().getData().getDependantResultDefinitionGroup()))
                        .collect(Collectors.collectingAndThen(toList(), list -> {
                            list.add(0, parent);
                            return list;
                        })))
                .flatMap(Collection::stream).collect(toList());

        orderedInputList.stream().filter(node -> isEmpty(node.getParents()))
                .filter(node -> isNull(node.getResultDefinition().getData().getDependantResultDefinitionGroup()))
                .collect(toList())
                .forEach(orderedResults::add);

        orderedInputList.stream()
                .filter(result -> orderedResults.stream().noneMatch(res -> res.getId().equals(result.getId())))
                .forEach(orderedResults::add);

        return orderedResults;
    }

}
