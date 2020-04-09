package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;

public class AlwaysPublishHelper {
    
    private AlwaysPublishHelper(){
    }

    public static List<TreeNode<ResultLine>> processAlwaysPublishResults(final List<TreeNode<ResultLine>> treeNodes) {

        Optional<TreeNode<ResultLine>> alwaysPublishLeaNode;

        while ((alwaysPublishLeaNode = getAlwaysPublishLeaNode(treeNodes)).isPresent()) {
            final TreeNode<ResultLine> node = alwaysPublishLeaNode.get();
            node.removeAllChildren();
            node.getParents().stream().forEach(parent -> parent.getChildren().remove(node));
            node.removeAllParents();
        }
        return treeNodes;
    }

    private static Optional<TreeNode<ResultLine>> getAlwaysPublishLeaNode(final List<TreeNode<ResultLine>> treeNodes) {
        return treeNodes.stream()
                .filter(LeafNodeHelper::isValidTreeNode)
                .filter(node -> nonNull(node.getResultDefinition().getData().getAlwaysPublished()))
                .filter(node -> node.getResultDefinition().getData().getAlwaysPublished() && node.isLeaf())
                .sorted(comparing(treeNode -> treeNode.getResultDefinition().getData().getRank()))
                .findFirst();
    }
}
