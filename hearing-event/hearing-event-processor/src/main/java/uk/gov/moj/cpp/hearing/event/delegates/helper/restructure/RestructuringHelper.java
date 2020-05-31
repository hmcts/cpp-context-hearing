package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultTextHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import javax.inject.Inject;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AlwaysPublishHelper.processAlwaysPublishResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DeDupeNextHearingHelper.deDupNextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.DurationElementHelper.setDurationElements;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ExcludeResultsHelper.removeExcludedResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.processPublishAsPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNonPublishableLinesHelper.removeNonPublishableResults;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructureNextHearingHelper.restructureNextHearing;

public class RestructuringHelper {
    private final ResultTreeBuilder resultTreeBuilder;

    @Inject
    public RestructuringHelper(final ResultTreeBuilder resultTreeBuilder) {
        this.resultTreeBuilder = resultTreeBuilder;
    }

    public List<TreeNode<ResultLine>> restructure(final JsonEnvelope envelope, final ResultsShared resultsShared) {
        final List<TreeNode<ResultLine>> treeNodes = resultTreeBuilder.build(envelope, resultsShared);
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

    private List<TreeNode<ResultLine>> updateResultText(final List<TreeNode<ResultLine>> treeNodeList) {
        treeNodeList.forEach(treeNode -> {
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
}
