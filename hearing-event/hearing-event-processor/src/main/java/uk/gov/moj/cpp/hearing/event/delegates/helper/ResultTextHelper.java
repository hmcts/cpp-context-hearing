package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.convertBooleanPromptValue;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"squid:S1612"})
public class ResultTextHelper {

    private ResultTextHelper(){
        //required by sonar
    }

    public static String getResultText(final ResultDefinition resultDefinition, final ResultLine resultLine) {

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
