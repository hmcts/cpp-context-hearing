package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class ListingCourtRestrictionEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingCourtRestrictionEventProcessor.class);

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;


    @Handles("public.listing.court-list-restricted")
    public void processRestrictCourtListPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Public court-list-restricted received {}", event.toObfuscatedDebugString());
        }
        sender.send(envelop(event.payloadAsJsonObject())
                .withName("hearing.command.restrict-court-list")
                .withMetadataFrom(event));
    }
}
