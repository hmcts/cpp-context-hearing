package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.ResultLine.resultLine;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class RemoveNodeFromTreeHelperTest {


    @Test
    public void shouldRemoveNodeFromTree() {
        final TreeNode<ResultLine> parentResultLineTreeNode = createResultLineTreeNode();
        final TreeNode<ResultLine> childResultLineTreeNode = createResultLineTreeNode();
        parentResultLineTreeNode.addChild(childResultLineTreeNode);
        childResultLineTreeNode.addParent(parentResultLineTreeNode);
        final List<TreeNode<ResultLine>> resultLines = new ArrayList<>(asList(parentResultLineTreeNode, childResultLineTreeNode));

        RemoveNodeFromTreeHelper.remove(childResultLineTreeNode, resultLines);
        assertThat(resultLines.size(), is(1));
        assertThat(resultLines.contains(childResultLineTreeNode), is(false));
        assertThat(resultLines.get(0).getChildren().isEmpty(), is(true));
    }

    private TreeNode<ResultDefinition> createResultDefinitionTreeNode() {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition();
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode<>(randomUUID(), resultDefinition);
        return resultDefinitionTreeNode;
    }

    private TreeNode<ResultLine> createResultLineTreeNode() {
        TreeNode<ResultLine> resultLineTreeNode = new TreeNode<>(randomUUID(), resultLine().build());
        final TreeNode<ResultDefinition> resultDefinition = createResultDefinitionTreeNode();
        resultLineTreeNode.setResultDefinition(resultDefinition);
        return resultLineTreeNode;
    }
}