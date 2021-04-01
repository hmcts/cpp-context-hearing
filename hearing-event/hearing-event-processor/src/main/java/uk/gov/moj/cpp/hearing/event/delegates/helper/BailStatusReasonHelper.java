package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromApplication;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class BailStatusReasonHelper {

    private static final List<String> PROMPT_REFERENCES = of("bailConditionReason", "bailExceptionReason");

    public void setReason(final ResultsShared resultsShared) {
        ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(this::setBailStatusReason);

        ofNullable(resultsShared.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    setBailStatusReason(ca.getSubject().getMasterDefendant(), offences);
                });
    }

    private void setBailStatusReason(final Defendant defendant) {
        if (nonNull(defendant.getPersonDefendant())) {
            String bailStatusReason = getBailStatusReason(defendant.getOffences());
            if (nonNull(bailStatusReason) && !bailStatusReason.isEmpty()) {
                defendant.getPersonDefendant().setBailReasons(bailStatusReason);
            }
        }
    }

    private void setBailStatusReason(final MasterDefendant defendant, final List<Offence> offences) {
        if (nonNull(defendant.getPersonDefendant())) {
            final String bailStatusReason = getBailStatusReason(offences);
            if (nonNull(bailStatusReason) && !bailStatusReason.isEmpty()) {
                defendant.getPersonDefendant().setBailReasons(bailStatusReason);
            }
        }
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
                .map(JudicialResultPrompt::getValue).distinct()
                .collect(joining(lineSeparator()));
    }
}
