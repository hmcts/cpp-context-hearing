package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import javax.inject.Inject;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseMarkerEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseMarkerEventProcessor.class);

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("public.progression.case-markers-updated")
    public void processPublicEventCaseMarkerUpdated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.case-markers-updated event received {}", event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.update-case-markers").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.case-markers-enriched-with-associated-hearings")
    public void enrichUpdateCaseMarkersWithAssociatedHearings(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.case-markers-enriched-with-associated-hearings event received {}", event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.update-case-markers-with-associated-hearings").apply(event.payloadAsJsonObject()));
    }
}