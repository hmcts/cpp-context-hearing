package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.Objects;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

@SuppressWarnings({"squid:S1612"})
public class ResultTextHelper {

    private ResultTextHelper(){
        //required by sonar
    }

    public static void setResultText(final List<TreeNode<ResultLine>> treeNodeList){
        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .forEach(ResultTextHelper::setResultText);
    }

    private static void setResultText(final TreeNode<ResultLine> node) {
        node.getChildren().forEach(ResultTextHelper::setResultText);
        final String resultTemplate = node.getJudicialResult().getResultTextTemplate();
        if(Objects.isNull(resultTemplate)){
            return;
        }
        final ResultTextParseRule<ResultLine> resultTextParseRule = new ResultTextParseRule<>();
        final String newResultText = resultTextParseRule.getNewResultText(node, resultTemplate);
        node.getJudicialResult().setResultText("".equals(newResultText) ? null : newResultText);
    }

}
