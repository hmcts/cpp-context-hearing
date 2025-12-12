package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class PublishedForNowsHelper {

    private PublishedForNowsHelper() {}

    public static List<TreeNode<ResultLine>> getNodesWithPublishedForNows(final List<TreeNode<ResultLine>> results) {
        return results
                .stream()
                .filter(LeafNodeHelper::isValidTreeNode)
                .filter(node -> TRUE.equals(node.getResultDefinition().getData().getPublishedForNows()))
                .collect(toList());
    }
}
