package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.FALSE;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AlwaysPublishHelper.processAlwaysPublishResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DeDupeNextHearingHelper.deDupNextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DurationElementHelper.setDurationElements;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ExcludeResultsHelper.removeExcludedResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.processPublishAsPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishedForNowsHelper.getNodesWithPublishedForNows;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNonPublishableLinesHelper.removeNonPublishableResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructureNextHearingHelper.restructureNextHearing;
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

    @Inject
    public RestructuringHelper(final ResultTreeBuilder resultTreeBuilder) {
        this.resultTreeBuilder = resultTreeBuilder;
    }

    public List<TreeNode<ResultLine>> restructure(final JsonEnvelope context, final ResultsShared resultsShared) {
        final List<TreeNode<ResultLine>> treeNodes = resultTreeBuilder.build(context, resultsShared);

        final List<TreeNode<ResultLine>> publishedForNowsNodes = getNodesWithPublishedForNows(treeNodes);

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

        removeNonPublishableResults(
                restructureNextHearing(
                        processAlwaysPublishResults(
                                deDupNextHearing(
                                        filterNodesWithRollUpPrompts(
                                                processPublishAsPrompt(
                                                        removeExcludedResults(
                                                                updateResultText(treeNodes)
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
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

    private List<TreeNode<ResultLine>> updateResultText(final List<TreeNode<ResultLine>> treeNodeList) {
        ResultTextHelper.setResultText(treeNodeList);
        return treeNodeList;
    }
}
