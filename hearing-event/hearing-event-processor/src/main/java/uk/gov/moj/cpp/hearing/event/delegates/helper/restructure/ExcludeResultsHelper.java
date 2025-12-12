package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.LeafNodeHelper.isValidTreeNode;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNodeFromTreeHelper.remove;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class ExcludeResultsHelper {

    private ExcludeResultsHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static List<TreeNode<ResultLine>> removeExcludedResults(final List<TreeNode<ResultLine>> results) {
        results
                .stream()
                .filter(LeafNodeHelper::isValidTreeNode)
                .filter(node -> TRUE.equals(node.getResultDefinition().getData().getExcludedFromResults()))
                .collect(toList())
                .forEach(node -> remove(node, results));

        return results;

    }

}
