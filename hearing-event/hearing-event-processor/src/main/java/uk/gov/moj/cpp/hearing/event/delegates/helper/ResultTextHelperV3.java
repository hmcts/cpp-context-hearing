package uk.gov.moj.cpp.hearing.event.delegates.helper;


import static org.apache.commons.collections.CollectionUtils.isEmpty;


import java.util.Objects;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

@SuppressWarnings({"squid:S1612"})
public class ResultTextHelperV3 {

    private ResultTextHelperV3(){
        //required by sonar
    }

    public static void setResultText(final List<TreeNode<ResultLine2>> treeNodeList){
        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .forEach(ResultTextHelperV3::setResultText);
    }

    private static void setResultText(final TreeNode<ResultLine2> node) {
        node.getChildren().forEach(ResultTextHelperV3::setResultText);
        final String resultTemplate = node.getJudicialResult().getResultTextTemplate();
        if(Objects.isNull(resultTemplate)){
            return;
        }
        final ResultTextParseRule<ResultLine2> resultTextParseRule = new ResultTextParseRule<>();
        final String newResultText = resultTextParseRule.getNewResultText(node, resultTemplate);
        node.getJudicialResult().setResultText("".equals(newResultText) ? null : newResultText);
    }

}
