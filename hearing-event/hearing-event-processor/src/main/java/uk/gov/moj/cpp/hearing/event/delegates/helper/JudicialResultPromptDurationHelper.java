package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.upperCase;
import static uk.gov.justice.core.courts.JudicialResultPromptDurationElement.judicialResultPromptDurationElement;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.DurationDateHelper.populateStartAndEndDates;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JudicialResultPromptDurationElement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.Pair;

public class JudicialResultPromptDurationHelper {

    private static final int PRIMARY_DURATION_TYPE = 1;
    private static final int SECONDARY_DURATION_TYPE = 2;
    private static final String DURATION_UNIT = "L";
    private static final Integer DURATION_VALUE = 1;
    private static final int MAX_VALUE = 999;


    private final BiFunction<List<JudicialResultPrompt>, Integer, Optional<Pair<String, Integer>>> getValue = (judicialResultPrompts, durationType) -> {
        final Optional<JudicialResultPrompt> judicialResultPromptOptional = getJudicialResultPromptDurationElement(judicialResultPrompts, durationType);
        if (judicialResultPromptOptional.isPresent()) {
            return splitDuration(judicialResultPromptOptional);
        }
        return empty();
    };


    public Optional<JudicialResultPromptDurationElement> populate(final List<JudicialResultPrompt> judicialResultPrompts, final Hearing hearing, final ResultDefinition resultDefinition) {
        return populateDurationElement(judicialResultPrompts, hearing, resultDefinition);
    }

    private Optional<JudicialResultPromptDurationElement> populateDurationElement(final List<JudicialResultPrompt> judicialResultPrompts, final Hearing hearing, final ResultDefinition resultDefinition) {

        final JudicialResultPromptDurationElement.Builder builder = judicialResultPromptDurationElement();


        final Optional<Pair<String, Integer>> primaryValue = getValue.apply(judicialResultPrompts, PRIMARY_DURATION_TYPE);

        final boolean lifeDuration = isTrue(resultDefinition.getLifeDuration());

        if (lifeDuration) {
            builder.withPrimaryDurationUnit(DURATION_UNIT).withPrimaryDurationValue(DURATION_VALUE);
        } else {
            if (primaryValue.isPresent()) {
                final Pair<String, Integer> resultPair = primaryValue.get();
                final String unit = resultPair.getKey();
                final int value = resultPair.getValue();
                if (value <= MAX_VALUE) {
                    builder.withPrimaryDurationValue(value).withPrimaryDurationUnit(unit);
                } else {
                    populateStartAndEndDates(builder, hearing, primaryValue.get());
                }
            }
        }

        final Optional<Pair<String, Integer>> secondaryValue = getValue.apply(judicialResultPrompts, SECONDARY_DURATION_TYPE);
        if (secondaryValue.isPresent()) {
            final Pair<String, Integer> resultPair = secondaryValue.get();
            final String unit = resultPair.getKey();
            final int value = resultPair.getValue();
            builder.withSecondaryDurationValue(value).withSecondaryDurationUnit(unit);
        }


        if (lifeDuration || primaryValue.isPresent() || secondaryValue.isPresent()) {
            return of(builder.build());
        } else {
            return empty();
        }

    }

    private Optional<JudicialResultPrompt> getJudicialResultPromptDurationElement(final List<JudicialResultPrompt> judicialResultPrompts, final Integer durationType) {
        if (isNull(judicialResultPrompts)) {
            return empty();
        }

        return judicialResultPrompts.stream()
                .filter(Objects::nonNull)
                .filter(p -> nonNull(p.getDurationSequence()) && p.getDurationSequence().equals(durationType))
                .findFirst();
    }

    private Optional<Pair<String, Integer>> splitDuration(final Optional<JudicialResultPrompt> judicialResultPromptOptional) {
        final String[] splitDuration = judicialResultPromptOptional.map(JudicialResultPrompt::getValue).map(s -> s.split("\\s+")).orElse(null);
        if (splitDuration != null && splitDuration.length == 2) {
            final int value = parseInt(splitDuration[0]);
            final String unit = upperCase(valueOf(splitDuration[1].charAt(0)));
            return of(Pair.of(unit, value));
        }
        return empty();
    }
}
