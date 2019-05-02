package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_FORTNIGHTLY;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_START_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_LUMP_SUM_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_MONTHLY;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAYMENT_FREQUENCY;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAY_BY_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_WEEKLY;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_INSTL;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_LUMSI;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_PDATE;

import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.PaymentTerms;
import uk.gov.justice.json.schemas.staging.PaymentTermsType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1135"})
public class StagingEnforcementPaymentTermsMapper extends AbstractStagingEnforcementMapper {

    public static final String EMPTY_STRING = "";
    public static final String REGEX_ONLY_NUMBERS = "[^0-9]";
    public static final String INCOMING_PROMPT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String OUTGOING_PROMPT_DATE_FORMAT = "dd MMM yyyy";
    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEnforcementPaymentTermsMapper.class.getName());
    private final Map<UUID, UUID> resultLineResultDefinitionIdMap;

    private final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts;

    StagingEnforcementPaymentTermsMapper(final List<SharedResultLine> sharedResultLines,
                                         final Map<UUID, UUID> resultLineResultDefinitionIdMap,
                                         final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts) {
        super(sharedResultLines);
        this.resultLineResultDefinitionIdMap = resultLineResultDefinitionIdMap;
        this.resultLineIdWithListOfPrompts = resultLineIdWithListOfPrompts;
    }

    public PaymentTerms map() {

        List<UUID> promptRefs = null;

        PaymentTermsType paymentTermsType = null;

        UUID resultDefinitionId = null;

        for (final Map.Entry<UUID, List<Prompt>> entry : resultLineIdWithListOfPrompts.entrySet()) {
            final UUID sharedResultLineId = entry.getKey();
            resultDefinitionId = resultLineResultDefinitionIdMap.get(sharedResultLineId);
            final List<UUID> promptRefsList = resultLineIdWithListOfPrompts.get(sharedResultLineId).stream().map(Prompt::getId).collect(Collectors.toList());
            paymentTermsType = getPaymentTermsType(resultDefinitionId, promptRefsList);
            if (nonNull(paymentTermsType)) {
                promptRefs = promptRefsList;
                break;
            }
        }

        if (nonNull(resultDefinitionId) && nonNull(paymentTermsType)) {
            return PaymentTerms.paymentTerms()
                    .withPaymentTermsType(paymentTermsType)
                    .withPayByDate(getPayByDate(paymentTermsType, promptRefs))
                    .withLumpSumAmount(getLumpSumAmount(resultDefinitionId, promptRefs))
                    .withDefaultDaysInJail(null) //TODO: Not supported in increment 2.4.
                    .withInstalmentAmount(getInstalmentAmount(resultDefinitionId, promptRefs))
                    .withInstalmentStartDate(getInstalmentStartDate(resultDefinitionId, promptRefs))
                    .withParentGuardianToPay(null) //TODO: Not supported in increment 2.4.
                    .build();
        }

        //default payment type is Instalments only with instalment amount 20, monthly, instalment start date 3 months from decision date
        return PaymentTerms.paymentTerms()
                .withPaymentTermsType(PaymentTermsType.MONTHLY_INSTALMENTS)
                .withInstalmentAmount(new BigDecimal(20))
                .withInstalmentStartDate(LocalDate.now().plusDays(90))
                .build();

    }

    private LocalDate getInstalmentStartDate(final UUID resultDefinitionId, final List<UUID> promptRefs) {
        String promptValue = null;
        if (resultDefinitionId.equals(RD_LUMSI) || resultDefinitionId.equals(RD_INSTL)) {
            promptValue = getPromptValue(promptRefs, P_INSTALMENT_START_DATE);
        }

        if (nonNull(promptValue)) {
            return LocalDate.parse(reformatDateIfRequired(promptValue));
        }

        return null;
    }

    private BigDecimal getInstalmentAmount(final UUID resultDefinitionId, final List<UUID> promptRefs) {
        String promptValue = null;
        if (resultDefinitionId.equals(RD_LUMSI) ||
                resultDefinitionId.equals(RD_INSTL)) {
            promptValue = getPromptValue(promptRefs, P_INSTALMENT_AMOUNT);
        }
        return isNull(promptValue) ? null : getStringAsDecimal(promptValue);
    }

    private BigDecimal getLumpSumAmount(final UUID resultDefinitionId, final List<UUID> promptRefs) {
        String promptValue = null;
        if (resultDefinitionId.equals(RD_LUMSI)) {
            promptValue = getPromptValue(promptRefs, P_LUMP_SUM_AMOUNT);
        }

        return isNull(promptValue) ? null : getStringAsDecimal(promptValue);
    }

    private LocalDate getPayByDate(final PaymentTermsType paymentTermsType, final List<UUID> promptRefs) {
        if (paymentTermsType == PaymentTermsType.BY_DATE) {
            final String promptValue = getPromptValue(promptRefs, P_PAY_BY_DATE);
            return LocalDate.parse(reformatDateIfRequired(promptValue));
        }
        return null;
    }

    private PaymentTermsType getPaymentTermsType(final UUID resultDefinitionId, final List<UUID> promptRefsList) {
        if (RD_PDATE.equals(resultDefinitionId)) {
            return PaymentTermsType.BY_DATE;
        } else if (RD_INSTL.equals(resultDefinitionId)) {
            return getPaymentTermsType(promptRefsList);
        } else if (RD_LUMSI.equals(resultDefinitionId)) {
            return getLumpSumPaymentTermsType(promptRefsList);
        } else {
            return null;
        }
    }

    private PaymentTermsType getLumpSumPaymentTermsType(final List<UUID> promptRefsList) {

        final String promptValue = getPromptValue(promptRefsList, P_PAYMENT_FREQUENCY);

        if (nonNull(promptValue)) {

            switch (promptValue) {
                case P_WEEKLY:
                    return PaymentTermsType.WEEKLY_INSTALMENTS_THEN_LUMP_SUM;
                case P_FORTNIGHTLY:
                    return PaymentTermsType.FORTNIGHTLY_INSTALMENTS_THEN_LUMP_SUM;
                case P_MONTHLY:
                    return PaymentTermsType.MONTHLY_INSTALMENTS_THEN_LUMP_SUM;
                default:
                    return null;
            }
        }

        return null;
    }

    private PaymentTermsType getPaymentTermsType(final List<UUID> promptRefsList) {

        final String promptValue = getPromptValue(promptRefsList, P_PAYMENT_FREQUENCY);

        if (nonNull(promptValue)) {

            switch (promptValue) {
                case P_WEEKLY:
                    return PaymentTermsType.WEEKLY_INSTALMENTS;
                case P_FORTNIGHTLY:
                    return PaymentTermsType.FORTNIGHTLY_INSTALMENTS;
                case P_MONTHLY:
                    return PaymentTermsType.MONTHLY_INSTALMENTS;
                default:
                    return null;
            }
        }

        return null;
    }

    private BigDecimal getStringAsDecimal(final String value) {
        return new BigDecimal(value.replaceAll(REGEX_ONLY_NUMBERS, EMPTY_STRING));
    }

    private String reformatDateIfRequired(final String value) {
        String originalValue = value;
        if (isRequired(OUTGOING_PROMPT_DATE_FORMAT, value, Locale.ENGLISH)) {
            originalValue = LocalDate.parse(value, DateTimeFormatter.ofPattern(OUTGOING_PROMPT_DATE_FORMAT)).format((DateTimeFormatter.ofPattern(INCOMING_PROMPT_DATE_FORMAT)));
        }
        return originalValue;
    }

    private boolean isRequired(final String format,final String value,final Locale locale) {
        final DateTimeFormatter fomatter = DateTimeFormatter.ofPattern(format, locale);
        boolean isNotEqualToOutgoingFormat = false;
        try {
            final LocalDate localDate = LocalDate.parse(value, fomatter);
            final String result = localDate.format(fomatter);
            isNotEqualToOutgoingFormat = result.equals(value);
        } catch (DateTimeParseException exp) {
            LOGGER.error(String.format("Invalid date - %s ", value), exp);
        }
        return isNotEqualToOutgoingFormat;
    }
}