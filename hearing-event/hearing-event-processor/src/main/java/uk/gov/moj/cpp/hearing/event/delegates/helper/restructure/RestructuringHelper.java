package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AlwaysPublishHelper.processAlwaysPublishResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DeDupeNextHearingHelper.deDupNextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DurationElementHelper.setDurationElements;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ExcludeResultsHelper.removeExcludedResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.processPublishAsPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishedForNowsHelper.getNodesWithPublishedForNows;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNonPublishableLinesHelper.removeNonPublishableResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructureNextHearingHelper.restructureNextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructuringHelperV3.JUDICIAL_RESULT_PROMPT_PREDICATE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RollUpPromptsHelper.filterNodesWithRollUpPrompts;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultTextHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

import javax.inject.Inject;

public class RestructuringHelper {

    private final ResultTreeBuilder resultTreeBuilder;
    private final ResultTextConfHelper resultTextConfHelper;

    @Inject
    public RestructuringHelper(final ResultTreeBuilder resultTreeBuilder, final ResultTextConfHelper resultTextConfHelper) {
        this.resultTreeBuilder = resultTreeBuilder;
        this.resultTextConfHelper = resultTextConfHelper;
    }

    public List<TreeNode<ResultLine>> restructure(final JsonEnvelope context, final ResultsShared resultsShared) {
        final List<TreeNode<ResultLine>> treeNodes = resultTreeBuilder.build(context, resultsShared);

        final List<TreeNode<ResultLine>> publishedForNowsNodes = getNodesWithPublishedForNows(treeNodes);
        if(resultTextConfHelper.isOldResultDefinition(treeNodes)) {
            updateResultText(
                    removeNonPublishableResults(
                            restructureNextHearing(
                                    processAlwaysPublishResults(
                                            deDupNextHearing(
                                                    filterNodesWithRollUpPrompts(
                                                            processPublishAsPrompt(
                                                                    removeExcludedResults(treeNodes))
                                                    )
                                            )
                                    )
                            )
                    )
            );
        } else {
            removeNonPublishableResults(
                    restructureNextHearing(
                            processAlwaysPublishResults(
                                    deDupNextHearing(
                                            filterNodesWithRollUpPrompts(
                                                    processPublishAsPrompt(
                                                            removeExcludedResults(
                                                                    updateResultTextWithNewLogic(treeNodes)
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }
        setDurationElements(treeNodes);
        treeNodes.forEach(treeNode -> treeNode.getJudicialResult().setPublishedForNows(FALSE));
        final List<TreeNode<ResultLine>> publishedForNowsNodesNotInRollup = publishedForNowsNodes.stream()
                .filter(node -> treeNodes.stream().noneMatch(tn -> tn.getId().equals(node.getId())))
                .collect(toList());
        removeNextHearingObject(publishedForNowsNodesNotInRollup);
        treeNodes.addAll(publishedForNowsNodesNotInRollup);
        return treeNodes;
    }

    public List<TreeNode<ResultLine>> restructure(final JsonEnvelope context, final ResultsSharedV2 resultsShared) {
        final List<TreeNode<ResultLine>> treeNodes = resultTreeBuilder.build(context, resultsShared);

        final List<TreeNode<ResultLine>> publishedForNowsNodes = getNodesWithPublishedForNows(treeNodes);

        if(resultTextConfHelper.isOldResultDefinition(treeNodes)){
            updateResultText(
                    removeNonPublishableResults(
                            restructureNextHearing(
                                    processAlwaysPublishResults(
                                            deDupNextHearing(
                                                    filterNodesWithRollUpPrompts(
                                                            processPublishAsPrompt(
                                                                    removeExcludedResults(treeNodes))
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }else {
            removeNonPublishableResults(
                    restructureNextHearing(
                            processAlwaysPublishResults(
                                    deDupNextHearing(
                                            filterNodesWithRollUpPrompts(
                                                    processPublishAsPrompt(
                                                            removeExcludedResults(
                                                                    updateResultTextWithNewLogic(treeNodes)
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }
        setDurationElements(treeNodes);
        treeNodes.forEach(treeNode -> treeNode.getJudicialResult().setPublishedForNows(FALSE));
        final List<TreeNode<ResultLine>> publishedForNowsNodesNotInRollup = publishedForNowsNodes.stream()
                .filter(node -> treeNodes.stream().noneMatch(tn -> tn.getId().equals(node.getId())))
                .collect(toList());
        removeNextHearingObject(publishedForNowsNodesNotInRollup);
        treeNodes.addAll(publishedForNowsNodesNotInRollup);
        return treeNodes;
    }

    /**
     * If the results is publish for NOWs then no nextHearing object should be set
     *
     * @param treeNodes
     */
    private void removeNextHearingObject(List<TreeNode<ResultLine>> treeNodes) {
        treeNodes.stream().filter(treeNode -> nonNull(treeNode.getJudicialResult().getNextHearing())).forEach(node -> node.getJudicialResult().setNextHearing(null));
    }

    private void updateResultText(final List<TreeNode<ResultLine>> treeNodeList) {
        treeNodeList.forEach(treeNode -> {
            if (nonNull(treeNode.getJudicialResult()) && isNotEmpty(treeNode.getJudicialResult().getJudicialResultPrompts())) {
                final String sortedPrompts = treeNode.getJudicialResult().getJudicialResultPrompts()
                        .stream()
                        .filter(JUDICIAL_RESULT_PROMPT_PREDICATE)
                        .map(p -> format("%s %s", p.getLabel(), p.getValue()))
                        .collect(joining(lineSeparator()));

                final String resultText = ResultTextHelper.getResultText(treeNode.getJudicialResult().getLabel(), sortedPrompts);

                treeNode.getJudicialResult().setResultText(resultText);
            }
        });
    }

    private List<TreeNode<ResultLine>> updateResultTextWithNewLogic(final List<TreeNode<ResultLine>> treeNodeList) {
        ResultTextHelper.setResultText(treeNodeList, resultTextConfHelper);
        return treeNodeList;
    }
}
