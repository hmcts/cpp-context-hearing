package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.delegates.NowsDelegate;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class NowsRequestedEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsRequestedEventProcessor.class);

    private final Enveloper enveloper;
    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final NowsDelegate nowsDelegate;

    @Inject
    public NowsRequestedEventProcessor(final Enveloper enveloper, final Sender sender,
                                       final NowsDelegate nowsDelegate,
                                       final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.nowsDelegate = nowsDelegate;
    }

    @Handles("hearing.events.nows-requested")
    public void processNowsRequested(final JsonEnvelope envelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.nows-requested {}", envelope.toObfuscatedDebugString());
        }

        final NowsRequested nowsRequested = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), NowsRequested.class);

        final String accountNumber = nowsRequested.getAccountNumber();

        final UUID requestId = nowsRequested.getRequestId();

        final CreateNowsRequest createNowsRequest = nowsRequested.getCreateNowsRequest();

        final List<Now> nows = createNowsRequest.getNows();

        if (nows.isEmpty()) {
            throw new IllegalArgumentException("No Orders");
        }

        final List<Now> financialOrders = nows.stream().filter(now -> now.getId().equals(requestId)).filter(now -> nonNull(now.getFinancialOrders())).collect(Collectors.toList());

        financialOrders.forEach(financialOrderDetail -> financialOrderDetail.getFinancialOrders().setAccountReference(accountNumber));

        nowsDelegate.sendNows(sender, envelope, createNowsRequest(createNowsRequest, financialOrders));

        //Get all non financial orders
        final List<Now> nonFinancialOrders = nows.stream().filter(now -> isNull(now.getFinancialOrders())).collect(Collectors.toList());

        nonFinancialOrders.forEach(nonFinancialOrder -> nonFinancialOrder.setFinancialOrders(financialOrders.get(0).getFinancialOrders()));

        if(!nonFinancialOrders.isEmpty()) {
            nowsDelegate.sendNows(sender, envelope, createNowsRequest(createNowsRequest, nonFinancialOrders));
        }

    }

    @Handles("hearing.events.nows-material-status-updated")
    public void propagateNowsMaterialStatusUpdated(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "public.hearing.events.nows-material-status-updated")
                .apply(createObjectBuilder()
                        .add("materialId", envelope.payloadAsJsonObject().getJsonString("materialId"))
                        .build()
                ));
    }

    private CreateNowsRequest createNowsRequest(final CreateNowsRequest nowsRequest, final List<Now> nows) {
        return new CreateNowsRequest(
                nowsRequest.getCourtClerk(),
                nowsRequest.getHearing(),
                nowsRequest.getNowTypes(),
                nows,
                nowsRequest.getSharedResultLines());
    }
}

