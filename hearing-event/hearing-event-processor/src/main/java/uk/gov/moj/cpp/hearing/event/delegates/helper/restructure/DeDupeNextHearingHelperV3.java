package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructureNextHearingHelperV3.getNextHearingResult;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

public class DeDupeNextHearingHelperV3 {

    private DeDupeNextHearingHelperV3(){

    }

    public static List<TreeNode<ResultLine2>> deDupNextHearing(final List<TreeNode<ResultLine2>> treeNodeList) {
        getNextHearingResult(treeNodeList).stream().forEach(nextHearingTreeNode -> {
            final List<TreeNode<ResultLine2>> parentTreeNodes = nextHearingTreeNode.getParents();
            if(isNotEmpty(parentTreeNodes)) {
                for (final TreeNode<ResultLine2> parentTreeNode : parentTreeNodes) {
                    final List<TreeNode<ResultLine2>> childTreeNodeList = parentTreeNode.getChildren();
                    if(isNotEmpty(childTreeNodeList)) {
                        final List<TreeNode<ResultLine2>> childrenToRemove =  childTreeNodeList.stream()
                                .filter(treeNode -> treeNode.getJudicialResult() != null && treeNode.getJudicialResult().getNextHearing() != null)
                                .filter(treeNode -> treeNode.getJudicialResult().getNextHearing().getListedStartDateTime() != null)
                                .sorted(comparing(treeNode -> treeNode.getJudicialResult().getNextHearing().getListedStartDateTime()))
                                .skip(1)
                                .map(child -> {
                                    child.removeAllParents();
                                    return child;
                                }).collect(toList());
                        childTreeNodeList.removeAll(childrenToRemove);
                    }
                }
            }
        });
        return treeNodeList;
    }
}
