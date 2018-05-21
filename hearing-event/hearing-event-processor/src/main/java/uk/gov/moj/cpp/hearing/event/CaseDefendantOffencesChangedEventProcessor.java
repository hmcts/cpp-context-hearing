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
        LOGGER.debug("public.progression.defendant-offences-changed event received {}", event.payloadAsJsonObject());
        sender.send(enveloper.withMetadataFrom(event, "hearing.update-case-defendant-offences").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.add-case-defendant-offence-enriched-with-hearing-ids")
    public void addCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.add-case-defendant-offence").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.update-case-defendant-offence-enriched-with-hearing-ids")
    public void updateCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.update-case-defendant-offence").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.delete-case-defendant-offence-enriched-with-hearing-ids")
    public void deleteCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.delete-case-defendant-offence").apply(event.payloadAsJsonObject()));
    }

}
