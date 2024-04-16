package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.delegates.helper.JudicialResultPromptDurationHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DurationElementHelperV3 {


    private DurationElementHelperV3() {
    }

    public static void setDurationElements(final List<TreeNode<ResultLine2>> results) {
        results.stream()
                .filter(LeafNodeHelperV3::isValidTreeNode)
                .forEach(DurationElementHelperV3::setDurationElement);
    }

    private static void setDurationElement(final TreeNode<ResultLine2> treeNode) {
        final LocalDate orderedDate = treeNode.getJudicialResult().getOrderedDate();
        final Optional<JudicialResultPromptDurationElement> resultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(treeNode.getJudicialResult().getJudicialResultPrompts(), treeNode.getResultDefinition().getData(), orderedDate);
        resultPromptDurationElement.ifPresent(judicialResultPromptDurationElement -> treeNode.getJudicialResult().setDurationElement(judicialResultPromptDurationElement));
    }
}
