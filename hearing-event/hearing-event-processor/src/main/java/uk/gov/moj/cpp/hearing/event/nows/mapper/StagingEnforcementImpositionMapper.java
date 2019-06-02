package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

class StagingEnforcementImpositionMapper extends AbstractStagingEnforcementMapper {

    private final Map<UUID, UUID> resultLineResultDefinitionIdMap;

    private final Map<UUID, String> sharedResultLineOffenceCodeMap;

    private final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts;

    private final Map<String, String> majorCreditorMap;

    StagingEnforcementImpositionMapper(final List<SharedResultLine> sharedResultLines,
                                       final Map<UUID, UUID> resultLineResultDefinitionIdMap,
                                       final Map<UUID, String> sharedResultLineOffenceCodeMap,
                                       final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts) {
        super(sharedResultLines);
        this.resultLineResultDefinitionIdMap = resultLineResultDefinitionIdMap;
        this.sharedResultLineOffenceCodeMap = sharedResultLineOffenceCodeMap;
        this.resultLineIdWithListOfPrompts = resultLineIdWithListOfPrompts;
        this.majorCreditorMap = new HashMap<>();
        this.majorCreditorMap.put("Transport for London", "TFL2");
        this.majorCreditorMap.put("Driver and Vehicle Licensing Agency", "DVL2");
        this.majorCreditorMap.put("Television Licensing Organisation", "TVL3");
    }

    List<Imposition> createImpositions() {

        final List<Imposition> impositionList = new ArrayList<>();

        resultLineIdWithListOfPrompts.forEach((sharedResultLineId, promptRefs) -> {

            final List<UUID> promptRefsList = resultLineIdWithListOfPrompts.get(sharedResultLineId).stream().map(Prompt::getId).collect(Collectors.toList());

            final UUID resultDefinitionId = resultLineResultDefinitionIdMap.get(sharedResultLineId);

            final String offenceCode = sharedResultLineOffenceCodeMap.get(sharedResultLineId);

            final ImpositionResultCode impositionResultCode = setImpositionResultCode(resultDefinitionId);

            if (nonNull(impositionResultCode)) {

                final BigDecimal impositionAmount = setImpositionAmount(impositionResultCode, promptRefsList);

                final String majorCreditor = ofNullable(setMajorCreditor(impositionResultCode, promptRefsList)).map(majorCreditorMap::get).orElse(null);

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

    private String setMajorCreditor(final ImpositionResultCode impositionResultCode, List<UUID> promptRefsList) {
//        only applies where impositionResultCode ==  impositionResultCode.FCOMP || impositionResultCode ==  impositionResultCode.FCOST.
//        In these cases when a resultCode is FCOMP, look for an associated prompt "Creditor name" (line 296).
//        When the resultCode is FCOST, look for an associated prompt  "Creditor name" (line 2061).

        if (impositionResultCode == ImpositionResultCode.FCOMP || impositionResultCode == ImpositionResultCode.FCOST) {
            return getPromptValue(promptRefsList, P_CREDITOR_NAME);
        }

        return null;
    }

    private BigDecimal setImpositionAmount(final ImpositionResultCode impositionResultCode, List<UUID> promptRefsList) {
        String impositionAmount = null;
        switch (impositionResultCode) {
            case FO:
                impositionAmount = getPromptValue(promptRefsList, P_AMOUNT_OF_FINE);
                break;
            case FCPC:
                impositionAmount = getPromptValue(promptRefsList, P_AMOUNT_OF_COSTS);
                break;
            case FVS:
                impositionAmount = getPromptValue(promptRefsList, P_AMOUNT_OF_SURCHARGE);
                break;
            case FVEBD:
                impositionAmount = getPromptValue(promptRefsList, P_AMOUNT_OF_BACK_DUTY);
                break;
            case FCOST:
                impositionAmount = getPromptValue(promptRefsList, P_AMOUNT_OF_COSTS);
                break;
            case FCOMP:
                impositionAmount = getPromptValue(promptRefsList, P_AMOUNT_OF_COMPENSATION);
                break;
            case FCC:
//                Will never be available in courts and therefore will never match
                break;
        }

        if(isNull(impositionAmount)) {
            return BigDecimal.ZERO;
        } else {
            //Remove '£' sign
            if(impositionAmount.startsWith("£")) {
                return new BigDecimal(impositionAmount.substring(1));
            }

            return new BigDecimal(impositionAmount);
        }
    }

    private ImpositionResultCode setImpositionResultCode(final UUID resultDefinitionId) {
        if(RD_FINE.equals(resultDefinitionId)) {
            return ImpositionResultCode.FO;
        } else if (RD_COSTSTOCROWNPROSECUTIONSERVICE.equals(resultDefinitionId)) {
            return ImpositionResultCode.FCPC;
        } else if (RD_SURCHARGE.equals(resultDefinitionId)) {
            return ImpositionResultCode.FVS;
        } else if (RD_VEHICLEEXCISEBACKDUTY.equals(resultDefinitionId)) {
            return ImpositionResultCode.FVEBD;
        } else if (RD_COSTS.equals(resultDefinitionId)) {
            return ImpositionResultCode.FCOST;
        } else if (RD_COMPENSATION.equals(resultDefinitionId)) {
            return ImpositionResultCode.FCOMP;
        } else {
            return null;
        }
    }
}
