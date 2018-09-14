package uk.gov.moj.cpp.hearing.event;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(EVENT_PROCESSOR)
public class UpdateOffencesForDefendantEventProcessor {

    private static final Logger LOGGER = getLogger(UpdateOffencesForDefendantEventProcessor.class);

    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;

    @Handles("public.progression.events.offences-for-defendant-updated")
    public void onPublicProgressionEventsOffencesForDefendantUpdated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.events.offences-for-defendant-updated event received {}", event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.update-offences-for-defendant").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.found-hearings-for-new-offence")
    public void addCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.add-new-offence-to-hearings").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.found-hearings-for-edit-offence")
    public void updateCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.update-offence-on-hearings").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.found-hearings-for-delete-offence")
    public void deleteCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.delete-offence-on-hearings").apply(event.payloadAsJsonObject()));
    }
}