package uk.gov.moj.cpp.hearing.event.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class ProgressionPublicEventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressionPublicEventProcessor.class);
    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    private static final String COMMAND_ADD_HEARINGS = "hearing.record-sending-sheet-complete";

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("public.progression.events.sending-sheet-completed")
    public void recordSendSheetCompleted(final JsonEnvelope event) {
        LOGGER.trace("Received public.progression.events.sending-sheet-completed, processing");

        sender.send(enveloper.withMetadataFrom(event, COMMAND_ADD_HEARINGS)
                .apply(event.payloadAsJsonObject()));
    }









}
