package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Arrays.asList;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.AMOUNT_OF_COSTS_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.COMPENSATION_AMOUNT_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.COSTS_TO_CROWN_PROSECUTION_SERVICE_AMOUNT_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.FINE_AMOUNT_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.VEHICLE_EXCISE_BACK_DUTY_AMOUNT_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.VICTIM_SURCHARGE_AMOUNT_PROMPT_REFERENCE;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinancialResultCalculator {


    private static final Logger LOGGER = LoggerFactory.getLogger(FinancialResultCalculator.class);
    private static final List<String> FINANCIAL_TOTAL_AMOUNT_IMPOSED_PROMPT_REFERENCES = asList(
            VICTIM_SURCHARGE_AMOUNT_PROMPT_REFERENCE, FINE_AMOUNT_PROMPT_REFERENCE,
            COMPENSATION_AMOUNT_PROMPT_REFERENCE, VEHICLE_EXCISE_BACK_DUTY_AMOUNT_PROMPT_REFERENCE,
            COSTS_TO_CROWN_PROSECUTION_SERVICE_AMOUNT_PROMPT_REFERENCE,
            AMOUNT_OF_COSTS_PROMPT_REFERENCE
    );

    public static class FinancialResult {
        private String totalAmountImposed;
        private String totalBalance;

        public String getTotalAmountImposed() {
            return totalAmountImposed;
        }

        public void setTotalAmountImposed(String totalAmountImposed) {
            this.totalAmountImposed = totalAmountImposed;
        }

        public String getTotalBalance() {
            return totalBalance;
        }

        public void setTotalBalance(String totalBalance) {
            this.totalBalance = totalBalance;
        }
    }

    // NOT using DecimalFormat in order to get
    // a high tolerance to unexpected characters
    private static long toPence(String str) {
        final StringBuilder sbIntegerPart = new StringBuilder();
        final StringBuilder sbDecimalPart = new StringBuilder();
        boolean foundDecimal = false;
        for (int done = 0; done < str.length(); done++) {
            char c = str.charAt(done);
            if (c == '.') {
                foundDecimal = true;
            } else if (Character.isDigit(c)) {
                if (foundDecimal) {
                    sbDecimalPart.append(c);
                } else {
                    sbIntegerPart.append(c);
                }
            } //ignoring non numerics
        }
        final long integerPart = sbIntegerPart.length() == 0 ? 0 : Long.parseLong(sbIntegerPart.toString());
        final long decimalPart = sbDecimalPart.length() == 0 ? 0 : Long.parseLong(sbDecimalPart.toString());
        return 100l * integerPart + decimalPart;
    }

    private String formatPenceAsPounds(final long pence) {
        long pounds = pence / 100;
        long pennies = pence % 100;
        return (new DecimalFormat("#0")).format(pounds) + "." + (new DecimalFormat("00").format(pennies));
    }


    public FinancialResult calculate(final Map<UUID, Prompt> id2PromptRef, final List<ResultLine> resultLines4Now) {
        FinancialResult result = new FinancialResult();
        long penceTotal =
                resultLines4Now.stream().flatMap(rl -> rl.getPrompts().stream())
                        .filter(getPromptPredicate(id2PromptRef))
                        .map(uk.gov.justice.core.courts.Prompt::getValue)
                        .map(str -> toPence(str))
                        .reduce(0l, (l1, l2) -> l1 + l2);

        long alreadyPaid =
                resultLines4Now.stream().flatMap(rl -> rl.getPrompts().stream())
                        .filter(p -> PromptTypesConstant.TOTAL_AMOUNT_ENFORCED_PROMPT_REFERENCE.equals(id2PromptRef.get(p.getId()).getReference())
                        )
                        .map(uk.gov.justice.core.courts.Prompt::getValue)
                        .map(str -> toPence(str))
                        .findFirst()
                        .orElse(0l);

        if (LOGGER.isInfoEnabled()) {
            String strPromptRefs = id2PromptRef.values().stream().map(p -> p.getId().toString() + "/" + p.getLabel() + "/" + p.getReference() + "/")
                    .collect(Collectors.joining(" - "));
            String strPromptValues = resultLines4Now.stream().flatMap(rl -> rl.getPrompts().stream()).map(p ->
                    p.getId() + "/" + p.getLabel() + "/" + p.getValue()).collect(Collectors.joining(" - "));
            LOGGER.info(String.format("prompt ref: %s  prompt values: %s", strPromptRefs, strPromptValues));
        }

        result.totalAmountImposed = formatPenceAsPounds(penceTotal);
        result.totalBalance = formatPenceAsPounds(penceTotal - alreadyPaid);
        return result;
    }

    private Predicate<uk.gov.justice.core.courts.Prompt> getPromptPredicate(final Map<UUID, Prompt> id2PromptRef) {
        return p -> {
            final UUID promptId = p.getId();
            final Prompt prompt = id2PromptRef.get(promptId);
            if (null == prompt) {
                LOGGER.error("Prompt with ID {} not found", promptId);
            }

            final String promptReference = prompt.getReference();
            if (StringUtils.isBlank(promptReference)) {
                LOGGER.debug("Prompt with ID {} has no reference", promptId);
            } else {
                LOGGER.debug("Prompt with ID {} has reference {}", promptId, promptReference);
            }
            return FINANCIAL_TOTAL_AMOUNT_IMPOSED_PROMPT_REFERENCES.contains(promptReference);
        };
    }

}
