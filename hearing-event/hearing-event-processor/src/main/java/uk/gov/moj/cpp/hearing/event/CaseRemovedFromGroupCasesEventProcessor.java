package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseRemovedFromGroupCasesEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseRemovedFromGroupCasesEventProcessor.class);

    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Sender sender;

    @Handles("public.progression.case-removed-from-group-cases")
    public void processPublicProgressionCaseRemovedFromGroupCases(final JsonEnvelope jsonEnvelope) {
        LOGGER.info("{} event received with payload {}",
                "public.progression.case-removed-from-group-cases",
                jsonEnvelope.payloadAsJsonObject());

        sender.send(envelopeFrom(metadataFrom(jsonEnvelope.metadata())
                        .withName("hearing.command.remove-case-from-group-cases"),
                jsonEnvelope.payloadAsJsonObject()));
    }

    @Handles("hearing.events.case-removed-from-group-cases")
    public void processHearingEventsCaseRemovedFromGroupCases(final JsonEnvelope jsonEnvelope) {
        LOGGER.info("{} event received with payload {}",
                "hearing.events.case-removed-from-group-cases",
                jsonEnvelope.payloadAsJsonObject());

        sender.send(envelopeFrom(metadataFrom(jsonEnvelope.metadata())
                        .withName("hearing.command.update-hearing-after-case-removed-from-group-cases"),
                jsonEnvelope.payloadAsJsonObject()));
    }
}