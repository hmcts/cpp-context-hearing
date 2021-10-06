package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class RemoveNodeFromTreeHelperV3 {

    private RemoveNodeFromTreeHelperV3() {
    }

    static void remove(final TreeNode<ResultLine2> resultLineTreeNode, final List<TreeNode<ResultLine2>> results) {
        final List<TreeNode<ResultLine2>> parents = resultLineTreeNode.getParents();
        parents.forEach(p -> p.getChildren().remove(resultLineTreeNode));
        parents.forEach(p -> p.getChildren().addAll(resultLineTreeNode.getChildren()));

        resultLineTreeNode.getChildren().forEach(c -> c.getParents().remove(resultLineTreeNode));
        resultLineTreeNode.getChildren().forEach(c -> c.getParents().addAll(resultLineTreeNode.getParents()));

        if (parents.isEmpty()) {
            resultLineTreeNode.getChildren().forEach(c -> c.setDefendantId(resultLineTreeNode.getDefendantId()));
            resultLineTreeNode.getChildren().forEach(c -> c.setOffenceId(resultLineTreeNode.getOffenceId()));
        }

        results.remove(resultLineTreeNode);
    }
}
