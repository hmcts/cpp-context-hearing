package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.CROWN_COURT_RESULT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.MAGISTRATE_RESULT_DEFINITION_ID;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RestructureNextHearingHelperV3 {

    private RestructureNextHearingHelperV3() {
    }

    public static List<TreeNode<ResultLine2>> getNextHearingResult(final List<TreeNode<ResultLine2>> treeNodeList) {
        final List<UUID> nextHearingIds = asList(fromString(CROWN_COURT_RESULT_DEFINITION_ID), fromString(MAGISTRATE_RESULT_DEFINITION_ID));
        return treeNodeList.stream().filter(node -> node.isLeaf() && !node.isStandalone())
                .filter(treeNode -> nextHearingIds.contains(treeNode.getResultDefinitionId()))
                .sorted(comparing(node -> node.getResultDefinition().getData().getRank()))
                .collect(toList());
    }

    public static List<TreeNode<ResultLine2>> restructureNextHearing(final List<TreeNode<ResultLine2>> treeNodeList) {
        final List<TreeNode<ResultLine2>> candidateNodeForRemoval = new ArrayList<>();
        getNextHearingResult(treeNodeList).forEach(nextHearingTreeNode -> {
                    final Optional<TreeNode<ResultLine2>> grandParent = processParent(nextHearingTreeNode.getParents(), candidateNodeForRemoval);
                    if (grandParent.isPresent()) {
                        nextHearingTreeNode.removeAllParents();
                        grandParent.get().addChild(nextHearingTreeNode);
                        nextHearingTreeNode.addParent(grandParent.get());
                    }
                }
        );

        treeNodeList.removeAll(candidateNodeForRemoval);
        return treeNodeList;
    }

    public static Optional<TreeNode<ResultLine2>> processParent(final List<TreeNode<ResultLine2>> parentList, final List<TreeNode<ResultLine2>> candidateNodeForRemoval) {

        if (isEmpty(parentList)) {
            return empty();
        }

        for (final TreeNode<ResultLine2> parent : parentList) {
            final Boolean publishedAsAPrompt = parent.getResultDefinition().getData().getPublishedAsAPrompt();
            final Boolean excludedFromResults = parent.getResultDefinition().getData().getExcludedFromResults();

            if (!publishedAsAPrompt && !excludedFromResults) {
                return ofNullable(parent);
            }
            candidateNodeForRemoval.add(parent);
            processParent(parent.getParents(), candidateNodeForRemoval);
        }

        return empty();

    }
}
