package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Stream;

public class PromptSequenceNumberHelper {

    private PromptSequenceNumberHelper() {
    }

    public static BigDecimal getNextPromptSequenceNumber(final TreeNode<ResultLine> resultLineTreeNode) {

        return ofNullable(resultLineTreeNode)
                .map(TreeNode::getJudicialResult)
                .map(JudicialResult::getJudicialResultPrompts)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(JudicialResultPrompt::getPromptSequence)
                .sorted(reverseOrder())
                .findFirst()
                .map(ps -> ps.add(PublishAsPromptHelper.PROMPT_SEQUENCE_NUMBER_INCREMENT))
                .orElse(PublishAsPromptHelper.DEFAULT_PROMPT_SEQUENCE_NUMBER);

    }
}
