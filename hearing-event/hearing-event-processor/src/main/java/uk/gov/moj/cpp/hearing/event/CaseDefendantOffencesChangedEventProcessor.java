package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseDefendantOffencesChangedEventProcessor {

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantOffencesChangedEventProcessor.class);

    @Handles("public.progression.defendant-offences-changed")
    public void processPublicCaseDefendantOffencesChanged(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.defendant-offences-changed event received {}", event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.defendant-offences-changed").apply(event.payloadAsJsonObject()));
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
