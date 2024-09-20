package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class RemoveNonPublishableLinesHelperTest {

    @Test
    public void treeWithTwoLeafNodesShouldProduceOneStandaloneNode() {
        final List<TreeNode<ResultLine>> tree = new ArrayList<>();
        final TreeNode<ResultLine> treeNode1 = createTreeNode(false, false, 1, true, "1", "2");
        tree.add(treeNode1);

        final TreeNode<ResultLine> treeNode2 = createTreeNode(true, true, 4, false, "4");
        tree.add(treeNode2);
        treeNode1.addChild(treeNode2);
        treeNode2.addParent(treeNode1);

        final TreeNode<ResultLine> treeNode3 = createTreeNode(true, true, 3, false, "3");
        tree.add(treeNode3);
        treeNode1.addChild(treeNode3);
        treeNode3.addParent(treeNode1);

        final List<TreeNode<ResultLine>> restructuredTree = RemoveNonPublishableLinesHelper.removeNonPublishableResults(tree);
        final List<TreeNode<ResultLine>> standaloneNodes = restructuredTree.stream().filter(node -> node.getChildren().size() == 0 && node.getParents().size() == 0).collect(Collectors.toList());
        assertThat(standaloneNodes.size(), is(1));
        assertThat(standaloneNodes.get(0).getJudicialResult().getNextHearing(), is(notNullValue()));
        final List<JudicialResultPrompt> judicialResultPrompts = standaloneNodes.get(0).getJudicialResult().getJudicialResultPrompts();
        assertThat(judicialResultPrompts.size(), is(4));
        assertThat(judicialResultPrompts.get(0).getLabel(), is("1"));
        assertThat(judicialResultPrompts.get(1).getLabel(), is("2"));
        assertThat(judicialResultPrompts.get(2).getLabel(), is("3"));
        assertThat(judicialResultPrompts.get(3).getLabel(), is("4"));
    }

    @Test
    public void treeWithOneLeafNodeHasNextHearingShouldProduceOneStandaloneNodeWithNextHearing() {
        final List<TreeNode<ResultLine>> tree = new ArrayList<>();
        final TreeNode<ResultLine> treeNode1 = createTreeNode(false, false, 1, false, "1", "2");
        tree.add(treeNode1);

        final TreeNode<ResultLine> treeNode2 = createTreeNode(true, true, 4, true, "4");
        tree.add(treeNode2);
        treeNode1.addChild(treeNode2);
        treeNode2.addParent(treeNode1);

        final TreeNode<ResultLine> treeNode3 = createTreeNode(true, true, 3, false, "3");
        tree.add(treeNode3);
        treeNode1.addChild(treeNode3);
        treeNode3.addParent(treeNode1);

        final List<TreeNode<ResultLine>> restructuredTree = RemoveNonPublishableLinesHelper.removeNonPublishableResults(tree);
        final List<TreeNode<ResultLine>> standaloneNodes = restructuredTree.stream().filter(node -> node.getChildren().size() == 0 && node.getParents().size() == 0).collect(Collectors.toList());
        assertThat(standaloneNodes.size(), is(1));
        assertThat(standaloneNodes.get(0).getJudicialResult().getNextHearing(), is(notNullValue()));
        final List<JudicialResultPrompt> judicialResultPrompts = standaloneNodes.get(0).getJudicialResult().getJudicialResultPrompts();
        assertThat(judicialResultPrompts.size(), is(4));
        assertThat(judicialResultPrompts.get(0).getLabel(), is("1"));
        assertThat(judicialResultPrompts.get(1).getLabel(), is("2"));
        assertThat(judicialResultPrompts.get(2).getLabel(), is("3"));
        assertThat(judicialResultPrompts.get(3).getLabel(), is("4"));
    }


    @Test
    public void treeWithOneLeafNodeShouldProduceOneNodeTreeStructure() {
        final List<TreeNode<ResultLine>> tree = new ArrayList<>();
        final TreeNode<ResultLine> treeNode1 = createTreeNode(false, false, 1, false, "1");
        tree.add(treeNode1);

        final TreeNode<ResultLine> treeNode2 = createTreeNode(false, false, 4, false, "4");
        tree.add(treeNode2);
        treeNode1.addChild(treeNode2);
        treeNode2.addParent(treeNode1);

        final TreeNode<ResultLine> treeNode3 = createTreeNode(true, true, 3, false, "3");
        tree.add(treeNode3);
        treeNode2.addChild(treeNode3);
        treeNode3.addParent(treeNode2);

        final List<TreeNode<ResultLine>> restructuredTree = RemoveNonPublishableLinesHelper.removeNonPublishableResults(tree);
        final List<TreeNode<ResultLine>> parentNodes = restructuredTree.stream().filter(node -> node.getChildren().isEmpty() && node.getParents().isEmpty()).collect(Collectors.toList());
        assertThat(parentNodes.size(), is(1));

        final List<TreeNode<ResultLine>> leafNodes = restructuredTree.stream().filter(TreeNode::isLeaf).collect(Collectors.toList());
        assertThat(leafNodes.size(), is(0));

        final List<JudicialResultPrompt> judicialResultPrompts = parentNodes.get(0).getJudicialResult().getJudicialResultPrompts();
        assertThat(judicialResultPrompts.size(), is(3));
        assertThat(judicialResultPrompts.get(0).getLabel(), is("1"));
        assertThat(judicialResultPrompts.get(1).getLabel(), is("3"));
        assertThat(judicialResultPrompts.get(2).getLabel(), is("4"));

        final JudicialResult judicialResult = parentNodes.get(0).getJudicialResult();
        assertThat(judicialResult.getNextHearing(), is(nullValue()));
    }

    @Test
    public void treeWithTwoLeafNodes_one_excludedFromResult_and_other_not_ShouldProduceOneStandaloneNode() {
        final List<TreeNode<ResultLine>> tree = new ArrayList<>();
        final TreeNode<ResultLine> treeNode1 = createTreeNode(false, false, 1, false, "1");
        tree.add(treeNode1);

        final TreeNode<ResultLine> treeNode2 = createTreeNode(false, true, 3, false, "3");
        tree.add(treeNode2);
        treeNode1.addChild(treeNode2);
        treeNode2.addParent(treeNode1);

        final TreeNode<ResultLine> treeNode3 = createTreeNode(true, true, 2, false, "2");
        tree.add(treeNode3);
        treeNode1.addChild(treeNode3);
        treeNode3.addParent(treeNode1);

        final List<TreeNode<ResultLine>> restructuredTree = RemoveNonPublishableLinesHelper.removeNonPublishableResults(tree);
        final List<TreeNode<ResultLine>> standaloneNodes = restructuredTree.stream().filter(node -> node.getChildren().size() == 0 && node.getParents().size() == 0).collect(Collectors.toList());
        assertThat(standaloneNodes.size(), is(1));

        final List<JudicialResultPrompt> judicialResultPrompts = standaloneNodes.get(0).getJudicialResult().getJudicialResultPrompts();
        assertThat(judicialResultPrompts.size(), is(3));
        assertThat(judicialResultPrompts.get(0).getLabel(), is("1"));
        assertThat(judicialResultPrompts.get(1).getLabel(), is("2"));
        assertThat(judicialResultPrompts.get(2).getLabel(), is("3"));
    }

    private TreeNode<ResultLine> createTreeNode(final boolean publishedAsAPrompt, final boolean excludedFromResults, final Integer rank, final boolean hasNextHearing, final String... promptLabels) {
        final TreeNode<ResultLine> treeNode = new TreeNode<>(UUID.randomUUID(), null);
        final ResultDefinition resultDefinition = new ResultDefinition();
        resultDefinition.setPublishedAsAPrompt(publishedAsAPrompt);
        resultDefinition.setExcludedFromResults(excludedFromResults);
        resultDefinition.setRank(rank);
        final JudicialResult judicialResult = createJudicialResult(promptLabels);
        if (hasNextHearing) {
            judicialResult.setNextHearing(NextHearing.nextHearing().build());
        }
        treeNode.setJudicialResult(judicialResult);
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode<>(UUID.randomUUID(), resultDefinition);
        treeNode.setResultDefinition(resultDefinitionTreeNode);
        return treeNode;
    }

    private JudicialResult createJudicialResult(final String... promptLabels) {
        List<JudicialResultPrompt> prompts = new ArrayList<>();
        for (String promptLabel : promptLabels) {
            prompts.add(JudicialResultPrompt.judicialResultPrompt().withLabel(promptLabel).build());
        }

        return new JudicialResult.Builder().withJudicialResultPrompts(prompts).build();
    }


}
