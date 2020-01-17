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
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1612", "squid:S00112", "squid:S00108", "squid:S1166"})
public class CustodyTimeLimitCalculator {


    public static final String CUSTODY_OR_REMANDED_INTO_CUSTODY = "C";
    public static final String REMANDED_INTO_CARE_OF_LOCAL_AUTHORITY = "L";

    public static final String CTL_TIME_LIMIT_PROMPT_REF = "CTLDATE";
    public static final String CTL_DAYS_SPENT_PROMPT_REF = "CTLTIME";
    public static final String DATE_FORMAT0 = "yyyy-MM-dd";
    public static final String DATE_FORMAT_1 = "dd-MM-yyyy";
    public static final String DATE_FORMAT_2 = "dd/MM/yy";
    public static final String DATE_FORMAT_3 = "dd MMM yyyy";
    public static final int DEFAULT_DEFENDANT_LEVEL_TIME_SPENT_DAYS = 0;
    protected static final String[] DATE_FORMATS = {DATE_FORMAT0, DATE_FORMAT_1, DATE_FORMAT_2,DATE_FORMAT_3};


    public void calculate(Hearing hearing) {
        //go through the offences
        hearing.getProsecutionCases().stream().flatMap(pc -> pc.getDefendants().stream()).filter(d -> valid(d)).forEach(
                d -> calculate(d)
        );
    }

    private void calculate(Defendant defendant) {

        if (defendant.getOffences() == null) {
            return;
        }
        final Map<Offence, CustodyTimeLimit> offencesToCustodyTimeLimit =
                defendant.getOffences().stream().map(o -> new AbstractMap.SimpleEntry<>(o, getCustodyTimeLimit(o)))
                        .filter(p -> p.getValue() != null)
                        .collect(Collectors.toMap(
                                pf -> pf.getKey(), pf -> pf.getValue()
                        ));
        if (offencesToCustodyTimeLimit.size() == 0) {
            return;
        }
        LocalDate defenceLevelCustodyTimeLimit = null;
        for (final Offence offence : defendant.getOffences()) {
            if (offencesToCustodyTimeLimit.containsKey(offence)) {
                offence.setCustodyTimeLimit(offencesToCustodyTimeLimit.get(offence));
            }
            if (offence.getCustodyTimeLimit() != null && (defenceLevelCustodyTimeLimit == null || defenceLevelCustodyTimeLimit.isAfter(offence.getCustodyTimeLimit().getTimeLimit()))) {
                defenceLevelCustodyTimeLimit = offence.getCustodyTimeLimit().getTimeLimit();
            }
        }
        defendant.getPersonDefendant().setCustodyTimeLimit(defenceLevelCustodyTimeLimit);
        defendant.getPersonDefendant().getBailStatus().
                setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit().withTimeLimit(defenceLevelCustodyTimeLimit).withDaysSpent(DEFAULT_DEFENDANT_LEVEL_TIME_SPENT_DAYS).build());
    }

    private CustodyTimeLimit getCustodyTimeLimit(Offence offence) {
        CustodyTimeLimit custodyTimeLimit = null;
        if (offence.getJudicialResults() != null && offence.getConvictionDate() == null) {
            custodyTimeLimit = offence.getJudicialResults().stream()
                    .filter(Objects::nonNull)
                    .filter(jr -> jr.getJudicialResultPrompts() !=null)
                    .map(jr -> extractCustodyTimeLimit(jr)).filter(ctl -> ctl != null)
                    .findFirst().orElse(null);
        }
        if (custodyTimeLimit != null) {
            return custodyTimeLimit;
        } else {
            return offence.getCustodyTimeLimit();
        }
    }

    private CustodyTimeLimit extractCustodyTimeLimit(final JudicialResult judicialResult) {
        final JudicialResultPrompt timeLimitPrompt = judicialResult.getJudicialResultPrompts().stream()
                .filter(p -> CTL_TIME_LIMIT_PROMPT_REF.equalsIgnoreCase(p.getPromptReference())).findFirst().orElse(null);
        if (timeLimitPrompt == null) {
            return null;
        }

        final JudicialResultPrompt daysSpentPrompt = judicialResult.getJudicialResultPrompts().stream().filter(p -> CTL_DAYS_SPENT_PROMPT_REF.equalsIgnoreCase(p.getPromptReference())).findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("failed to find day spent in judicialResult %s", judicialResult.getJudicialResultId())));
        final int daysSpent = Integer.parseInt(daysSpentPrompt.getValue().trim());

        final LocalDate timeLimit = parseLocalDate(timeLimitPrompt.getValue());
        return new CustodyTimeLimit(daysSpent, timeLimit);

    }


    private LocalDate parseLocalDate(String str) {
        for (final String format : DATE_FORMATS) {
            try {
                return LocalDate.parse(str, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException pex) {
            }
        }
        throw new RuntimeException(String.format("failed to parse %s with formats %s", str, DATE_FORMATS));
    }

    private static boolean valid(Defendant defendant) {
        BailStatus bailStatus = null;
        if (defendant.getPersonDefendant() != null) {
            bailStatus = defendant.getPersonDefendant().getBailStatus();
        }
        return bailStatus != null && (CUSTODY_OR_REMANDED_INTO_CUSTODY.equals(bailStatus.getCode()) || REMANDED_INTO_CARE_OF_LOCAL_AUTHORITY.equals(bailStatus.getCode()));
    }

}
