package uk.gov.moj.cpp.hearing.event;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(EVENT_PROCESSOR)
public class DefendantCaseWithdrawnOrDismissedEventProcessor {

    private static final Logger LOGGER = getLogger(DefendantCaseWithdrawnOrDismissedEventProcessor.class);

    @Inject
    private Sender sender;

    @Handles("hearing.event.defendant-case-withdrawn-or-dismissed")
    public void defendantCaseWithdrawnOrDismissed(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.defendant-case-withdrawn-or-dismissed event received {}", event.toObfuscatedDebugString());
        }
        sender.send(envelop(event.payloadAsJsonObject())
                .withName("public.hearing.defendant-case-withdrawn-or-dismissed")
                .withMetadataFrom(event));
    }
}
