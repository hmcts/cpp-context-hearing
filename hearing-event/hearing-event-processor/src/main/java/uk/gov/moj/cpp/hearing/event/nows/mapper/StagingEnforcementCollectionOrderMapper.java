package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.json.schemas.staging.ReserveTermsType.INSTALMENTS_ONLY;
import static uk.gov.justice.json.schemas.staging.ReserveTermsType.LUMP_SUM;
import static uk.gov.justice.json.schemas.staging.ReserveTermsType.LUMP_SUM_PLUS_INSTALMENTS;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_FOURTEEN;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_START_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_LUMP_SUM_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_LUMP_SUM_PAY_WITHIN;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAYMENT_FREQUENCY;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_TWOEIGHT;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_ABCD;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_AEOC;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_COLLECTIONORDER;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_RINSTL;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_RLSUM;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_RLSUMI;

import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.CollectionOrder;
import uk.gov.justice.json.schemas.staging.Frequency;
import uk.gov.justice.json.schemas.staging.Instalments;
import uk.gov.justice.json.schemas.staging.LumpSum;
import uk.gov.justice.json.schemas.staging.ReserveTerms;
import uk.gov.justice.json.schemas.staging.ReserveTermsType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1135" , "squid:S1871"})
public class StagingEnforcementCollectionOrderMapper extends AbstractStagingEnforcementMapper {

    private final Map<UUID, UUID> resultLineResultDefinitionIdMap;

    private final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts;

    private final Now now;

    StagingEnforcementCollectionOrderMapper(final List<SharedResultLine> sharedResultLines,
                                            final Now now,
                                            final Map<UUID, UUID> resultLineResultDefinitionIdMap,
                                            final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts) {
        super(sharedResultLines);
        this.now = now;
        this.resultLineResultDefinitionIdMap = resultLineResultDefinitionIdMap;
        this.resultLineIdWithListOfPrompts = resultLineIdWithListOfPrompts;
    }

    public CollectionOrder map() {

        boolean isApplicationForBenefitsDeduction = false;

        boolean isAttachmentOfEarnings = false;

        ReserveTermsType reserveTermsType = null;

        LumpSum lumpSum = null;

        Instalments instalments = null;

        boolean collectionOrderMade = false;

        final String totalBalance = nonNull(now.getFinancialOrders()) ? now.getFinancialOrders().getTotalBalance() : "0";

        for (final Map.Entry<UUID, List<Prompt>> entry : resultLineIdWithListOfPrompts.entrySet()) {

            final UUID key = entry.getKey();

            final List<UUID> promptRefsList = entry.getValue().stream().map(Prompt::getId).collect(Collectors.toList());

            final UUID resultDefinitionId = resultLineResultDefinitionIdMap.get(key);

            if(resultDefinitionId.equals(RD_COLLECTIONORDER)) {
                collectionOrderMade = true;
            }

            if (resultDefinitionId.equals(RD_ABCD)) {
                isApplicationForBenefitsDeduction = true;
            }

            if (resultDefinitionId.equals(RD_AEOC)) {
                isAttachmentOfEarnings = true;
            }

            if(isNull(reserveTermsType)) {
                reserveTermsType = setReserveTermsType(resultDefinitionId);

                if (nonNull(reserveTermsType)) {
                    lumpSum = setLumpSum(reserveTermsType, promptRefsList, totalBalance);
                    instalments = setInstalments(reserveTermsType, promptRefsList);
                }
            }
        }

        ReserveTerms reserveTerms = null;

        if(nonNull(reserveTermsType)) {
            reserveTerms = ReserveTerms.reserveTerms()
                    .withInstalments(instalments)
                    .withLumpSum(lumpSum)
                    .withReserveTermsType(reserveTermsType)
                    .build();
        }

        return CollectionOrder.collectionOrder()
                .withIsApplicationForBenefitsDeduction(isApplicationForBenefitsDeduction)
                .withIsAttachmentOfEarnings(isAttachmentOfEarnings)
                .withIsCollectionOrderMade(collectionOrderMade)
                .withReserveTerms(reserveTerms)
                .build();
    }

    private Instalments setInstalments(final ReserveTermsType reserveTermsType, final List<UUID> promptRefsList) {

//        Only applies where the reserve terms type is RINSTL or RLSUMI i.e. not for RLSUM
//        When RINSTL - look for an associated prompt "Payment frequency" (line 2041)
//        When RLSUMI- look for an associated prompt "Payment frequency" (line 2039)

        if (reserveTermsType != LUMP_SUM) {
            final BigDecimal amount = getInstalmentAmount(promptRefsList);
            final Frequency frequency = getFrequency(reserveTermsType, promptRefsList);
            final LocalDate localDate = getInstalmentStartDate(reserveTermsType, promptRefsList);
            if(nonNull(amount)) {
                return Instalments.instalments()
                        .withAmount(amount)
                        .withFrequency(frequency)
                        .withStartDate(localDate)
                        .build();
            }
        }

        return null;
    }

