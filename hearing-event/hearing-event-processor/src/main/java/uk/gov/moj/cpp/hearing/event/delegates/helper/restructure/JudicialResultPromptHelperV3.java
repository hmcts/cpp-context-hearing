package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructuringHelperV3.JUDICIAL_RESULT_PROMPT_PREDICATE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.convertBooleanPromptValue;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class JudicialResultPromptHelperV3 {

    private JudicialResultPromptHelperV3() {
    }

    public static JudicialResultPrompt makePrompt(final TreeNode<ResultLine2> resultLineTreeNode, final BigDecimal newPromptSequenceNumber) {
        final String newPromptValue =
                ofNullable(resultLineTreeNode)
                        .map(TreeNode::getJudicialResult)
                        .map(JudicialResult::getJudicialResultPrompts)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .filter(JUDICIAL_RESULT_PROMPT_PREDICATE)
                        .map(p -> format("%s:%s", p.getLabel(), getPromptValue(p.getValue(),p.getType())))
                        .collect(joining(lineSeparator()));

        final ResultDefinition resultDefinition = requireNonNull(resultLineTreeNode).getResultDefinition().getData();

        final JudicialResultPrompt.Builder builder = judicialResultPrompt()
                .withJudicialResultPromptTypeId(resultDefinition.getId())
                .withCourtExtract(parseCourtExtract(resultDefinition))
                .withPromptSequence(newPromptSequenceNumber)
                .withLabel(resultDefinition.getLabel())
                .withWelshLabel(resultDefinition.getWelshLabel())
                .withValue(newPromptValue)
                .withQualifier(resultDefinition.getQualifier())
                .withUsergroups(resultDefinition.getUserGroups());

        final Optional<String> promptReference = setPromptReference(resultLineTreeNode);

        promptReference.ifPresent(builder::withPromptReference);

        return builder.build();
    }

    private static String getPromptValue(final String originalValue, final String promptType) {
        if ("BOOLEAN".equalsIgnoreCase(promptType)) {
            return convertBooleanPromptValue(originalValue);
        }
        return originalValue;
    }

    private static String parseCourtExtract(final ResultDefinition resultDefinition) {
        final Boolean isAvailableForCourtExtract = resultDefinition.getIsAvailableForCourtExtract();
        return nonNull(isAvailableForCourtExtract) && isAvailableForCourtExtract ? "Y" : "N";
    }

    private static Optional<String> setPromptReference(final TreeNode<ResultLine2> resultLineTreeNode) {
        return of(resultLineTreeNode)
                .filter(node -> nonNull(node.getJudicialResult()) && nonNull(node.getJudicialResult().getJudicialResultId()))
                .map(node -> node.getJudicialResult().getJudicialResultId().toString());
    }
}
