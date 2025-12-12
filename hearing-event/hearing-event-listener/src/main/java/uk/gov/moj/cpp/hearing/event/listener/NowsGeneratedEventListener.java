package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class NowsGeneratedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsGeneratedEventListener.class);

    private final NowsMaterialRepository nowsMaterialRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public NowsGeneratedEventListener(final NowsMaterialRepository nowsMaterialRepository,
                                      final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.nowsMaterialRepository = nowsMaterialRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.events.nows-material-status-updated")
    public void nowsGenerated(final JsonEnvelope event) {
        final NowsMaterialStatusUpdated nowsMaterialStatusUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), NowsMaterialStatusUpdated.class);
        this.nowsMaterialRepository.updateStatus(nowsMaterialStatusUpdated.getMaterialId(), nowsMaterialStatusUpdated.getStatus());
        LOGGER.info("NOWs material status updated successfully in viewstore for Hearing Id {} with Material Id: {}", nowsMaterialStatusUpdated.getHearingId(), nowsMaterialStatusUpdated.getMaterialId());
    }
}