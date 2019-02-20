package uk.gov.moj.cpp.hearing.event;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.AdjournHearingDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.NowsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.SaveNowVariantsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.nows.NowsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
@SuppressWarnings({"squid:S1188"})
public class PublishResultsEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsEventProcessor.class.getName());

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private NowsGenerator nowsGenerator;

    @Inject
    private NowsDelegate nowsDelegate;

    @Inject
    private SaveNowVariantsDelegate saveNowVariantsDelegate;

    @Inject
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    @Inject
    private PublishResultsDelegate publishResultsDelegate;

    @Inject
    private AdjournHearingDelegate adjournHearingDelegate;

    @Inject
    private Sender sender;

    @Handles("hearing.results-shared")
    public void resultsShared(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.results-shared event received {}", event.toObfuscatedDebugString());
        }

        final ResultsShared resultsShared = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        final List<Target> targets = resultsShared.getHearing().getTargets();

        final HearingAdjourned hearingAdjourned = adjournHearingDelegate.execute(resultsShared, event);

        final List<Now> nows = nowsGenerator.createNows(event, resultsShared, hearingAdjourned);

        final Map<UUID, List<Now>> nowsGroupByDefendant = nows.stream().collect(Collectors.groupingBy(Now::getDefendantId));

        final List<Variant> newVariants = new ArrayList<>();

        nowsGroupByDefendant.forEach((defendant, nowsList) -> {

            if (!nowsList.isEmpty()) {

                // in case they were wiped out
                resultsShared.getHearing().setTargets(targets);

                newVariants.addAll(saveNowVariantsDelegate.saveNowsVariants(sender, event, nowsList, resultsShared));

                final CreateNowsRequest nowsRequest = nowsDelegate.generateNows(event, nowsList, resultsShared);

                final JurisdictionType jurisdictionType = resultsShared.getHearing().getJurisdictionType();

                final Set<UUID> nowTypeIds = nowsRequest.getNows().stream()
                        .filter(now ->
                                (now.getNowsTypeId().equals(NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID) ||
                                        now.getNowsTypeId().equals(ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID)) && jurisdictionType != JurisdictionType.CROWN)
                        .map(Now::getNowsTypeId)
                        .collect(Collectors.toSet());

                processOrderWithNonFinancialOrCrownCourt(event, nowsRequest, nowTypeIds, jurisdictionType);

                if (nonNull(nowsRequest.getHearing())) {
                    nowsRequest.getHearing().setTargets(targets);
                }

                processOrderWithFinancialPenaltyAndAttachmentOfEarnings(event, nowsRequest, nowTypeIds, jurisdictionType);

                processOrderWithFinancial(event, nowsRequest, nowTypeIds, jurisdictionType);

            }

        });

        // in case they were wiped out
        resultsShared.getHearing().setTargets(targets);
        publishResultsDelegate.shareResults(event, sender, event, resultsShared, newVariants);

        updateResultLineStatusDelegate.updateResultLineStatus(sender, event, resultsShared);
    }

    private void processOrderWithFinancialPenaltyAndAttachmentOfEarnings(final JsonEnvelope event, final CreateNowsRequest nowsRequest, final Set<UUID> nowTypeIds, final JurisdictionType jurisdictionType) {

        final boolean hasOrderWithFinancialPenaltyAndAttachmentOfEarnings = nowTypeIds.size() == 2;

        if(hasOrderWithFinancialPenaltyAndAttachmentOfEarnings) {

            final List<Now> financialPenaltyWithAttachmentOfEarningsOrder = nowsRequest.getNows().stream()
                    .filter(now -> nowTypeIds.contains(now.getNowsTypeId()) && jurisdictionType != JurisdictionType.CROWN)
                    .collect(Collectors.toList());

            nowsDelegate.sendPendingNows(sender, event, createNowsRequest(nowsRequest, financialPenaltyWithAttachmentOfEarningsOrder));
        }
    }

    private void processOrderWithFinancial(final JsonEnvelope event, final CreateNowsRequest nowsRequest, final Set<UUID> nowTypeIds, final JurisdictionType jurisdictionType) {

        final boolean hasOrderWithFinancialPenaltyAndAttachmentOfEarnings = nowTypeIds.size() == 2;

        List<Now> nowsPendingWithFinancialImposition;

        if(hasOrderWithFinancialPenaltyAndAttachmentOfEarnings) {

            nowsPendingWithFinancialImposition = nowsRequest.getNows().stream()
                    .filter(now -> nonNull(now.getFinancialOrders()) && nonNull(now.getFinancialOrders().getAccountReference())
                            && jurisdictionType != JurisdictionType.CROWN)
                    .filter(now -> !nowTypeIds.contains(now.getNowsTypeId()))
                    .collect(Collectors.toList());
        } else {

            nowsPendingWithFinancialImposition = nowsRequest.getNows().stream()
                    .filter(now -> nonNull(now.getFinancialOrders()) && nonNull(now.getFinancialOrders().getAccountReference())
                            && jurisdictionType != JurisdictionType.CROWN)
                    .collect(Collectors.toList());
        }

        if (!nowsPendingWithFinancialImposition.isEmpty()) {
            nowsDelegate.sendPendingNows(sender, event, createNowsRequest(nowsRequest, nowsPendingWithFinancialImposition));
        }
    }

    private void processOrderWithNonFinancialOrCrownCourt(final JsonEnvelope event, final CreateNowsRequest nowsRequest, final Set<UUID> nowTypeIds, final JurisdictionType jurisdictionType) {

        final boolean hasOrderWithFinancialPenaltyAndAttachmentOfEarnings = nowTypeIds.size() == 2;

        List<Now> nowsToSendToSendDirect;

        if(hasOrderWithFinancialPenaltyAndAttachmentOfEarnings) {

            nowsToSendToSendDirect = nowsRequest.getNows().stream()
                    .filter(now -> isNull(now.getFinancialOrders()) || isNull(now.getFinancialOrders().getAccountReference())
                            || jurisdictionType == JurisdictionType.CROWN)
                    .filter(now -> !nowTypeIds.contains(now.getNowsTypeId()))
                    .collect(Collectors.toList());

        } else {

            nowsToSendToSendDirect = nowsRequest.getNows().stream()
                    .filter(now -> isNull(now.getFinancialOrders()) || isNull(now.getFinancialOrders().getAccountReference())
                            || jurisdictionType == JurisdictionType.CROWN)
                    .collect(Collectors.toList());
        }

        if (!nowsToSendToSendDirect.isEmpty()) {
            nowsDelegate.sendNows(sender, event, createNowsRequest(nowsRequest, nowsToSendToSendDirect));
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
