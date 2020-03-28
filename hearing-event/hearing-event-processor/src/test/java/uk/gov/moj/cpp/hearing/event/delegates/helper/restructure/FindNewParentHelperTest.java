package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.FindNewParentHelper.findNewParent;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.Optional;

import org.junit.Test;

public class FindNewParentHelperTest {
    @Test
    public void shouldFindTheFirstAncestorThatIsNotExcludedAndNotPublishAsPromptParent() {
        final TreeNode<ResultLine> topLevelResultLineTreeNode = createResultLineTreeNode(false, false, 100);
        final TreeNode<ResultLine> firstLevelChildResultLineTreeNode = createResultLineTreeNode(false, false, 200);
        final TreeNode<ResultLine> secondLevelChildResultLineTreeNode = createResultLineTreeNode(true, false, 300);
        final TreeNode<ResultLine> thirdLevelChildResultLineTreeNode = createResultLineTreeNode(false, true, 400);
        final TreeNode<ResultLine> fourthLevelChildResultLineTreeNode = createResultLineTreeNode(false, true, 500);

        fourthLevelChildResultLineTreeNode.addParent(thirdLevelChildResultLineTreeNode);
        thirdLevelChildResultLineTreeNode.addParent(secondLevelChildResultLineTreeNode);
        secondLevelChildResultLineTreeNode.addParent(firstLevelChildResultLineTreeNode);
        firstLevelChildResultLineTreeNode.addParent(topLevelResultLineTreeNode);

        final Optional<TreeNode<ResultLine>> optionalNewParent = findNewParent(fourthLevelChildResultLineTreeNode);
        assertThat(optionalNewParent.isPresent(), is(true));
        assertThat(optionalNewParent.get(), is(firstLevelChildResultLineTreeNode));
    }

    @Test
    public void shouldReturnOptionalEmptyIfPerantCannotBeFound() {
        final TreeNode<ResultLine> topLevelResultLineTreeNode = createResultLineTreeNode(true, false, 100);
        final TreeNode<ResultLine> firstLevelChildResultLineTreeNode = createResultLineTreeNode(false, true, 200);
        final TreeNode<ResultLine> secondLevelChildResultLineTreeNode = createResultLineTreeNode(false, true, 300);

        secondLevelChildResultLineTreeNode.addParent(firstLevelChildResultLineTreeNode);
        firstLevelChildResultLineTreeNode.addParent(topLevelResultLineTreeNode);

        final Optional<TreeNode<ResultLine>> optionalNewParent = findNewParent(secondLevelChildResultLineTreeNode);
        assertThat(optionalNewParent, is(Optional.empty()));
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