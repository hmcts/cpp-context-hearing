package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNodeFromTreeHelper.remove;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class RollUpPromptsHelper {
    private RollUpPromptsHelper() {}

    public static List<TreeNode<ResultLine>> filterNodesWithRollUpPrompts(final List<TreeNode<ResultLine>> results) {
        results
                .stream()
                .filter(LeafNodeHelper::isValidTreeNode)
                .filter(node -> !(TRUE.equals(node.getResultDefinition().getData().getRollUpPrompts())
                        || TRUE.equals(node.getResultDefinition().getData().getPublishedAsAPrompt())
                        || TRUE.equals(node.getResultDefinition().getData().getAlwaysPublished())))
                .collect(toList())
                .forEach(node -> {
                    if(!node.getChildren().isEmpty() && !node.getParents().isEmpty()) {
                        remove(node, results);
                    }
                });
        return results;
    }
}
