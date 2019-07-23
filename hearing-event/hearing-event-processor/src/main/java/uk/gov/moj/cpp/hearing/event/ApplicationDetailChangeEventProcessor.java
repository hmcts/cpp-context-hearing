package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class ApplicationDetailChangeEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDetailChangeEventProcessor.class);


    private static final String COMMAND_UPDATE_COURT_APPLICATION = "hearing.update-court-application";

    private static final String PUBLIC_EVENT_PROGRESSION_COURT_APPLICATION_CHANGED = "public.progression.court-application-updated";

    private static final String EVENT_RECEIVED_LOG_TEMPLATE = "{} event received {}";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles(PUBLIC_EVENT_PROGRESSION_COURT_APPLICATION_CHANGED)
    public void handleCourtApplicationChanged(final JsonEnvelope envelope) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_RECEIVED_LOG_TEMPLATE, PUBLIC_EVENT_PROGRESSION_COURT_APPLICATION_CHANGED, envelope.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(envelope, COMMAND_UPDATE_COURT_APPLICATION).apply(envelope.payloadAsJsonObject()));
    }

}
