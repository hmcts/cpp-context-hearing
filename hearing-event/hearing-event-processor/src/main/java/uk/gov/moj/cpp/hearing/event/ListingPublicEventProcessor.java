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
public class ListingPublicEventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListingPublicEventProcessor.class);
    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    private static final String COMMAND_ADD_HEARINGS = "hearing.record-confirmed-hearing";

    @Handles("public.hearing-confirmed")
    public void recordHearingConfirmed(final JsonEnvelope event) {
        LOGGER.trace("Received hearing-added public event, processing");

        sender.send(enveloper.withMetadataFrom(event, COMMAND_ADD_HEARINGS)
                .apply(event.payloadAsJsonObject()));
    }
}
