package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

public class RemoveNonPublishableLinesHelperV3 {

    private RemoveNonPublishableLinesHelperV3() {
    }

    public static List<TreeNode<ResultLine2>> removeNonPublishableResults(final List<TreeNode<ResultLine2>> treeNodeList) {

        final List<TreeNode<ResultLine2>> leafTreeNodes = treeNodeList.stream()
                .filter(node -> node.isLeaf() && !node.isStandalone())
                .sorted(comparing(node -> node.getResultDefinition().getData().getRank()))
                .collect(Collectors.toList());

        final List<TreeNode<ResultLine2>> intermediateLeafTreeNodes = treeNodeList.stream()
                                .filter(node -> node.isIntermediateLeaf() && !node.isStandalone())
                                .sorted(comparing(node -> node.getResultDefinition().getData().getRank()))
                                .collect(Collectors.toList());

        final List<TreeNode<ResultLine2>> treeNodes = concat(leafTreeNodes.stream(), intermediateLeafTreeNodes.stream()).collect(Collectors.toList());

        final List<TreeNode<ResultLine2>> candidateNodeForRemoval = new ArrayList<>();
        treeNodes.forEach(treeNode -> {
                    final List<Pair<Integer, List<JudicialResultPrompt>>> judicialResultPrompts = new ArrayList<>();
                    populatePrompts(treeNode, judicialResultPrompts);
                    final Optional<TreeNode<ResultLine2>> parent = processParent(treeNode, judicialResultPrompts, candidateNodeForRemoval);
                    if (parent.isPresent()) {
                        populatePrompts(parent.get(), judicialResultPrompts);
                        judicialResultPrompts.sort(comparing(promptPair -> promptPair.getKey().doubleValue()));
                        parent.get().getJudicialResult().setJudicialResultPrompts(new ArrayList<>());
                        judicialResultPrompts.forEach(promptPair -> parent.get().getJudicialResult().getJudicialResultPrompts().addAll(promptPair.getValue()));
                        parent.get().getChildren().remove(treeNode);
                        candidateNodeForRemoval.add(treeNode);
                        //Only set next hearing object if the parent node doesn't have it
                        if(nextHearingOnlyExistsOnChildNode(treeNode, parent.get())) {
                            parent.get().getJudicialResult().setNextHearing(treeNode.getJudicialResult().getNextHearing());
                        }
                    }
                }
        );
        candidateNodeForRemoval.forEach(node -> {
            node.removeAllParents();
            node.removeAllChildren();
        });
        treeNodeList.removeAll(candidateNodeForRemoval);
        return treeNodeList;
    }

    private static boolean nextHearingOnlyExistsOnChildNode(TreeNode<ResultLine2> childNode, TreeNode<ResultLine2> parent) {
        return isNull(parent.getJudicialResult().getNextHearing()) && nonNull(childNode.getJudicialResult().getNextHearing());
    }

    private static void populatePrompts(final TreeNode<ResultLine2> treeNode, final List<Pair<Integer, List<JudicialResultPrompt>>> rankAndJudicialResultPromptPairs) {
        final Integer rank = treeNode.getResultDefinition().getData().getRank();
        final List<JudicialResultPrompt> judicialResultPrompts = treeNode.getJudicialResult().getJudicialResultPrompts();
        if (isNotEmpty(judicialResultPrompts)) {
            rankAndJudicialResultPromptPairs.add(Pair.of(rank, judicialResultPrompts));
        }
    }

    public static Optional<TreeNode<ResultLine2>> processParent(final TreeNode<ResultLine2> treeNode, final List<Pair<Integer, List<JudicialResultPrompt>>> judicialResultPrompts, final List<TreeNode<ResultLine2>> candidateNodeForRemoval) {
        final List<TreeNode<ResultLine2>> parents = treeNode.getParents();
        if (isNotEmpty(parents)) {
            for (final TreeNode<ResultLine2> parent : parents) {
                final Boolean publishedAsAPrompt = parent.getResultDefinition().getData().getPublishedAsAPrompt();
                final Boolean excludedFromResults = parent.getResultDefinition().getData().getExcludedFromResults();
                if (!publishedAsAPrompt || !excludedFromResults) {
                    return Optional.ofNullable(parent);
                } else {
                    populatePrompts(treeNode, judicialResultPrompts);
                    candidateNodeForRemoval.add(parent);
                    return processParent(parent, judicialResultPrompts, candidateNodeForRemoval);
                }
            }
        }
        return Optional.empty();
    }
}
