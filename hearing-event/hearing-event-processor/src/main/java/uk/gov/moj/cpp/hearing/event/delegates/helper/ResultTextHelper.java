package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;

@SuppressWarnings({"squid:S1612"})
public class ResultTextHelper {

    public static final String CHAR_DASH = " - ";
    public static final String CHAR_EMPTY = "";

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
        if (nonNull(resultTemplate)) {
            final ResultTextParseRule<ResultLine> resultTextParseRule = new ResultTextParseRule<>();
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
