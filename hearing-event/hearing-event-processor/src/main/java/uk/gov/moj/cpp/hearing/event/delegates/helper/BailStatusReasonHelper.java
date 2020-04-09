package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.util.List;

public class BailStatusReasonHelper {

    private static final List<String> PROMPT_REFERENCES = of("BAILCONDREAS", "BAILEXCEPTREAS");

    public void setReason(final ResultsShared resultsShared) {

        resultsShared.getHearing()
                .getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .map(this::setBailStatusReason)
                .collect(toList());
    }

    private Defendant setBailStatusReason(final Defendant defendant) {

        defendant
                .getPersonDefendant()
                .setBailReasons(getBailStatusReason(defendant.getOffences()));

        return defendant;
    }

    private String getBailStatusReason(final List<Offence> offences) {

        return offences
                .stream()
                .filter(o -> null != o.getJudicialResults())
                .map(Offence::getJudicialResults)
                .flatMap(List::stream)
                .filter(j -> null != j.getJudicialResultPrompts())
                .map(JudicialResult::getJudicialResultPrompts)
                .flatMap(List::stream)
                .filter(prompt -> PROMPT_REFERENCES.contains(prompt.getPromptReference()))
                .map(JudicialResultPrompt::getValue)
                .collect(joining(lineSeparator()));
    }
}
