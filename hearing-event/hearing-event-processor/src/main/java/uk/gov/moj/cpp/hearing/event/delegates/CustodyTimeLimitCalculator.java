package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Offence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1612", "squid:S00112", "squid:S00108", "squid:S1166"})
public class CustodyTimeLimitCalculator {


    public static final String CUSTODY_OR_REMANDED_INTO_CUSTODY = "C";
    public static final String REMANDED_INTO_CARE_OF_LOCAL_AUTHORITY = "L";

    public static final String CTL_TIME_LIMIT_PROMPT_REF = "CTLDATE";
    public static final String CTL_DAYS_SPENT_PROMPT_REF = "CTLTIME";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final int DEFAULT_DEFENDANT_LEVEL_TIME_SPENT_DAYS = 0;


    public void calculate(Hearing hearing) {
        //go through the offences
        hearing.getProsecutionCases().stream()
                .flatMap(pc -> pc.getDefendants().stream())
                .filter(CustodyTimeLimitCalculator::valid)
                .forEach(this::calculate);
    }

    private void calculate(Defendant defendant) {

        if (Objects.isNull(defendant.getOffences())) {
            return;
        }
        final Map<Offence, CustodyTimeLimit> offencesToCustodyTimeLimit =
                defendant.getOffences().stream()
                        .map(o -> new AbstractMap.SimpleEntry<>(o, getCustodyTimeLimit(o)))
                        .filter(p -> Objects.nonNull(p.getValue()))
                        .collect(Collectors.toMap(
                                AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue
                        ));
        if (offencesToCustodyTimeLimit.isEmpty()) {
            return;
        }
        LocalDate defenceLevelCustodyTimeLimit = null;
        for (final Offence offence : defendant.getOffences()) {
            if (offencesToCustodyTimeLimit.containsKey(offence)) {
                offence.setCustodyTimeLimit(offencesToCustodyTimeLimit.get(offence));
            }
            if (Objects.nonNull(offence.getCustodyTimeLimit()) && (Objects.isNull(defenceLevelCustodyTimeLimit) || defenceLevelCustodyTimeLimit.isAfter(offence.getCustodyTimeLimit().getTimeLimit()))) {
                defenceLevelCustodyTimeLimit = offence.getCustodyTimeLimit().getTimeLimit();
            }
        }
        defendant.getPersonDefendant().setCustodyTimeLimit(defenceLevelCustodyTimeLimit);
        defendant.getPersonDefendant().getBailStatus().
                setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit().withTimeLimit(defenceLevelCustodyTimeLimit).withDaysSpent(DEFAULT_DEFENDANT_LEVEL_TIME_SPENT_DAYS).build());
    }

    private CustodyTimeLimit getCustodyTimeLimit(Offence offence) {
        Optional<CustodyTimeLimit> custodyTimeLimit = Optional.empty();
        if (Objects.nonNull(offence.getJudicialResults()) && Objects.isNull(offence.getConvictionDate())) {
            custodyTimeLimit = offence.getJudicialResults().stream()
                    .filter(Objects::nonNull)
                    .filter(jr -> Objects.nonNull(jr.getJudicialResultPrompts()))
                    .map(this::extractCustodyTimeLimit)
                    .filter(Objects::nonNull)
                    .findFirst();
        }

        return custodyTimeLimit.orElseGet(offence::getCustodyTimeLimit);
    }

    private CustodyTimeLimit extractCustodyTimeLimit(final JudicialResult judicialResult) {
        final Optional<JudicialResultPrompt> timeLimitPrompt = judicialResult.getJudicialResultPrompts().stream()
                .filter(p -> CTL_TIME_LIMIT_PROMPT_REF.equalsIgnoreCase(p.getPromptReference()))
                .findFirst();

        if (!timeLimitPrompt.isPresent()) {
            return null;
        }

        final JudicialResultPrompt daysSpentPrompt = judicialResult.getJudicialResultPrompts().stream()
                .filter(p -> CTL_DAYS_SPENT_PROMPT_REF.equalsIgnoreCase(p.getPromptReference()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("failed to find day spent in judicialResult %s", judicialResult.getJudicialResultId())));
        final int daysSpent = Integer.parseInt(daysSpentPrompt.getValue().trim());

        final LocalDate timeLimit = LocalDate.parse(timeLimitPrompt.get().getValue(), DateTimeFormatter.ofPattern(DATE_FORMAT));
        return new CustodyTimeLimit(daysSpent, timeLimit);

    }

    private static boolean valid(Defendant defendant) {
        Optional<BailStatus> bailStatus = Optional.empty();
        if (Objects.nonNull(defendant.getPersonDefendant())) {
            bailStatus = Optional.ofNullable(defendant.getPersonDefendant().getBailStatus());
        }
        return bailStatus.isPresent() && (CUSTODY_OR_REMANDED_INTO_CUSTODY.equals(bailStatus.get().getCode()) || REMANDED_INTO_CARE_OF_LOCAL_AUTHORITY.equals(bailStatus.get().getCode()));
    }

}
