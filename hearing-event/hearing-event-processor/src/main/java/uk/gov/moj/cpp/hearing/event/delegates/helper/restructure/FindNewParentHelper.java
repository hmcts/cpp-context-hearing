package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.LeafNodeHelper.isValidTreeNode;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;

public class FindNewParentHelper {

    private FindNewParentHelper() {
    }

    public static Optional<TreeNode<ResultLine>> findNewParent(final TreeNode<ResultLine> resultLineTreeNode) {
        final List<TreeNode<ResultLine>> parents = resultLineTreeNode.getParents();
        for (final TreeNode<ResultLine> parent : parents) {
            if (!isPublishAsPrompt(parent) && isNotExcludedFromResults(parent)) {
                return of(parent);
            } else {
                return findNewParent(parent);
            }
        }
        return empty();
    }

    private static boolean isPublishAsPrompt(final TreeNode<ResultLine> resultLineTreeNode) {
        return isValidTreeNode(resultLineTreeNode) && Boolean.TRUE.equals(resultLineTreeNode.getResultDefinition().getData().getPublishedAsAPrompt());
    }

    private static boolean isNotExcludedFromResults(final TreeNode<ResultLine> resultLineTreeNode) {
        return isValidTreeNode(resultLineTreeNode) && !Boolean.TRUE.equals(resultLineTreeNode.getResultDefinition().getData().getExcludedFromResults());
    }
}