    private BigDecimal getInstalmentAmount(final List<UUID> promptRefsList) {

        String promptValue = getPromptValue(promptRefsList, P_INSTALMENT_AMOUNT);

        return isNull(promptValue) ? null : new BigDecimal(promptValue);
    }

    private LocalDate getInstalmentStartDate(final ReserveTermsType reserveTermsType, final List<UUID> promptRefsList) {
        if (reserveTermsType != LUMP_SUM) {
            final String instalmentStartDate = getPromptValue(promptRefsList, P_INSTALMENT_START_DATE);
            if (nonNull(instalmentStartDate)) {
                return LocalDate.parse(instalmentStartDate);
            }
        }
        return null;
    }

    private Frequency getFrequency(final ReserveTermsType reserveTermsType, final List<UUID> promptRefsList) {
        if (reserveTermsType != LUMP_SUM) {
            final String promptType = getPromptValue(promptRefsList, P_PAYMENT_FREQUENCY);
            if (nonNull(promptType)) {
                return Frequency.valueOf(promptType.toUpperCase());
            }
        }
        return null;
    }

    private LumpSum setLumpSum(ReserveTermsType reserveTermsType, final List<UUID> promptRefsList, final String totalBalance) {
        if (reserveTermsType != INSTALMENTS_ONLY) {
            final BigDecimal reserveTermsAmount = getReserveTermsAmount(reserveTermsType, promptRefsList, totalBalance);
            final Integer withInDays = getReserveTermsWithinDays(reserveTermsType, promptRefsList);
            if(nonNull(reserveTermsAmount)) {
                return LumpSum.lumpSum()
                        .withAmount(reserveTermsAmount)
                        .withWithinDays(withInDays)
                        .build();
            }
        }
        return null;
    }

    private Integer getReserveTermsWithinDays(final ReserveTermsType reserveTermsType, final List<UUID> promptRefsList) {

        String withinDays = null;

        if (reserveTermsType == LUMP_SUM) {

            final String withinDaysFixList = getPromptValue(promptRefsList, P_LUMP_SUM_PAY_WITHIN);

            if (nonNull(withinDaysFixList)) {
                if (withinDaysFixList.equals(P_FOURTEEN)) {
                    withinDays = "14";
                } else if (withinDaysFixList.equals(P_TWOEIGHT)) {
                    withinDays = "28";
                }
            }

        } else if (reserveTermsType == LUMP_SUM_PLUS_INSTALMENTS) {

            final String withinDaysFixList = getPromptValue(promptRefsList, P_LUMP_SUM_PAY_WITHIN);

            if (nonNull(withinDaysFixList)) {
                if (withinDaysFixList.equals(P_FOURTEEN)) {
                    withinDays = "14";
                } else if (withinDaysFixList.equals(P_TWOEIGHT)) {
                    withinDays = "28";
                }
            }
        }

        if (nonNull(withinDays)) {
            return new Integer(withinDays);
        }

        return null;
    }

    private BigDecimal getReserveTermsAmount(ReserveTermsType reserveTermsType, final List<UUID> promptRefsList, final String totalBalance) {

        String outstandingBalance = null;

        if (reserveTermsType == LUMP_SUM) {

            outstandingBalance = totalBalance;

        } else if (reserveTermsType == LUMP_SUM_PLUS_INSTALMENTS) {

            outstandingBalance = getPromptValue(promptRefsList, P_LUMP_SUM_AMOUNT);
        }

        return nonNull(outstandingBalance) ? new BigDecimal(outstandingBalance) : null;
    }

    private ReserveTermsType setReserveTermsType(final UUID resultDefinitionId) {
//          Match the following result definition short codes
//          Reserve Terms Lump sum - RLSUM (definition line 536),
//          Reserve Terms Lump sum plus instalments - RLSUMI (definition line 537),
//          Reserve Terms Instalments only - RINSTL (definition line 538)

        if(RD_RLSUM.equals(resultDefinitionId)) {
            return LUMP_SUM;
        } else if (RD_RLSUMI.equals(resultDefinitionId)) {
            return LUMP_SUM_PLUS_INSTALMENTS;
        } else if(RD_RINSTL.equals(resultDefinitionId)) {
            return INSTALMENTS_ONLY;
        }

        return null;
    }
}