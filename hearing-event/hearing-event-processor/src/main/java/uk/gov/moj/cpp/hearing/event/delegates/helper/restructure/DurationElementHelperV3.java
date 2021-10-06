package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.delegates.helper.JudicialResultPromptDurationHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;

public class DurationElementHelperV3 {


    private DurationElementHelperV3() {
    }

    public static void setDurationElements(final List<TreeNode<ResultLine2>> results, final Hearing hearing) {
        results.stream()
                .filter(LeafNodeHelperV3::isValidTreeNode)
                .forEach(n -> setDurationElement(n, hearing));
    }

    private static void setDurationElement(final TreeNode<ResultLine2> treeNode, final Hearing hearing) {
        final Optional<JudicialResultPromptDurationElement> resultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(treeNode.getJudicialResult().getJudicialResultPrompts(), hearing, treeNode.getResultDefinition().getData());
        if (resultPromptDurationElement.isPresent()) {
            treeNode.getJudicialResult().setDurationElement(resultPromptDurationElement.get());
        }
    }
}
