package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.delegates.helper.JudicialResultPromptDurationHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DurationElementHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DurationElementHelper.class);

    private DurationElementHelper() {
    }

    public static void setDurationElements(final List<TreeNode<ResultLine>> results) {
        results.stream()
                .filter(LeafNodeHelper::isValidTreeNode)
                .forEach(DurationElementHelper::setDurationElement);
    }

    private static void setDurationElement(final TreeNode<ResultLine> treeNode) {
        final LocalDate orderedDate = treeNode.getJudicialResult().getOrderedDate();
        final Optional<JudicialResultPromptDurationElement> resultPromptDurationElement = new JudicialResultPromptDurationHelper().populate(treeNode.getJudicialResult().getJudicialResultPrompts(), treeNode.getResultDefinition().getData(), orderedDate);
        resultPromptDurationElement.ifPresent(judicialResultPromptDurationElement -> treeNode.getJudicialResult().setDurationElement(judicialResultPromptDurationElement));
        LOGGER.info("After setting duration element: {}", treeNode.getJudicialResult());
    }
}
