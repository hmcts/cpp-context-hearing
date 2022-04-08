package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromApplication;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BailConditionsHelper {

    private static final String BAIL_CONDITIONS = "Bail Conditions";
    private static final String[] applicableBailStatusCodes = new String[]{"L", "P", "B"};

    public void setBailConditions(final Hearing hearing) {
        ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(this::setBailConditions);

        ofNullable(hearing.getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    setBailConditions(ca.getSubject().getMasterDefendant(), offences);
                });
    }

    public void setBailConditions(final ResultsShared resultsShared) {
        ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(this::setBailConditions);

        ofNullable(resultsShared.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    setBailConditions(ca.getSubject().getMasterDefendant(), offences);
                });
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

    private void setBailConditions(final MasterDefendant defendant, final List<Offence> offences) {
        if (nonNull((defendant.getPersonDefendant()))) {
            if (nonNull(defendant.getPersonDefendant().getBailStatus()) && Arrays.stream(applicableBailStatusCodes).anyMatch(defendant.getPersonDefendant().getBailStatus().getCode()::equals)) {
                final String bailConditions = getBailConditions(offences);
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
        final List<JudicialResultsLabelAndResultPrompts> distinctJudicialResultsLabelAndResultPrompts = filterOutDuplicateLabels(judicialResultsLabelAndResultPrompts);
        final Iterator<JudicialResultsLabelAndResultPrompts> resultIterator = distinctJudicialResultsLabelAndResultPrompts.iterator();
        while (resultIterator.hasNext()) {
            final JudicialResultsLabelAndResultPrompts judicialResultsLabelAndResultPrompt = resultIterator.next();
            final String label = judicialResultsLabelAndResultPrompt.getLabel();
            if (!bailConditionsBuilder.toString().contains(label)) {
                final Iterator<JudicialResultPrompt> promptsIterator = judicialResultsLabelAndResultPrompt.getJudicialResultPrompts().iterator();
                addLabelToBailCondition(bailConditionsBuilder, resultIterator, label, promptsIterator);
                while (promptsIterator.hasNext()) {
                    final JudicialResultPrompt judicialResultPrompt = promptsIterator.next();
                    addPromptToBailConditions(bailConditionsBuilder, resultIterator, promptsIterator, judicialResultPrompt);
                }
            }
        }
        return bailConditionsBuilder;
    }

    private void addLabelToBailCondition(final StringBuilder bailConditionsBuilder, final Iterator<JudicialResultsLabelAndResultPrompts> resultIterator, final String label, final Iterator<JudicialResultPrompt> promptsIterator) {
        if (promptsIterator.hasNext()) {
            bailConditionsBuilder.append(String.format("%s%n", label));
        } else {
            if (resultIterator.hasNext()) {
                bailConditionsBuilder.append(String.format("%s ;", label));
            } else {
                bailConditionsBuilder.append(String.format("%s", label));
            }
        }
    }

    private void addPromptToBailConditions(final StringBuilder bailConditionsBuilder, final Iterator<JudicialResultsLabelAndResultPrompts> resultIterator, final Iterator<JudicialResultPrompt> promptsIterator, final JudicialResultPrompt judicialResultPrompt) {
        if (promptsIterator.hasNext()) {
            bailConditionsBuilder.append(String.format("%s : %s%n", judicialResultPrompt.getLabel(), judicialResultPrompt.getValue()));
        } else {
            bailConditionsBuilder.append(String.format("%s : %s", judicialResultPrompt.getLabel(), judicialResultPrompt.getValue()));
            if (resultIterator.hasNext()) {
                bailConditionsBuilder.append(" ;");
            }
        }
    }

    private List<JudicialResultsLabelAndResultPrompts> filterOutDuplicateLabels(final List<JudicialResultsLabelAndResultPrompts> judicialResultsLabelAndResultPrompts) {
        return judicialResultsLabelAndResultPrompts.stream().filter(distinctByKey(JudicialResultsLabelAndResultPrompts::getLabel)).collect(toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        final Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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
