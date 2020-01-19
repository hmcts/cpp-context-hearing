package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.justice.json.schemas.staging.ImpositionResultCode.FCOMP;
import static uk.gov.justice.json.schemas.staging.ImpositionResultCode.FCOST;
import static uk.gov.justice.json.schemas.staging.ImpositionResultCode.FCPC;
import static uk.gov.justice.json.schemas.staging.ImpositionResultCode.FO;
import static uk.gov.justice.json.schemas.staging.ImpositionResultCode.FVEBD;
import static uk.gov.justice.json.schemas.staging.ImpositionResultCode.FVS;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_AMOUNT_OF_BACK_DUTY;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_AMOUNT_OF_COMPENSATION;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_AMOUNT_OF_COSTS;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_AMOUNT_OF_FINE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_AMOUNT_OF_SURCHARGE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_CREDITOR_NAME;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_COMPENSATION;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_COSTS;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_COSTSTOCROWNPROSECUTIONSERVICE;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_FINE;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_SURCHARGE;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_VEHICLEEXCISEBACKDUTY;

import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.Imposition;
import uk.gov.justice.json.schemas.staging.ImpositionResultCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StagingEnforcementImpositionMapper extends AbstractStagingEnforcementMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEnforcementImpositionMapper.class.getName());

    private final Map<UUID, UUID> resultLineResultDefinitionIdMap;

    private final Map<UUID, String> sharedResultLineOffenceCodeMap;

    private final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts;

    private final Map<String, String> majorCreditorMap;

    private final Set<String> prosecutionAuthorityCodes;

    private static final EnumMap<ImpositionResultCode, String> impositionAmountPromptReferences = getImpositionAmountPromptReferences();

    StagingEnforcementImpositionMapper(final List<SharedResultLine> sharedResultLines,
                                       final Map<UUID, UUID> resultLineResultDefinitionIdMap,
                                       final Map<UUID, String> sharedResultLineOffenceCodeMap,
                                       final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts,
                                       final Set<String> prosecutionAuthorityCodes) {
        super(sharedResultLines);
        this.resultLineResultDefinitionIdMap = resultLineResultDefinitionIdMap;
        this.sharedResultLineOffenceCodeMap = sharedResultLineOffenceCodeMap;
        this.resultLineIdWithListOfPrompts = resultLineIdWithListOfPrompts;
        this.prosecutionAuthorityCodes = prosecutionAuthorityCodes;
        this.majorCreditorMap = new HashMap<>();
        this.majorCreditorMap.put("Transport for London", "TFL2");
        this.majorCreditorMap.put("Driver and Vehicle Licensing Agency", "DVL2");
        this.majorCreditorMap.put("Television Licensing Organisation", "TVL3");
        this.majorCreditorMap.put("TFL", "TFL2");
        this.majorCreditorMap.put("TVL", "TVL3");
    }

    List<Imposition> createImpositions() {

        final List<Imposition> impositionList = new ArrayList<>();

        resultLineIdWithListOfPrompts.forEach((sharedResultLineId, promptRefs) -> {

            final List<UUID> promptRefsList = resultLineIdWithListOfPrompts.get(sharedResultLineId).stream().map(Prompt::getId).collect(Collectors.toList());

            final UUID resultDefinitionId = resultLineResultDefinitionIdMap.get(sharedResultLineId);

            final String offenceCode = sharedResultLineOffenceCodeMap.get(sharedResultLineId);

            LOGGER.info("mapped offenceCode {} ", offenceCode);

            final ImpositionResultCode impositionResultCode = setImpositionResultCode(resultDefinitionId);

            if (nonNull(impositionResultCode)) {

                final BigDecimal impositionAmount = setImpositionAmount(impositionResultCode, promptRefsList, sharedResultLineId);

                final String majorCreditor = getMajorCreditor(impositionResultCode, promptRefsList);

                /*
                 * Note : prosecutionAuthorityId - Not required by courts as in courts any major creditor may be provided.
                 * This is provided in the design for ATCM.
                 */
                impositionList.add(Imposition.imposition()
                        .withImpositionAmount(impositionAmount)
                        .withImpositionResultCode(impositionResultCode)
                        .withMajorCreditor(majorCreditor)
                        .withOffenceCode(offenceCode)
                        .withProsecutionAuthorityId(null)
                        .build());
            }
        });

        return impositionList;
    }

    private String getMajorCreditor(final ImpositionResultCode impositionResultCode, final List<UUID> promptRefsList) {
        return ofNullable(getMajorCreditorFromPrompt(impositionResultCode, promptRefsList)).map(majorCreditorMap::get).orElse(null);
    }

    private String getMajorCreditorFromPrompt(final ImpositionResultCode impositionResultCode, List<UUID> promptRefsList) {
//        only applies where impositionResultCode ==  impositionResultCode.FCOMP || impositionResultCode ==  impositionResultCode.FCOST.
//        In these cases when a resultCode is FCOMP, look for an associated prompt "Creditor name" (line 296).
//        When the resultCode is FCOST, look for an associated prompt  "Creditor name" (line 2061).

        String majorCreditor = null;

        if (impositionResultCode == FCOMP || impositionResultCode == ImpositionResultCode.FCOST) {

            majorCreditor = getPromptValue(promptRefsList, P_CREDITOR_NAME);

            if (isNull(majorCreditor)) {
                final Optional<String> firstItem = prosecutionAuthorityCodes.stream().findFirst();
                if (firstItem.isPresent()) {
                    majorCreditor = firstItem.get();
                }
            }
        }

        return majorCreditor;
    }

    private static final EnumMap<ImpositionResultCode, String> getImpositionAmountPromptReferences() {
        final EnumMap<ImpositionResultCode, String> result = new EnumMap<>(ImpositionResultCode.class);
        result.put(FO, P_AMOUNT_OF_FINE);
        result.put(FCPC, P_AMOUNT_OF_COSTS);
        result.put(FVS, P_AMOUNT_OF_SURCHARGE);
        result.put(FVEBD, P_AMOUNT_OF_BACK_DUTY);
        result.put(FCOST, P_AMOUNT_OF_COSTS);
        result.put(FCOMP, P_AMOUNT_OF_COMPENSATION);
        // FCC, P_AMOUNT_OF_COMPENSATION never be available in courts and therefore will never match
        return result;

    }

    private BigDecimal setImpositionAmount(final ImpositionResultCode impositionResultCode, List<UUID> promptRefsList, UUID sharedResultLineId) {
        String impositionAmount = null;
        if (impositionAmountPromptReferences.containsKey(impositionResultCode)) {
            impositionAmount = getPromptValue(promptRefsList, impositionAmountPromptReferences.get(impositionResultCode), sharedResultLineId);

        }

        if(isNull(impositionAmount)) {
            return BigDecimal.ZERO;
        } else {
            //Remove '£' sign
            if(impositionAmount.startsWith("£")) {
                return getStringAsDecimal(impositionAmount.substring(1));
            }

            return getStringAsDecimal(impositionAmount);
        }
    }

    private ImpositionResultCode setImpositionResultCode(final UUID resultDefinitionId) {
        if(RD_FINE.equals(resultDefinitionId)) {
            return FO;
        } else if (RD_COSTSTOCROWNPROSECUTIONSERVICE.equals(resultDefinitionId)) {
            return FCPC;
        } else if (RD_SURCHARGE.equals(resultDefinitionId)) {
            return FVS;
        } else if (RD_VEHICLEEXCISEBACKDUTY.equals(resultDefinitionId)) {
            return FVEBD;
        } else if (RD_COSTS.equals(resultDefinitionId)) {
            return ImpositionResultCode.FCOST;
        } else if (RD_COMPENSATION.equals(resultDefinitionId)) {
            return FCOMP;
        } else {
            return null;
        }
    }
}
