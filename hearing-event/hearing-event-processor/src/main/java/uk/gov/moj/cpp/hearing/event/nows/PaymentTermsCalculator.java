package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Objects.nonNull;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_DAYS_IN_DEFAULT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_START_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_LUMP_SUM_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAYMENT_FREQUENCY;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAY_BY_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_INSTL;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_LUMSI;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_PDATE;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaymentTermsCalculator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-YYYY");

    private static final String PAY_BY_DATE = "Date to pay in full by %s";

    private static final String LUMP_SUM_PLUS_INSTALMENTS = "Lump sum amount £%s Instalment amount £%s Payment frequency %s Instalment start date %s";

    private static final String INSTALMENTS_ONLY = "Instalment amount £%s Payment frequency %s Instalment start date %s";

    private static final String OPTIONAL_TEXT = " Number of days in default %s";

    private static final DateTimeFormatter FORMATTER_WELSH = DateTimeFormatter.ofPattern("dd-MM-YYYY");

    private static final String PAY_BY_DATE_WELSH = "Rhaid talu'r swm llawn erbyn %s";

    private static final String LUMP_SUM_PLUS_INSTALMENTS_WELSH = "Cyfanswm y lwmp swm £%s Swm y rhandaliad £%s Amlder y taliadau %s Dyddiad cychwyn talu rhandaliadau %s";

    private static final String INSTALMENTS_ONLY_WELSH = "Swm y rhandaliad £%s Amlder y taliadau %s Dyddiad cychwyn talu rhandaliadau %s";

    private static final String OPTIONAL_TEXT_WELSH = " Nifer y diwrnodau lle erys y ddyled heb ei thalu %s";


    public String calculatePaymentTerms(final Map<UUID, Prompt> id2PromptRef, final List<ResultLine> resultLines4Now, final Boolean isWelsh) {

        final boolean hasPayDateResult = resultLines4Now.stream().anyMatch(resultLine -> resultLine.getResultDefinitionId().equals(RD_PDATE));

        final boolean hasLumpSumPlusInstalmentsResult = resultLines4Now.stream().anyMatch(resultLine -> resultLine.getResultDefinitionId().equals(RD_LUMSI));

        final boolean hasInstalmentsOnlyResult = resultLines4Now.stream().anyMatch(resultLine -> resultLine.getResultDefinitionId().equals(RD_INSTL));

        final boolean hasOptionalText = nonNull(getPromptValue(id2PromptRef, resultLines4Now, P_DAYS_IN_DEFAULT));

        if (isWelsh) {
            return extractPromptValueWelsh(id2PromptRef, resultLines4Now, hasPayDateResult, hasLumpSumPlusInstalmentsResult, hasInstalmentsOnlyResult, hasOptionalText);
        } else {
            return extractPromptValue(id2PromptRef, resultLines4Now, hasPayDateResult, hasLumpSumPlusInstalmentsResult, hasInstalmentsOnlyResult, hasOptionalText);
        }
    }


    private String extractPromptValue(final Map<UUID, Prompt> id2PromptRef, final List<ResultLine> resultLines4Now, final boolean hasPayDateResult, final boolean hasLumpSumPlusInstalmentsResult, final boolean hasInstalmentsOnlyResult, final boolean hasOptionalText) {
        if (hasPayDateResult) {
            if (hasOptionalText) {
                return String.format(PAY_BY_DATE + OPTIONAL_TEXT, LocalDate.parse(getPromptValue(id2PromptRef, resultLines4Now, P_PAY_BY_DATE)).format(FORMATTER),
                        getPromptValue(id2PromptRef, resultLines4Now, P_DAYS_IN_DEFAULT));
            } else {
                return String.format(PAY_BY_DATE, LocalDate.parse(getPromptValue(id2PromptRef, resultLines4Now, P_PAY_BY_DATE)).format(FORMATTER));
            }
        }

        if (hasLumpSumPlusInstalmentsResult) {
            if (hasOptionalText) {
                return String.format(LUMP_SUM_PLUS_INSTALMENTS + OPTIONAL_TEXT,
                        getPromptValue(id2PromptRef, resultLines4Now, P_LUMP_SUM_AMOUNT),
                        getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValue(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER),
                        getPromptValue(id2PromptRef, resultLines4Now, P_DAYS_IN_DEFAULT));
            } else {
                return String.format(LUMP_SUM_PLUS_INSTALMENTS,
                        getPromptValue(id2PromptRef, resultLines4Now, P_LUMP_SUM_AMOUNT),
                        getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValue(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER)
                );
            }
        }

        if (hasInstalmentsOnlyResult) {
            if (hasOptionalText) {
                return String.format(INSTALMENTS_ONLY + OPTIONAL_TEXT,
                        getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValue(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER),
                        getPromptValue(id2PromptRef, resultLines4Now, P_DAYS_IN_DEFAULT)
                );
            } else {
                return String.format(INSTALMENTS_ONLY,
                        getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValue(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValue(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER)
                );
            }
        }

        return null;
    }

    private String getPromptValue(final Map<UUID, Prompt> id2PromptRef, final List<ResultLine> resultLines4Now, String promptReference) {
        return resultLines4Now.stream().flatMap(rl -> rl.getPrompts().stream())
                .filter(p -> promptReference.equals(id2PromptRef.get(p.getId()).getReference())
                )
                .map(uk.gov.justice.core.courts.Prompt::getValue)
                .findFirst()
                .orElse(null);
    }

    private String extractPromptValueWelsh(final Map<UUID, Prompt> id2PromptRef, final List<ResultLine> resultLines4Now, final boolean hasPayDateResult, final boolean hasLumpSumPlusInstalmentsResult, final boolean hasInstalmentsOnlyResult, final boolean hasOptionalText) {
        if (hasPayDateResult) {
            if (hasOptionalText) {
                return String.format(PAY_BY_DATE_WELSH + OPTIONAL_TEXT_WELSH, LocalDate.parse(getPromptValueWelsh(id2PromptRef, resultLines4Now, P_PAY_BY_DATE)).format(FORMATTER_WELSH),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_DAYS_IN_DEFAULT));
            } else {
                return String.format(PAY_BY_DATE_WELSH, LocalDate.parse(getPromptValueWelsh(id2PromptRef, resultLines4Now, P_PAY_BY_DATE)).format(FORMATTER_WELSH));
            }
        }

        if (hasLumpSumPlusInstalmentsResult) {
            if (hasOptionalText) {
                return String.format(LUMP_SUM_PLUS_INSTALMENTS_WELSH + OPTIONAL_TEXT_WELSH,
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_LUMP_SUM_AMOUNT),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER_WELSH),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_DAYS_IN_DEFAULT));
            } else {
                return String.format(LUMP_SUM_PLUS_INSTALMENTS_WELSH,
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_LUMP_SUM_AMOUNT),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER_WELSH)
                );
            }
        }

        if (hasInstalmentsOnlyResult) {
            if (hasOptionalText) {
                return String.format(INSTALMENTS_ONLY_WELSH + OPTIONAL_TEXT_WELSH,
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER_WELSH),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_DAYS_IN_DEFAULT)
                );
            } else {
                return String.format(INSTALMENTS_ONLY_WELSH,
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_AMOUNT),
                        getPromptValueWelsh(id2PromptRef, resultLines4Now, P_PAYMENT_FREQUENCY),
                        LocalDate.parse(getPromptValueWelsh(id2PromptRef, resultLines4Now, P_INSTALMENT_START_DATE)).format(FORMATTER_WELSH)
                );
            }
        }

        return null;
    }

    private String getPromptValueWelsh(final Map<UUID, Prompt> id2PromptRef, final List<ResultLine> resultLines4Now, String promptReference) {
        return resultLines4Now.stream().flatMap(rl -> rl.getPrompts().stream())
                .filter(p -> promptReference.equals(id2PromptRef.get(p.getId()).getReference())
                )
                .map(p -> (nonNull(p.getWelshValue()) &&!p.getWelshValue().isEmpty()) ? p.getWelshValue() : p.getValue())
                .findFirst()
                .orElse(null);
    }

}
