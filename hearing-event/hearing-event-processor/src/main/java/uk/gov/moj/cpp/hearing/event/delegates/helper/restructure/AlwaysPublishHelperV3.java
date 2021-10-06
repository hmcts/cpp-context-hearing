package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;

public class AlwaysPublishHelperV3 {

    private AlwaysPublishHelperV3(){
    }

    public static List<TreeNode<ResultLine2>> processAlwaysPublishResults(final List<TreeNode<ResultLine2>> treeNodes) {

        Optional<TreeNode<ResultLine2>> alwaysPublishLeaNode;

        while ((alwaysPublishLeaNode = getAlwaysPublishLeafNode(treeNodes)).isPresent()) {
            final TreeNode<ResultLine2> node = alwaysPublishLeaNode.get();
            node.removeAllChildren();
            node.getParents().stream().forEach(parent -> parent.getChildren().remove(node));
            node.removeAllParents();
        }
        return treeNodes;
    }

    private static Optional<TreeNode<ResultLine2>> getAlwaysPublishLeafNode(final List<TreeNode<ResultLine2>> treeNodes) {
        return treeNodes.stream()
                .filter(LeafNodeHelperV3::isValidTreeNode)
                .filter(node -> nonNull(node.getResultDefinition().getData().getAlwaysPublished()))
                .filter(node -> node.getResultDefinition().getData().getAlwaysPublished() && node.isLeaf())
                .sorted(comparing(treeNode -> treeNode.getResultDefinition().getData().getRank()))
                .findFirst();
    }
}
