package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Stream;

public class JudicialResultPromptHelper {

    private JudicialResultPromptHelper() {
    }

    public static JudicialResultPrompt makePrompt(final TreeNode<ResultLine> resultLineTreeNode, final BigDecimal newPromptSequenceNumber) {
        final String newPromptValue =
                ofNullable(resultLineTreeNode)
                        .map(TreeNode::getJudicialResult)
                        .map(JudicialResult::getJudicialResultPrompts)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .map(p -> format("%s:%s", p.getLabel(), p.getValue()))
                        .collect(joining(lineSeparator()));

        final ResultDefinition resultDefinition = resultLineTreeNode.getResultDefinition().getData();

        return judicialResultPrompt()
                .withJudicialResultPromptTypeId(resultDefinition.getId())
                .withCourtExtract(parseCourtExtract(resultDefinition))
                .withPromptSequence(newPromptSequenceNumber)
                .withLabel(resultDefinition.getLabel())
                .withWelshLabel(resultDefinition.getWelshLabel())
                .withValue(newPromptValue)
                .withQualifier(resultDefinition.getQualifier())
                .withUsergroups(resultDefinition.getUserGroups())
                .build();
    }

    private static String parseCourtExtract(final ResultDefinition resultDefinition){
        final Boolean isAvailableForCourtExtract = resultDefinition.getIsAvailableForCourtExtract();
        return nonNull(isAvailableForCourtExtract) && isAvailableForCourtExtract ? "Y" : "N";
    }
}
