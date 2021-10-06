package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNodeFromTreeHelperV3.remove;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class RollUpPromptsHelperV3 {
    private RollUpPromptsHelperV3() {}

    public static List<TreeNode<ResultLine2>> filterNodesWithRollUpPrompts(final List<TreeNode<ResultLine2>> results) {
        results
                .stream()
                .filter(LeafNodeHelperV3::isValidTreeNode)
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
