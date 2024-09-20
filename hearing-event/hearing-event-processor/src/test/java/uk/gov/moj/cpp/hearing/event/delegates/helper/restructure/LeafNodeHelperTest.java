package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.LeafNodeHelper.getLowestRankedLeafNodeWithIsPublishAsPromptFlag;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class LeafNodeHelperTest {

    @Test
    public void shouldgetLowestRankedNextLeafNodeFromTree() {
        final TreeNode<ResultLine> parentResultLineTreeNode = createResultLineTreeNode(false, false, 100);
        final TreeNode<ResultLine> lowRankingChildResultLineTreeNode = createResultLineTreeNode(false, true, 200);
        final TreeNode<ResultLine> highRankingChildResultLineTreeNode = createResultLineTreeNode(false, true, 300);
        parentResultLineTreeNode.addChild(lowRankingChildResultLineTreeNode);
        parentResultLineTreeNode.addChild(highRankingChildResultLineTreeNode);
        lowRankingChildResultLineTreeNode.addParent(parentResultLineTreeNode);
        highRankingChildResultLineTreeNode.addParent(parentResultLineTreeNode);
        final List<TreeNode<ResultLine>> resultLines = new ArrayList<>(asList(parentResultLineTreeNode, lowRankingChildResultLineTreeNode, highRankingChildResultLineTreeNode));
        final Optional<TreeNode<ResultLine>> result = getLowestRankedLeafNodeWithIsPublishAsPromptFlag(resultLines);
        assertThat(result.get(), is(lowRankingChildResultLineTreeNode));
    }

    @Test
    public void shouldReturnOptionalEmptyIfNoLeafNodeExists() {
        final TreeNode<ResultLine> standaloneResultLineTreeNode = createResultLineTreeNode(false, true, 100);
        final List<TreeNode<ResultLine>> resultLines = new ArrayList<>(asList(standaloneResultLineTreeNode));
        final Optional<TreeNode<ResultLine>> result = getLowestRankedLeafNodeWithIsPublishAsPromptFlag(resultLines);
        assertThat(result, is(empty()));
    }

    @Test
    public void shouldReturnOptionalEmptyIfLeafNodeNotPublishedAsAPrompt() {
        final TreeNode<ResultLine> parentResultLineTreeNode = createResultLineTreeNode(false, false, 100);
        final TreeNode<ResultLine> childResultLineTreeNode = createResultLineTreeNode(false, false, 200);
        parentResultLineTreeNode.addChild(childResultLineTreeNode);
        childResultLineTreeNode.addParent(parentResultLineTreeNode);
        final List<TreeNode<ResultLine>> resultLines = new ArrayList<>(asList(parentResultLineTreeNode, childResultLineTreeNode));
        final Optional<TreeNode<ResultLine>> result = getLowestRankedLeafNodeWithIsPublishAsPromptFlag(resultLines);
        assertThat(result, is(empty()));
    }


    private TreeNode<ResultLine> createResultLineTreeNode(final boolean excludedFromResults, final boolean publishedAsAPrompt, final int rank) {
        final TreeNode<ResultLine> resultLineTreeNode = new TreeNode<>(randomUUID(), ResultLine.resultLine().build());
        final TreeNode<ResultDefinition> resultDefinition = createResultDefinitionTreeNode(excludedFromResults, publishedAsAPrompt, rank);
        resultLineTreeNode.setResultDefinition(resultDefinition);
        return resultLineTreeNode;
    }

    private TreeNode<ResultDefinition> createResultDefinitionTreeNode(final boolean excludedFromResults, final boolean publishedAsAPrompt, final int rank) {
        final ResultDefinition resultDefinition = resultDefinition()
                .setExcludedFromResults(excludedFromResults)
                .setPublishedAsAPrompt(publishedAsAPrompt)
                .setRank(rank);
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode<>(randomUUID(), resultDefinition);
        return resultDefinitionTreeNode;
    }
}