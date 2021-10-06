package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.LeafNodeHelperV3.isValidTreeNode;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;

public class FindNewParentHelperV3 {

    private FindNewParentHelperV3() {
    }

    public static Optional<TreeNode<ResultLine2>> findNewParent(final TreeNode<ResultLine2> resultLineTreeNode) {
        final List<TreeNode<ResultLine2>> parents = resultLineTreeNode.getParents();
        for (final TreeNode<ResultLine2> parent : parents) {
            if (!isPublishAsPrompt(parent) && isNotExcludedFromResults(parent)) {
                return of(parent);
            } else {
                return findNewParent(parent);
            }
        }
        return empty();
    }

    private static boolean isPublishAsPrompt(final TreeNode<ResultLine2> resultLineTreeNode) {
        return isValidTreeNode(resultLineTreeNode) && Boolean.TRUE.equals(resultLineTreeNode.getResultDefinition().getData().getPublishedAsAPrompt());
    }

    private static boolean isNotExcludedFromResults(final TreeNode<ResultLine2> resultLineTreeNode) {
        return isValidTreeNode(resultLineTreeNode) && !Boolean.TRUE.equals(resultLineTreeNode.getResultDefinition().getData().getExcludedFromResults());
    }
}
