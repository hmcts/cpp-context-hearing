package uk.gov.moj.cpp.hearing.event.delegates.helper;


import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isEmpty;


import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

@SuppressWarnings({"squid:S1612"})
public class ResultTextHelperV3 {

    public static final String CHAR_DASH = " - ";
    public static final String CHAR_EMPTY = "";

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
        if (nonNull(resultTemplate)) {
            final ResultTextParseRule<ResultLine2> resultTextParseRule = new ResultTextParseRule<>();
            final String newResultText = resultTextParseRule.getNewResultText(node, resultTemplate);
            node.getJudicialResult().setResultText(CHAR_EMPTY.equals(newResultText) ? null : newResultText);
        }
        if (ofNullable(node.getJudicialResult().getAlwaysPublished()).orElse(false)) {
            node.getJudicialResult().setResultText(ofNullable(node.getJudicialResult().getResultText())
                    .map(text -> node.getJudicialResult().getShortCode() + CHAR_DASH + node.getJudicialResult().getLabel() + System.lineSeparator() + text)
                    .orElse(node.getJudicialResult().getShortCode() + CHAR_DASH + node.getJudicialResult().getLabel())
            );
        }
    }

}
