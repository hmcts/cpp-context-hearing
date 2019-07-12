package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.EnforceFinancialImposition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EnforceFinancialImpositionMapper {

    private final CreateNowsRequest nowsRequest;

    private final UUID requestId;

    public EnforceFinancialImpositionMapper(final UUID requestId, final CreateNowsRequest nowsRequest) {
        this.nowsRequest = nowsRequest;
        this.requestId = requestId;
    }

    public EnforceFinancialImposition map() {

        requireNonNull(nowsRequest);

        requireNonNull(nowsRequest.getNows());

        final Hearing hearing = nowsRequest.getHearing();

        final List<SharedResultLine> sharedResultLines = nowsRequest.getSharedResultLines();

        final List<Now> nows = nowsRequest.getNows().stream().filter(now -> nonNull(now.getFinancialOrders())).collect(Collectors.toList());

        if (nows.isEmpty()) {
            throw new IllegalArgumentException("Not Found any financial order in the request.");
        }

        final Now now = nows.get(0); //At this point we should have only one financial Order

        final Defendant defendant = getDefendant(now.getDefendantId(), hearing);

        if (isNull(defendant)) {
            throw new IllegalArgumentException("Only One Order will be sent to enforcement at time.");
        }

        return createEnforceFinancialImposition(defendant, now, hearing, sharedResultLines);

    }

    private EnforceFinancialImposition createEnforceFinancialImposition(final Defendant defendant, final Now nows, final Hearing hearing, final List<SharedResultLine> sharedResultLines) {

        final String originator = "Courts";

        final Map<UUID, UUID> resultLineResultDefinitionIdMap = hearing.getTargets().stream()
                .filter(target -> target.getDefendantId().equals(defendant.getId()))
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toMap(ResultLine::getResultLineId, ResultLine::getResultDefinitionId));

        final Map<UUID, String> sharedResultLineOffenceCodeMap = mapSharedResultWithOffenceCode(
                defendant, sharedResultLines);

        final Map<UUID, List<Prompt>> resultLineIdWithListOfPrompts = hearing.getTargets().stream()
                .filter(target -> target.getDefendantId().equals(defendant.getId()))
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toMap(ResultLine::getResultLineId, ResultLine::getPrompts));

        final StagingEnforcementProsecutionMapper prosecutionMapper = new StagingEnforcementProsecutionMapper(defendant.getId(), hearing);

        final HearingLanguage hearingLanguage = isNull(hearing.getHearingLanguage()) ? HearingLanguage.ENGLISH : hearing.getHearingLanguage();

        final Set<String> prosecutionAuthorityCodes = Optional.of(hearing.getProsecutionCases())
                .get().stream().map(prosecutionCase -> prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityCode())
                .collect(Collectors.toSet());

        return EnforceFinancialImposition.enforceFinancialImposition()
                .withRequestId(requestId)
                .withOriginator(originator)
                .withImposingCourt(hearing.getCourtCentre().getId())
                .withProsecutionCaseReference(prosecutionMapper.getCaseReference())
                .withDefendant(new StagingEnforcementDefendantMapper(defendant, nows, hearingLanguage.toString(), sharedResultLines).createDefendant())
                .withImposition(new StagingEnforcementImpositionMapper(sharedResultLines, resultLineResultDefinitionIdMap, sharedResultLineOffenceCodeMap, resultLineIdWithListOfPrompts, prosecutionAuthorityCodes).createImpositions())
                .withParentGuardian(new StagingEnforcementParentGuardianMapper(defendant).map())
                .withCollectionOrder(new StagingEnforcementCollectionOrderMapper(sharedResultLines, nows, resultLineResultDefinitionIdMap, resultLineIdWithListOfPrompts).map())
                .withPlea(new StagingEnforcementPleaMapper(defendant).map())
                .withEmployer(new StagingEnforcementEmployerMapper(nows.getFinancialOrders().getEmployerOrganisation()).map())
                //TODO: Set Null By ATCM.  Does Courts need to support minor creditors in increment 2.4?  Check with Helen Locke/BPOs.  If required check with Helen Locke regarding the results mappings
                .withMinorCreditor(new StagingEnforcementMinorCreditorMapper().map())//TODO: we do not differentiate major from minor -> prompt 296.  BPO Question concerning how we will deal with minor creditors and the expectation of GOB
                .withPaymentTerms(new StagingEnforcementPaymentTermsMapper(sharedResultLines, resultLineResultDefinitionIdMap, resultLineIdWithListOfPrompts).map())
                .withProsecutionAuthorityCode(prosecutionMapper.getAuthorityCode())
                .build();
    }

    private Map<UUID, String> mapSharedResultWithOffenceCode(final Defendant defendant, final List<SharedResultLine> sharedResultLines) {
        final Map<UUID, UUID> sharedResultLineIdOffenceIdMap = sharedResultLines.stream()
                .filter(s -> s.getDefendantId().equals(defendant.getId()))
                .collect(Collectors.toMap(SharedResultLine::getId, SharedResultLine::getOffenceId));

        final Map<UUID, String> offenceIdOffenceCodeMap = defendant.getOffences().stream()
                .collect(Collectors.toMap(Offence::getId, Offence::getOffenceCode));

        final Map<UUID, String> sharedResultLineOffenceCodeMap = new HashMap<>();

        sharedResultLineIdOffenceIdMap.forEach((sharedResultLineId, offenceId) ->
                sharedResultLineOffenceCodeMap.put(sharedResultLineId, offenceIdOffenceCodeMap.get(offenceId)));

        return sharedResultLineOffenceCodeMap;
    }

    private Defendant getDefendant(final UUID defendantId, final Hearing hearing) {

        requireNonNull(hearing);

        return hearing.getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> defendant.getId().equals(defendantId))
                .findFirst()
                .orElse(null);
    }
}
