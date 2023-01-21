package uk.gov.moj.cpp.hearing.event.delegates.helper;


import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({"squid:S1612"})
public class ResultTextHelperV3 {

    public static final String CHAR_DASH = " - ";
    public static final String CHAR_EMPTY = "";

    private ResultTextHelperV3() {
        //required by sonar
    }

    public static void setResultText(final List<TreeNode<ResultLine2>> treeNodeList) {
        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .forEach(ResultTextHelperV3::setResultText);

        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .forEach(ResultTextHelperV3::updateResultTextForAlwaysPublished);

        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .filter(node ->!ofNullable(node.getJudicialResult().getAlwaysPublished()).orElse(false))
                .forEach(node -> updateResultTextTop(node,node.getResultDefinition().getData().getShortCode() + CHAR_DASH + node.getJudicialResult().getLabel()));
        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .forEach(ResultTextHelperV3::setEmptyResultText);
    }

    private static void setEmptyResultText(final TreeNode<ResultLine2> node) {
        node.getChildren().forEach(ResultTextHelperV3::setEmptyResultText);
        if(! ofNullable(node.getJudicialResult().getResultText()).isPresent()){
            node.getJudicialResult().setResultText(node.getResultDefinition().getData().getShortCode() + CHAR_DASH + node.getJudicialResult().getLabel());
        }
    }

    private static void updateResultTextForAlwaysPublished(final TreeNode<ResultLine2> node) {
        node.getChildren().forEach(ResultTextHelperV3::updateResultTextForAlwaysPublished);
        if (ofNullable(node.getJudicialResult().getAlwaysPublished()).orElse(false)) {
            updateResultTextTop(node, node.getResultDefinition().getData().getShortCode() + CHAR_DASH + node.getJudicialResult().getLabel());
        }

    }
    @SuppressWarnings("PMD.NullAssignment")
    private static void setResultText(final TreeNode<ResultLine2> node) {
        node.getChildren().forEach(ResultTextHelperV3::setResultText);
        final String resultTemplate = node.getResultDefinition().getData().getResultTextTemplate();
        if (nonNull(resultTemplate)) {
            final ResultTextParseRule<ResultLine2> resultTextParseRule = new ResultTextParseRule<>();
            final String newResultText = resultTextParseRule.getNewResultText(node, resultTemplate);
            node.getJudicialResult().setResultText(CHAR_EMPTY.equals(newResultText) ? null : newResultText);
        }
    }

    private static void updateResultTextTop(final TreeNode<ResultLine2> node, final String prefix) {
        node.getJudicialResult().setResultText(ofNullable(node.getJudicialResult().getResultText())
                .map(resultText -> ofNullable(prefix).map(s -> s + lineSeparator() + resultText).orElse(resultText))
                .orElse(prefix)
        );
    }

    private static String getGroupResultText(final TreeNode<ResultLine2> node, final String dependantResultDefinitionGroup){
        if(isEmpty(node.getChildren())){
            if(dependantResultDefinitionGroup.equals(node.getResultDefinition().getData().getDependantResultDefinitionGroup())){
                return node.getJudicialResult().getResultText();
            }else{
                return "";
            }
        }else{
            return node.getChildren().stream().map( n -> ResultTextHelperV3.getGroupResultText(n, dependantResultDefinitionGroup))
                    .filter(StringUtils::isNoneEmpty)
                    .collect(Collectors.joining(lineSeparator()));
        }
    }
}
