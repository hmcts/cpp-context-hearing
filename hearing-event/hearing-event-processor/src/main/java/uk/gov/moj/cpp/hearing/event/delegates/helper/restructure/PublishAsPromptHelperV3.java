package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.FindNewParentHelperV3.findNewParent;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.JudicialResultPromptHelperV3.makePrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.LeafNodeHelperV3.getLowestRankedLeafNodeWithIsPublishAsPromptFlag;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PromptSequenceNumberHelperV3.getNextPromptSequenceNumber;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RemoveNodeFromTreeHelperV3.remove;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PublishAsPromptHelperV3 {

    static final BigDecimal PROMPT_SEQUENCE_NUMBER_INCREMENT = new BigDecimal(100);
    static final BigDecimal DEFAULT_PROMPT_SEQUENCE_NUMBER = new BigDecimal(0);

    private PublishAsPromptHelperV3() {
    }

    public static List<TreeNode<ResultLine2>> processPublishAsPrompt(final List<TreeNode<ResultLine2>> results) {
        Optional<TreeNode<ResultLine2>> leafNode;
        while ((leafNode = getLowestRankedLeafNodeWithIsPublishAsPromptFlag(results)).isPresent()) {
            final Optional<TreeNode<ResultLine2>> newParentOptional = findNewParent(leafNode.get());
            if (newParentOptional.isPresent()) {
                processPublishAsPrompt(newParentOptional.get(), leafNode.get(), results);
            } else {
                break;
            }
        }
        return results;
    }

    static void processPublishAsPrompt(final TreeNode<ResultLine2> targetParent, final TreeNode<ResultLine2> resultLineTreeNode, final List<TreeNode<ResultLine2>> results) {
        final BigDecimal nextPromptSequenceNumber = getNextPromptSequenceNumber(targetParent);
        final JudicialResultPrompt newPrompt = makePrompt(resultLineTreeNode, nextPromptSequenceNumber);

        if (isNull(targetParent.getJudicialResult().getJudicialResultPrompts())) {
            targetParent.getJudicialResult().setJudicialResultPrompts(new ArrayList<>());
        }

        targetParent.getJudicialResult()
                .getJudicialResultPrompts()
                .add(newPrompt);

        if (nonNull(newPrompt.getQualifier())) {
            final String resultQualifier = targetParent.getJudicialResult().getQualifier();
            if (isNull(resultQualifier)) {
                targetParent.getJudicialResult().setQualifier(newPrompt.getQualifier());
            } else {
                targetParent.getJudicialResult().setQualifier(format("%s,%s", resultQualifier, newPrompt.getQualifier()));
            }
        }
        if (isNull(targetParent.getJudicialResult().getNextHearing())
                && nonNull(resultLineTreeNode.getJudicialResult())
                && nonNull(resultLineTreeNode.getJudicialResult().getNextHearing())
                && isValidNextHearing(resultLineTreeNode.getJudicialResult().getNextHearing())) {
            targetParent.getJudicialResult().setNextHearing(resultLineTreeNode.getJudicialResult().getNextHearing());
        }

        remove(resultLineTreeNode, results);
    }

    static boolean isValidNextHearing(NextHearing nextHearing) {

        if (isNull(nextHearing.getType()) || isNull(nextHearing.getCourtCentre())) {
            return false;
        } else if (nonNull(nextHearing.getEstimatedMinutes()) && nonNull(nextHearing.getListedStartDateTime())) {
            return true;
        } else if (nonNull(nextHearing.getEstimatedMinutes()) && nonNull(nextHearing.getWeekCommencingDate())) {
            return true;
        } else if (nonNull(nextHearing.getDateToBeFixed())) {
            return true;
        }
        return false;
    }
}
