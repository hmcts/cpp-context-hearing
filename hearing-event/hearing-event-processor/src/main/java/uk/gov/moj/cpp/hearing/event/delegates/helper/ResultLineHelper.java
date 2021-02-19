package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.UUID;

public class ResultLineHelper {
    public ResultLine getResultLine(final List<ResultLine> resultLines, final ResultLine currentResultLine) {
        if(isNull(currentResultLine) || isEmpty(currentResultLine.getParentResultLineIds())) {
            return currentResultLine;
        }

        ResultLine resultLine = null;
        final List<UUID> parentResultLineIds = currentResultLine.getParentResultLineIds();
        for (final UUID parentResultLineId : parentResultLineIds) {
            final ResultLine parentResultLine = findResultLine(resultLines, parentResultLineId);
            resultLine = getResultLine(resultLines, parentResultLine);
        }

        return resultLine;
    }

    public ResultLine findResultLine(final List<ResultLine> resultLines, final UUID resultLineId) {
        return resultLines.stream().filter(resultLine -> resultLineId.equals(resultLine.getResultLineId())).findFirst().orElse(null);
    }

    public TreeNode<ResultLine> getResultLineTreeNode(final Target target, final ResultLine resultLine, final TreeNode<ResultDefinition> resultDefinitionNode, final JudicialResult judicialResult) {
        final TreeNode<ResultLine> treeNode = new TreeNode<>(resultLine.getResultLineId(), resultLine);
        treeNode.setResultDefinition(resultDefinitionNode);
        treeNode.setJudicialResult(judicialResult);
        treeNode.setTargetId(target.getTargetId());
        treeNode.setResultDefinitionId(resultDefinitionNode.getId());
        treeNode.setApplicationId(target.getApplicationId());
        treeNode.setDefendantId(target.getDefendantId());
        treeNode.setOffenceId(target.getOffenceId());
        treeNode.setLevel(resultLine.getLevel());
        return treeNode;
    }
}
