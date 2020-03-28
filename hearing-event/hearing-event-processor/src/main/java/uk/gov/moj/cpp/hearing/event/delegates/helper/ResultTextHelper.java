package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings({"squid:S1612"})
public class ResultTextHelper {

    private ResultTextHelper(){
        //required by sonar
    }

    public static String getResultText(final ResultDefinition resultDefinition, final ResultLine resultLine) {

        final List<UUID> referenceList = resultDefinition
                .getPrompts()
                .stream()
                .sorted(comparing(p -> p.getSequence(), nullsLast(naturalOrder())))
                .map(p -> p.getId())
                .filter(Objects::nonNull)
                .collect(toList());

        final List<Prompt> sortedPromptList = resultLine
                .getPrompts()
                .stream()
                .sorted(new UUIDComparator(referenceList))
                .collect(toList());

        final String sortedPrompts = sortedPromptList
                .stream()
                .map(p -> format("%s %s", p.getLabel(), p.getValue()))
                .collect(joining(lineSeparator()));

        return getResultText(resultDefinition.getLabel(), sortedPrompts);
    }

    public static String getResultText(final String label, final String sortedPrompts){
        return format("%s%s%s", label, lineSeparator(), sortedPrompts);
    }
}
