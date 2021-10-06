package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Target2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.UUID;

public class ResultLineHelperV3 {
    public ResultLine2 getResultLine(final List<ResultLine2> resultLines, final ResultLine2 currentResultLine) {
        if(isNull(currentResultLine) || isEmpty(currentResultLine.getParentResultLineIds())) {
            return currentResultLine;
        }

        ResultLine2 resultLine = null;
        final List<UUID> parentResultLineIds = currentResultLine.getParentResultLineIds();
        for (final UUID parentResultLineId : parentResultLineIds) {
            final ResultLine2 parentResultLine = findResultLine(resultLines, parentResultLineId);
            resultLine = getResultLine(resultLines, parentResultLine);
        }

        return resultLine;
    }

    public ResultLine2 findResultLine(final List<ResultLine2> resultLines, final UUID resultLineId) {
        return resultLines.stream().filter(resultLine -> resultLineId.equals(resultLine.getResultLineId())).findFirst().orElse(null);
    }

    public TreeNode<ResultLine2> getResultLineTreeNode(final Target2 target, final ResultLine2 resultLine, final TreeNode<ResultDefinition> resultDefinitionNode, final JudicialResult judicialResult) {
        final TreeNode<ResultLine2> treeNode = new TreeNode<>(resultLine.getResultLineId(), resultLine);
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
