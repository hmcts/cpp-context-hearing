package uk.gov.moj.cpp.hearing.event;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID;

import uk.gov.justice.core.courts.Category;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.AdjournHearingDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.NowsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateDefendantWithApplicationDetailsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.exception.ResultDefinitionNotFoundException;
import uk.gov.moj.cpp.hearing.event.nows.NowsGenerator;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.ResultsSharedFilter;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
@SuppressWarnings({"squid:S1188"})
public class PublishResultsEventProcessor {

    protected static final List<String> dismissedResultList = Collections.unmodifiableList(Arrays.asList("14d66587-8fbe-424f-a369-b1144f1684e3", "f8bd4d1f-1467-4903-b1e6-d2249ccc8c25", "8542b0d9-27f0-4df3-a4a3-0ac0a85c33ad"));
    protected static final List<String> withDrawnResultList = Collections.unmodifiableList(Arrays.asList("6feb0f2e-8d1e-40c7-af2c-05b28c69e5fc", "eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8",
            "c8326b9e-56eb-406c-b74b-9f90c772b657", "eaecff82-32da-4cc1-b530-b55195485cc7", "4d5f25a5-9102-472f-a2da-c58d1eeb9c93"));
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsEventProcessor.class.getName());
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private NowsGenerator nowsGenerator;

    @Inject
    private NowsDelegate nowsDelegate;

    @Inject
    private UpdateDefendantWithApplicationDetailsDelegate updateDefendantWithApplicationDetailsDelegate;

    @Inject
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    @Inject
    private PublishResultsDelegate publishResultsDelegate;

    @Inject
    private AdjournHearingDelegate adjournHearingDelegate;

    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Inject
    private ReferenceDataService referenceDataService;

    @Inject
    private ResultsSharedFilter resultsSharedFilter;

    @Handles("hearing.results-shared")
    public void resultsShared(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.results-shared event received {}", event.toObfuscatedDebugString());
        }

        final ResultsShared resultsShared = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);
        final Hearing hearing = resultsShared.getHearing();
        final List<CourtApplication> courtApplications = hearing.getCourtApplications();

        ofNullable(resultsShared.getHearing().getProsecutionCases()).ifPresent(
                prosecutionCases ->
                        prosecutionCases
                                .forEach(prosecutionCase ->
                                        prosecutionCase.getDefendants().forEach(defendant ->
                                                {
                                                    final UUID caseId = prosecutionCase.getId();
                                                    final UUID defendantId = defendant.getId();
                                                    final List<UUID> offenceIds = defendant.getOffences().stream().map(Offence::getId).collect(toList());
                                                    final Map<UUID, OffenceResult> offenceResultMap = mapOffenceWithOffenceResult(event, defendant.getOffences(), resultsShared);
                                                    updateTheDefendantsCase(event, resultsShared.getHearing().getId(), caseId, defendantId, offenceIds, offenceResultMap);
                                                }
                                        )
                                ));


        if (courtApplications != null && !courtApplications.isEmpty()) {
            // invoke command (resultsShared)
            updateDefendantWithApplicationDetailsDelegate.execute(sender, event, resultsShared);
        }

        if (hearing.getProsecutionCases() != null || courtApplications != null) {
            final HearingAdjourned hearingAdjourned = adjournHearingDelegate.execute(resultsShared, event);

            final ResultsShared resultsSharedFiltered = resultsSharedFilter.filterTargets(resultsShared, t -> t.getApplicationId() == null);

            if (!resultsSharedFiltered.getTargets().isEmpty()) {
                final List<Now> nows = nowsGenerator.createNows(event, resultsSharedFiltered, hearingAdjourned);

                final Map<UUID, List<Now>> nowsGroupByDefendant = nows.stream().collect(Collectors.groupingBy(Now::getDefendantId));

                nowsGroupByDefendant.forEach((defendant, nowsList) -> {

                    if (!nowsList.isEmpty()) {

                        final CreateNowsRequest nowsRequest = nowsDelegate.generateNows(event, nowsList, resultsSharedFiltered);

                        final Set<UUID> nowTypeIds = nowsRequest.getNows().stream()
                                .filter(now ->
                                        (now.getNowsTypeId().equals(NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID) ||
                                                now.getNowsTypeId().equals(ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID)))
                                .map(Now::getNowsTypeId)
                                .collect(Collectors.toSet());

                        final List<Target> targets = resultsShared.getTargets();

                        processOrderWithNonFinancial(event, nowsRequest, targets);

                        processOrderWithFinancialPenaltyAndAttachmentOfEarnings(event, nowsRequest, nowTypeIds, targets);

                        processOrderWithFinancial(event, nowsRequest, nowTypeIds, targets);

                    }

                });
            }
        }
        LOGGER.info("requested target size {}, saved target size {}", resultsShared.getTargets().size(), resultsShared.getSavedTargets().size());
        final List<UUID> requestedTargetIds = resultsShared.getTargets().stream().map(Target::getTargetId).collect(Collectors.toList());
        final List<Target> addSavedTargets = resultsShared.getSavedTargets().stream().filter(value -> !requestedTargetIds.contains(value.getTargetId())).collect(Collectors.toList());
        resultsShared.getTargets().addAll(addSavedTargets);
        LOGGER.info("combined target size {}", resultsShared.getTargets().size());
        publishResultsDelegate.shareResults(event, sender, resultsShared);

        updateResultLineStatusDelegate.updateResultLineStatus(sender, event, resultsShared);
    }

    public void updateTheDefendantsCase(final JsonEnvelope event, final UUID hearingId, final UUID caseId, final UUID defendantId, final List<UUID> offenceIds, final Map<UUID, OffenceResult> offenceResultMap) {

        final JsonObject payload = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("caseId", caseId.toString())
                .add("defendantId", defendantId.toString())
                .add("offenceIds", convertToJsonArray(offenceIds))
                .add("resultedOffences", convertOffentResultMapToJsonArray(offenceResultMap))
                .build();

        sender.send(envelop(payload)
                .withName("hearing.command.handler.update-offence-results")
                .withMetadataFrom(event));
    }

    private JsonArrayBuilder convertToJsonArray(final List<UUID> offenceIds) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        offenceIds.stream().map(UUID::toString).forEach(arrayBuilder::add);
        return arrayBuilder;
    }

    private JsonArrayBuilder convertOffentResultMapToJsonArray(final Map<UUID, OffenceResult> offenceResultMap) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        offenceResultMap.entrySet().stream().forEach(offenceResult -> {
            final JsonObject offenceResultObject = Json.createObjectBuilder()
                    .add("offenceId", offenceResult.getKey().toString())
                    .add("offenceResult", offenceResult.getValue().name())
                    .build();
            arrayBuilder.add(offenceResultObject);
        });
        return arrayBuilder;
    }

    private Map<UUID, OffenceResult> mapOffenceWithOffenceResult(JsonEnvelope event, List<Offence> offences, ResultsShared resultsShared) {

        final Map<UUID, OffenceResult> offenceResultMap = new HashMap<>();

        offences.stream().forEach(offence -> {
            final List<ResultLine> completedResultLinesForThisOffence = resultsShared.getTargets().stream()
                    .filter(target -> offence.getId().equals(target.getOffenceId()))
                    .flatMap(target -> target.getResultLines().stream())
                    .filter(r -> r.getLevel().equals(Level.OFFENCE))
                    .filter(ResultLine::getIsComplete)
                    .filter(r -> (isNull(r.getIsDeleted()) || !r.getIsDeleted()))
                    .collect(toList());

            completedResultLinesForThisOffence.forEach(resultLine -> {
                final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(event, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());
                if (isNull(resultDefinition)) {
                    throw new ResultDefinitionNotFoundException(format(
                            "resultDefinition not found for resultLineId: %s, resultDefinitionId: %s, hearingId: %s orderedDate: %s",
                            resultLine.getResultLineId(), resultLine.getResultDefinitionId(), resultsShared.getHearing().getId(), resultLine.getOrderedDate()));
                }
                final Category resultCategory = getCategory(resultDefinition);
                if (resultCategory.equals(Category.FINAL)) {
                    offenceResultMap.put(offence.getId(), mapOffenceResult(resultDefinition));
                } else if (Category.ANCILLARY.equals(resultCategory)
                        || Category.INTERMEDIARY.equals(resultCategory)) {
                    offenceResultMap.put(offence.getId(), OffenceResult.ADJOURNED);
                } else {
                    throw new ResultDefinitionNotFoundException(format(
                            "don't know how to handle the category for not found for resultLineId: %s, resultDefinitionId: %s, hearingId: %s orderedDate: %s",
                            resultLine.getResultLineId(), resultLine.getResultDefinitionId(), resultsShared.getHearing().getId(), resultLine.getOrderedDate()));
                }
            });
        });
        return offenceResultMap;
    }

    private OffenceResult mapOffenceResult(final ResultDefinition resultDefinition) {
        if (dismissedResultList.contains(resultDefinition.getId().toString())) {
            return OffenceResult.DISMISSED;
        } else if (withDrawnResultList.contains(resultDefinition.getId().toString())) {
            return OffenceResult.WITHDRAWN;
        } else {
            return OffenceResult.GUILTY;
        }
    }

    private Category getCategory(final ResultDefinition resultDefinition) {
        Category category = null;

        if (nonNull(resultDefinition) && nonNull(resultDefinition.getCategory())) {

            switch (resultDefinition.getCategory()) {
                case "A":
                    category = Category.ANCILLARY;
                    break;
                case "F":
                    category = Category.FINAL;
                    break;
                case "I":
                    category = Category.INTERMEDIARY;
                    break;
                default:
                    throw new IllegalArgumentException(format("No valid category found for result defnition %s", resultDefinition.getId().toString()));
            }
        }

        return category;
    }


    private void processOrderWithFinancialPenaltyAndAttachmentOfEarnings(final JsonEnvelope event, final CreateNowsRequest nowsRequest, final Set<UUID> nowTypeIds,
                                                                         final List<Target> targets) {

        final boolean hasOrderWithFinancialPenaltyAndAttachmentOfEarnings = nowTypeIds.size() == 2;

        if (hasOrderWithFinancialPenaltyAndAttachmentOfEarnings) {

            final List<Now> financialPenaltyWithAttachmentOfEarningsOrder = nowsRequest.getNows()
                    .stream()
                    .filter(now -> nowTypeIds.contains(now.getNowsTypeId()))
                    .collect(Collectors.toList());

            nowsDelegate.sendPendingNows(sender, event, createNowsRequest(nowsRequest, financialPenaltyWithAttachmentOfEarningsOrder), targets);
        }
    }

    private void processOrderWithFinancial(final JsonEnvelope event, final CreateNowsRequest nowsRequest, final Set<UUID> nowTypeIds,
                                           final List<Target> targets) {

        final boolean hasOrderWithFinancialPenaltyAndAttachmentOfEarnings = nowTypeIds.size() == 2;

        List<Now> nowsPendingWithFinancialImposition;

        if (hasOrderWithFinancialPenaltyAndAttachmentOfEarnings) {

            nowsPendingWithFinancialImposition = nowsRequest.getNows()
                    .stream()
                    .filter(now -> nonNull(now.getFinancialOrders()) && nonNull(now.getFinancialOrders().getAccountReference()))
                    .filter(now -> !nowTypeIds.contains(now.getNowsTypeId()))
                    .collect(Collectors.toList());
        } else {

            nowsPendingWithFinancialImposition = nowsRequest.getNows().stream()
                    .filter(now -> nonNull(now.getFinancialOrders()) && nonNull(now.getFinancialOrders().getAccountReference()))
                    .collect(Collectors.toList());
        }

        if (!nowsPendingWithFinancialImposition.isEmpty()) {
            nowsDelegate.sendPendingNows(sender, event, createNowsRequest(nowsRequest, nowsPendingWithFinancialImposition), targets);
        }
    }

    private void processOrderWithNonFinancial(final JsonEnvelope event, final CreateNowsRequest nowsRequest,
                                              final List<Target> targets) {

        final List<Now> nowsToSendToSendDirect = nowsRequest.getNows().stream()
                .filter(now -> isNull(now.getFinancialOrders()) || isNull(now.getFinancialOrders().getAccountReference())
                )
                .collect(Collectors.toList());


        if (!nowsToSendToSendDirect.isEmpty()) {
            nowsDelegate.sendNows(sender, event, createNowsRequest(nowsRequest, nowsToSendToSendDirect), targets);
        }
    }

    private CreateNowsRequest createNowsRequest(final CreateNowsRequest nowsRequest, final List<Now> nowList) {
        return new CreateNowsRequest(
                nowsRequest.getCourtClerk(),
                nowsRequest.getHearing(),
                nowsRequest.getNowTypes(),
                nowList,
                nowsRequest.getSharedResultLines());
    }
}