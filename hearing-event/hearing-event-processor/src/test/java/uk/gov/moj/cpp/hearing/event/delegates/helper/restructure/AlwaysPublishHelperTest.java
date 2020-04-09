package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;

public class AlwaysPublishHelperTest {

    @Test
    public void treeWithTwoAlwaysPublishedLeafNodesShouldProduceThreeStandaloneNodes(){
        final List<TreeNode<ResultLine>> tree = new ArrayList<>();
        final TreeNode<ResultLine> treeNode1 = createTreeNode(false, 1);
        tree.add(treeNode1);

        final TreeNode<ResultLine> treeNode2 = createTreeNode(true, 4);
        tree.add(treeNode2);
        treeNode1.addChild(treeNode2);
        treeNode2.addParent(treeNode1);

        final TreeNode<ResultLine> treeNode3 = createTreeNode(true, 3);
        tree.add(treeNode3);
        treeNode1.addChild(treeNode3);
        treeNode3.addParent(treeNode1);

        final List<TreeNode<ResultLine>> restructuredTree = AlwaysPublishHelper.processAlwaysPublishResults(tree);
        final List<TreeNode<ResultLine>> distinctNodes = restructuredTree.stream().filter(node -> node.getChildren().size() == 0 && node.getParents().size() == 0).collect(Collectors.toList());
        assertThat(distinctNodes.size(), is(3));
    }

    @Test
    public void treeWithOneAlwaysPublishedLeafNodesShouldProduceOneStandaloneNode(){
        final List<TreeNode<ResultLine>> tree = new ArrayList<>();
        final TreeNode<ResultLine> treeNode1 = createTreeNode(false, 1);
        tree.add(treeNode1);

        final TreeNode<ResultLine> treeNode2 = createTreeNode(false,3);
        tree.add(treeNode2);
        treeNode1.addChild(treeNode2);
        treeNode2.addParent(treeNode1);

        final TreeNode<ResultLine> treeNode3 = createTreeNode(true, 2);
        tree.add(treeNode3);
        treeNode1.addChild(treeNode3);
        treeNode3.addParent(treeNode1);

        final List<TreeNode<ResultLine>> restructuredTree = AlwaysPublishHelper.processAlwaysPublishResults(tree);
        final List<TreeNode<ResultLine>> distinctNodes = restructuredTree.stream().filter(node -> node.getChildren().size() == 0 && node.getParents().size() == 0).collect(Collectors.toList());
        assertThat(distinctNodes.size(), is(1));
    }

    private TreeNode<ResultLine> createTreeNode(final boolean alwaysPublished, final Integer rank){
        final TreeNode<ResultLine> treeNode = new TreeNode(UUID.randomUUID(), null);
        final ResultDefinition resultDefinition = new ResultDefinition();
        resultDefinition.setAlwaysPublished(alwaysPublished);
        resultDefinition.setRank(rank);
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode(UUID.randomUUID(), resultDefinition);
        treeNode.setResultDefinition(resultDefinitionTreeNode);
        return treeNode;
    }
}
