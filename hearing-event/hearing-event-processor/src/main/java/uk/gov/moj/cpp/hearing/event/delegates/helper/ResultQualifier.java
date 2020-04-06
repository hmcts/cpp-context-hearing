package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedListElement;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ResultQualifier {

    public static final String SEPARATOR = "###";
    private static final List<String> fixedListTypes = asList("FIXL", "FIXLM");

    public Optional<String> populate(final String qualifier, final List<JudicialResultPrompt> judicialResultPromptList, final ReferenceDataService referenceDataService, final JsonEnvelope context, final LocalDate orderDate) {

        final List<JudicialResultPrompt> fixedListTypeJudicialResultPrompts = judicialResultPromptList
                .stream()
                .filter(j -> fixedListTypes.contains(j.getType()))
                .collect(toList());

        if (!fixedListTypeJudicialResultPrompts.isEmpty()) {
            final AllFixedList allFixedList = referenceDataService.getAllFixedList(context, orderDate);
            fixedListTypeJudicialResultPrompts.forEach(j -> setQualfierIfFound(j, allFixedList));
        }

        final String promptQualifiers = ofNullable(judicialResultPromptList)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(JudicialResultPrompt::getQualifier)
                .filter(Objects::nonNull)
                .collect(joining(","));

        Optional<String> result = empty();
        if (isNotEmpty(qualifier) && isNotEmpty(promptQualifiers)) {
            result = of(format("%s,%s", qualifier, promptQualifiers));
        } else if (isEmpty(qualifier) && isNotEmpty(promptQualifiers)) {
            result = of(promptQualifiers);
        } else if (isNotEmpty(qualifier) && isEmpty(promptQualifiers)) {
            result = of(qualifier);
        }
        return result;
    }

    private void setQualfierIfFound(final JudicialResultPrompt judicialResultPrompt, final AllFixedList allFixedList) {

        final String value = judicialResultPrompt.getValue();

        if (nonNull(value)) {
            final String result = Stream.of(
                    judicialResultPrompt
                            .getValue()
                            .split(SEPARATOR))

                    .map(s -> getQualifierFromFixedList(s, allFixedList))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(joining(","));
            if (isNotEmpty(result)) {
                judicialResultPrompt.setQualifier(result);
            }
        }
    }

    private Optional<String> getQualifierFromFixedList(final String value, final AllFixedList allFixedList) {
        return allFixedList
                .getFixedListCollection()
                .stream()
                .map(FixedList::getElements)
                .flatMap(Collection::stream)
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .map(FixedListElement::getCjsQualifier)
                .filter(Objects::nonNull)
                .findFirst();
    }
}
