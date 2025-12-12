package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class PublishedForNowsHelperV3 {

    private PublishedForNowsHelperV3() {}

    public static List<TreeNode<ResultLine2>> getNodesWithPublishedForNows(final List<TreeNode<ResultLine2>> results) {
        return results
                .stream()
                .filter(LeafNodeHelperV3::isValidTreeNode)
                .filter(node -> TRUE.equals(node.getResultDefinition().getData().getPublishedForNows()))
                .collect(toList());
    }
}
