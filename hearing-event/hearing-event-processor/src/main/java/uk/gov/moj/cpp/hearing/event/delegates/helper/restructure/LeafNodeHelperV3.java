package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;

public class LeafNodeHelperV3 {
    private LeafNodeHelperV3() {

    }

    public static Optional<TreeNode<ResultLine2>> getLowestRankedLeafNodeWithIsPublishAsPromptFlag(final List<TreeNode<ResultLine2>> results) {
        return results
                .stream()
                .filter(TreeNode::isLeaf)
                .filter(LeafNodeHelperV3::isPublishAsPrompt)
                .min(comparing(r -> r.getResultDefinition().getData().getRank()));
    }

    private static boolean isPublishAsPrompt(final TreeNode<ResultLine2> resultLineTreeNode) {
        return isValidTreeNode(resultLineTreeNode) && Boolean.TRUE.equals(resultLineTreeNode.getResultDefinition().getData().getPublishedAsAPrompt());
    }

    public static boolean isValidTreeNode(final TreeNode<ResultLine2> resultLineTreeNode) {
        return nonNull(resultLineTreeNode) && nonNull(resultLineTreeNode.getResultDefinition()) && nonNull(resultLineTreeNode.getResultDefinition().getData());
    }
}
