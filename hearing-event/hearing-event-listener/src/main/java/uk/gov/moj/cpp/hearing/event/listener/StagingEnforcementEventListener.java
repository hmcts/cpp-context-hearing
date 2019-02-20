package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Now;
import uk.gov.moj.cpp.hearing.repository.NowRepository;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class StagingEnforcementEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEnforcementEventListener.class);

    @Inject
    private NowRepository nowRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.events.pending-nows-requested")
    public void pendingNowsRequested(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.pending-nows-requested event received {}", event.toObfuscatedDebugString());
        }

        final PendingNowsRequested pendingNowsRequested = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(),
                PendingNowsRequested.class);

        final CreateNowsRequest createNowsRequest = pendingNowsRequested.getCreateNowsRequest();

        final UUID hearingId = createNowsRequest.getHearing().getId();

        createNowsRequest.getNows().stream()
                .filter(now -> nonNull(now.getFinancialOrders()))
                .forEach(now -> nowRepository.save(new Now(now.getId(), hearingId, event.payloadAsJsonObject().toString())));

    }
}
