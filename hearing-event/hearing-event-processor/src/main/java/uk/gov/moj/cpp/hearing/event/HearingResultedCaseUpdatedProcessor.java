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
public class HearingResultedCaseUpdatedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingResultedCaseUpdatedProcessor.class);
    private final Enveloper enveloper;
    private final Sender sender;
    private static final String COMMAND_CASE_DEFENDANT_UPDATED= "hearing.command.update-case-defendants";


    @Inject
    public HearingResultedCaseUpdatedProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }


    @Handles("public.progression.hearing-resulted-case-updated")
    public void handleCaseDefendantUpdate(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.hearing-resulted-case-updated event received {}", envelop.toObfuscatedDebugString());
        }

        this.sender.send(this.enveloper.withMetadataFrom(envelop, COMMAND_CASE_DEFENDANT_UPDATED).apply(envelop.payloadAsJsonObject()));
    }
}
