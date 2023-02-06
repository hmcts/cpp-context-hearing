package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AlwaysPublishHelperV3.processAlwaysPublishResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DeDupeNextHearingHelperV3.deDupNextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DurationElementHelperV3.setDurationElements;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ExcludeResultsHelperV3.removeExcludedResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelperV3.processPublishAsPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishedForNowsHelperV3.getNodesWithPublishedForNows;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNonPublishableLinesHelperV3.removeNonPublishableResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructureNextHearingHelperV3.restructureNextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RollUpPromptsHelperV3.filterNodesWithRollUpPrompts;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.EXCLUDED_PROMPT_REFERENCE;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultTextHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultTextHelperV3;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

public class RestructuringHelperV3 {

    public static final Predicate<JudicialResultPrompt> JUDICIAL_RESULT_PROMPT_PREDICATE = p -> !EXCLUDED_PROMPT_REFERENCE.equals(p.getPromptReference());

    private final ResultTreeBuilderV3 resultTreeBuilder;
    private final ResultTextConfHelper resultTextConfHelper;

    @Inject
    public RestructuringHelperV3(final ResultTreeBuilderV3 resultTreeBuilder, final ResultTextConfHelper resultTextConfHelper) {
        this.resultTreeBuilder = resultTreeBuilder;
        this.resultTextConfHelper = resultTextConfHelper;
    }


    public List<TreeNode<ResultLine2>> restructure(final JsonEnvelope context, final ResultsSharedV3 resultsShared) {
        final List<TreeNode<ResultLine2>> treeNodes = resultTreeBuilder.build(context, resultsShared);

        final List<TreeNode<ResultLine2>> publishedForNowsNodes = getNodesWithPublishedForNows(treeNodes);

        if(resultTextConfHelper.isOldResultDefinitionV2(treeNodes)){
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
        final List<TreeNode<ResultLine2>> publishedForNowsNodesNotInRollup = publishedForNowsNodes.stream()
                .filter(node -> treeNodes.stream().noneMatch(tn -> tn.getId().equals(node.getId())))
                .collect(toList());
        removeNextHearingObject(publishedForNowsNodesNotInRollup);
        treeNodes.addAll(publishedForNowsNodesNotInRollup);
        return treeNodes;
    }

    /**
     * If the results is publish for NOWs then no nextHearing object should be set
     * @param treeNodes
     */
    private void removeNextHearingObject(List<TreeNode<ResultLine2>> treeNodes) {
        treeNodes.stream().filter(treeNode -> nonNull(treeNode.getJudicialResult().getNextHearing())).forEach(node -> node.getJudicialResult().setNextHearing(null));
    }

    private void updateResultText(final List<TreeNode<ResultLine2>> treeNodeList) {
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

    private  List<TreeNode<ResultLine2>>  updateResultTextWithNewLogic(final List<TreeNode<ResultLine2>> treeNodeList) {

        ResultTextHelperV3.setResultText(treeNodeList, resultTextConfHelper);
        return treeNodeList;
    }
}
