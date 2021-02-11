package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BailConditionsHelper {

    private static final String BAIL_CONDITIONS = "Bail Conditions";
    private static final String[] applicableBailStatusCodes = new String[]{"L", "P", "B"};

    public void setBailConditions(final ResultsShared resultsShared) {
        resultsShared.getHearing()
                .getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(this::setBailConditions);
    }

    private void setBailConditions(final Defendant defendant) {
        if (nonNull((defendant.getPersonDefendant()))) {
            if (nonNull(defendant.getPersonDefendant().getBailStatus()) && Arrays.stream(applicableBailStatusCodes).anyMatch(defendant.getPersonDefendant().getBailStatus().getCode()::equals)) {
                final String bailConditions = getBailConditions(defendant.getOffences());
                defendant.getPersonDefendant().setBailConditions(bailConditions);
            } else {
                //Set Bail Conditions if Bail Status = ‘B’, ‘P’ or ‘L’. Otherwise field must be blank.
                defendant.getPersonDefendant().setBailConditions("");
            }
        }
    }

    private String getBailConditions(final List<Offence> offences) {
        final List<JudicialResultsLabelAndResultPrompts> judicialResultsLabelAndResultPrompts = getJudicialResultsLabelBasedOnRankAndResultPrompts(offences);
        final StringBuilder bailConditions = constructBailConditions(judicialResultsLabelAndResultPrompts);
        return bailConditions.toString();
    }

    private StringBuilder constructBailConditions(final List<JudicialResultsLabelAndResultPrompts> judicialResultsLabelAndResultPrompts) {
        final StringBuilder bailConditionsBuilder = new StringBuilder();
        for (final JudicialResultsLabelAndResultPrompts judicialResultsLabelAndResultPrompt : judicialResultsLabelAndResultPrompts) {
            final String label = judicialResultsLabelAndResultPrompt.getLabel();

            if(!bailConditionsBuilder.toString().contains(label)) {
                bailConditionsBuilder.append(String.format("%s%n", label));
                for (final JudicialResultPrompt judicialResultPrompt : judicialResultsLabelAndResultPrompt.getJudicialResultPrompts()) {
                    bailConditionsBuilder.append(String.format("%s : %s%n", judicialResultPrompt.getLabel(), judicialResultPrompt.getValue()));
                }
            }
        }
        return bailConditionsBuilder;
    }

    private List<JudicialResultsLabelAndResultPrompts> getJudicialResultsLabelBasedOnRankAndResultPrompts(final List<Offence> offences) {
        final List<JudicialResult> judicialResults = getJudicialResultsBasedOnResultDefinitionGroup(offences);
        return getJudicialResultsBasedOnRank(judicialResults);
    }

    private List<JudicialResultsLabelAndResultPrompts> getJudicialResultsBasedOnRank(final List<JudicialResult> judicialResults) {
        return judicialResults.stream()
                .sorted(comparing(JudicialResult::getRank, nullsLast(naturalOrder())))
                .map(jr -> new JudicialResultsLabelAndResultPrompts(jr.getLabel(), getJudicialResultPromptsBasedOnSequence(jr)))
                .collect(Collectors.toList());
    }

    private List<JudicialResult> getJudicialResultsBasedOnResultDefinitionGroup(final List<Offence> offences) {
        return offences
                .stream()
                .filter(o -> nonNull(o.getJudicialResults()))
                .map(Offence::getJudicialResults)
                .flatMap(List::stream)
                .filter(jr -> nonNull(jr.getResultDefinitionGroup()))
                .filter(jr -> jr.getResultDefinitionGroup().equalsIgnoreCase(BAIL_CONDITIONS))
                .collect(Collectors.toList());
    }

    private List<JudicialResultPrompt> getJudicialResultPromptsBasedOnSequence(final JudicialResult jr) {
        return ofNullable(jr.getJudicialResultPrompts())
                .map(Collection::stream).orElseGet(Stream::empty)
                .sorted(comparing(JudicialResultPrompt::getPromptSequence, nullsLast(naturalOrder())))
                .collect(toList());
    }

    private static class JudicialResultsLabelAndResultPrompts {

        private final String label;
        private final List<JudicialResultPrompt> judicialResultPrompts;

        public JudicialResultsLabelAndResultPrompts(final String label, final List<JudicialResultPrompt> judicialResultPrompts) {
            this.label = label;
            this.judicialResultPrompts = judicialResultPrompts;
        }

        public String getLabel() {
            return label;
        }

        public List<JudicialResultPrompt> getJudicialResultPrompts() {
            return judicialResultPrompts;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final JudicialResultsLabelAndResultPrompts that = (JudicialResultsLabelAndResultPrompts) o;
            return Objects.equals(label, that.label) &&
                    Objects.equals(judicialResultPrompts, that.judicialResultPrompts);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label, judicialResultPrompts);
        }
    }
}
