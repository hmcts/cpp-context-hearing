package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNodeFromTreeHelperV3.remove;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class ExcludeResultsHelperV3 {

    private ExcludeResultsHelperV3() {
        throw new IllegalStateException("Utility class");
    }

    public static List<TreeNode<ResultLine2>> removeExcludedResults(final List<TreeNode<ResultLine2>> results) {
        results
                .stream()
                .filter(LeafNodeHelperV3::isValidTreeNode)
                .filter(node -> TRUE.equals(node.getResultDefinition().getData().getExcludedFromResults()))
                .collect(toList())
                .forEach(node -> remove(node, results));

        return results;

    }

}
