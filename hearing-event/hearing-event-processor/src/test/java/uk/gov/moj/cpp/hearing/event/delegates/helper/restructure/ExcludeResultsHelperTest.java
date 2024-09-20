package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.core.courts.ResultLine.resultLine;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ExcludeResultsHelper.removeExcludedResults;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ExcludeResultsHelperTest {

    @Test
    public void shouldRemoveExcludedResults() {

        final TreeNode<ResultLine> topLevelResultLineTreeNode = createResultLineTreeNode(false);

        final TreeNode<ResultLine> firstChildResultLineTreeNode = createResultLineTreeNode(true);

        final TreeNode<ResultLine> secondChildResultLineTreeNode = createResultLineTreeNode(true);

        final TreeNode<ResultLine> thirdChildResultLineTreeNode = createResultLineTreeNode(false);

        topLevelResultLineTreeNode.addChild(firstChildResultLineTreeNode);
        firstChildResultLineTreeNode.addParent(topLevelResultLineTreeNode);
        firstChildResultLineTreeNode.addChild(secondChildResultLineTreeNode);
        secondChildResultLineTreeNode.addParent(firstChildResultLineTreeNode);
        secondChildResultLineTreeNode.addChild(thirdChildResultLineTreeNode);
        thirdChildResultLineTreeNode.addParent(secondChildResultLineTreeNode);

        final List<TreeNode<ResultLine>> resultLines = new ArrayList<>(asList(topLevelResultLineTreeNode, firstChildResultLineTreeNode, secondChildResultLineTreeNode, thirdChildResultLineTreeNode));

        removeExcludedResults(resultLines);

        assertThat(resultLines.size(), is(2));
        
        final List<TreeNode<ResultLine>> collect = resultLines
                .stream()
                .filter(r -> r.equals(topLevelResultLineTreeNode))
                .map(r -> r.getChildren())
                .flatMap(Collection::stream)
                .collect(toList());

        assertThat(collect.size(), is(1));
        assertThat(collect.get(0), is(thirdChildResultLineTreeNode));


        final List<TreeNode<ResultLine>> collect2 = resultLines
                .stream()
                .filter(r -> r.equals(thirdChildResultLineTreeNode))
                .map(TreeNode::getParents)
                .flatMap(Collection::stream)
                .collect(toList());

        assertThat(collect2.size(), is(1));
        assertThat(collect2.get(0), is(topLevelResultLineTreeNode));
    }

    @Test
    public void shouldRemoveExcludedResults2() {


        final TreeNode<ResultLine> topLevelResultLineTreeNode = createResultLineTreeNode(false);

        final TreeNode<ResultLine> firstChildResultLineTreeNode = createResultLineTreeNode(false);


        final TreeNode<ResultLine> secondChildResultLineTreeNode = createResultLineTreeNode(true);


        final TreeNode<ResultLine> thirdChildResultLineTreeNode = createResultLineTreeNode(false);


        final TreeNode<ResultLine> fourthChildResultLineTreeNode = createResultLineTreeNode(false);

        
        topLevelResultLineTreeNode.addChild(firstChildResultLineTreeNode);
        topLevelResultLineTreeNode.addChild(secondChildResultLineTreeNode);
        firstChildResultLineTreeNode.addParent(topLevelResultLineTreeNode);
        secondChildResultLineTreeNode.addParent(topLevelResultLineTreeNode);
        secondChildResultLineTreeNode.addChild(thirdChildResultLineTreeNode);
        secondChildResultLineTreeNode.addChild(fourthChildResultLineTreeNode);
        thirdChildResultLineTreeNode.addParent(secondChildResultLineTreeNode);
        fourthChildResultLineTreeNode.addParent(secondChildResultLineTreeNode);
        
        final List<TreeNode<ResultLine>> resultLines = new ArrayList<>(asList(topLevelResultLineTreeNode, firstChildResultLineTreeNode, secondChildResultLineTreeNode, thirdChildResultLineTreeNode, fourthChildResultLineTreeNode));

        removeExcludedResults(resultLines);

        assertThat(resultLines.size(), is(4));
        
        final List<TreeNode<ResultLine>> collect = resultLines
                .stream()
                .filter(r -> r.equals(topLevelResultLineTreeNode))
                .map(r -> r.getChildren())
                .flatMap(Collection::stream)
                .collect(toList());

        assertThat(collect.size(), is(3));
        assertTrue(collect.contains(firstChildResultLineTreeNode));
        assertTrue(collect.contains(thirdChildResultLineTreeNode));
        assertTrue(collect.contains(fourthChildResultLineTreeNode));


        final List<TreeNode<ResultLine>> collect2 = resultLines
                .stream()
                .filter(r -> r.equals(thirdChildResultLineTreeNode))
                .map(TreeNode::getParents)
                .flatMap(Collection::stream)
                .collect(toList());

        assertThat(collect2.size(), is(1));
        assertThat(collect2.get(0), is(topLevelResultLineTreeNode));
    }

    private TreeNode<ResultDefinition> createResultDefinitionTreeNode(final boolean excludedFromResults) {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setExcludedFromResults(excludedFromResults);
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode<>(randomUUID(), resultDefinition);
        return resultDefinitionTreeNode;
    }

    private TreeNode<ResultLine> createResultLineTreeNode(final boolean excludedFromResults) {
        TreeNode<ResultLine> resultLineTreeNode = new TreeNode<>(randomUUID(), resultLine().build());
        final TreeNode<ResultDefinition> resultDefinition = createResultDefinitionTreeNode(excludedFromResults);
        resultLineTreeNode.setResultDefinition(resultDefinition);
        return resultLineTreeNode;
    }
}