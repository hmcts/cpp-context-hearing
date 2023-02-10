package uk.gov.moj.cpp.hearing.event.delegates.helper;


import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.convertBooleanPromptValue;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.ResultTextHelper.PROMPT_PREDICATE;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ResultTextConfHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import java.util.List;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;


@SuppressWarnings({"squid:S1612"})
public class ResultTextHelperV3 {

    public static final String CHAR_DASH = " - ";
    public static final String CHAR_EMPTY = "";

    private ResultTextHelperV3() {
        //required by sonar
    }

    public static void setResultText(final List<TreeNode<ResultLine2>> treeNodeList, final ResultTextConfHelper resultTextConfHelper) {
        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .filter(node -> !resultTextConfHelper.isOldResultDefinition(node.getJudicialResult().getOrderedDate()))
                .forEach(ResultTextHelperV3::setResultText);

        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .filter(node -> !resultTextConfHelper.isOldResultDefinition(node.getJudicialResult().getOrderedDate()))
                .forEach(ResultTextHelperV3::updateResultTextForAlwaysPublished);

        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .filter(node ->!ofNullable(node.getJudicialResult().getAlwaysPublished()).orElse(false))
                .filter(node -> !resultTextConfHelper.isOldResultDefinition(node.getJudicialResult().getOrderedDate()))
                .forEach(node -> updateResultTextTop(node,node.getResultDefinition().getData().getShortCode() + CHAR_DASH + node.getJudicialResult().getLabel()));
        treeNodeList.stream()
                .filter(node -> isEmpty(node.getParents()))
                .filter(node -> !resultTextConfHelper.isOldResultDefinition(node.getJudicialResult().getOrderedDate()))
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

    public static String getResultText(final ResultDefinition resultDefinition, final ResultLine2 resultLine) {

        final List<Prompt> referencePromptList = resultDefinition
                .getPrompts()
                .stream()
                .filter(p -> !TRUE.equals(p.isHidden()))
                .sorted(comparing(Prompt::getSequence, nullsLast(naturalOrder())))
                .filter(Objects::nonNull)
                .collect(toList());

        final List<UUID> referenceList = referencePromptList
                .stream()
                .map(Prompt::getId)
                .collect(toList());

        final List<uk.gov.justice.core.courts.Prompt> sortedPromptList = resultLine
                .getPrompts()
                .stream()
                .filter(p -> referenceList.contains(p.getId()))
                .sorted(new UUIDComparator(referenceList))
                .collect(toList());

        final String sortedPrompts = sortedPromptList
                .stream()
                .filter(PROMPT_PREDICATE)
                .map(p -> format("%s %s", p.getLabel(), getPromptValue(p, referencePromptList)))
                .collect(joining(lineSeparator()));

        return getResultText(resultDefinition.getLabel(), sortedPrompts);

    }

    public static String getResultText(final String label, final String sortedPrompts){
        return format("%s%s%s", label, lineSeparator(), sortedPrompts);
    }

    private static String getPromptValue(final uk.gov.justice.core.courts.Prompt prompt, final List<Prompt> referencePromptList) {
        final Optional<Prompt> optionalPrompt = referencePromptList.stream().filter(p -> p.getId().equals(prompt.getId())).findFirst();
        final String originalValue = prompt.getValue();

        if (optionalPrompt.isPresent() && "BOOLEAN".equalsIgnoreCase(optionalPrompt.get().getType())) {
            return convertBooleanPromptValue(originalValue);
        }
        return originalValue;
    }
}
