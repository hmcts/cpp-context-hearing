package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;

public class LeafNodeHelper {
    private LeafNodeHelper() {

    }

    public static Optional<TreeNode<ResultLine>> getLowestRankedLeafNodeWithIsPublishAsPromptFlag(final List<TreeNode<ResultLine>> results) {
        return results
                .stream()
                .filter(TreeNode::isLeaf)
                .filter(LeafNodeHelper::isPublishAsPrompt)
                .min(comparing(r -> r.getResultDefinition().getData().getRank()));
    }

    private static boolean isPublishAsPrompt(final TreeNode<ResultLine> resultLineTreeNode) {
        return isValidTreeNode(resultLineTreeNode) && Boolean.TRUE.equals(resultLineTreeNode.getResultDefinition().getData().getPublishedAsAPrompt());
    }

    public static boolean isValidTreeNode(final TreeNode<ResultLine> resultLineTreeNode) {
        return nonNull(resultLineTreeNode) && nonNull(resultLineTreeNode.getResultDefinition()) && nonNull(resultLineTreeNode.getResultDefinition().getData());
    }
}
