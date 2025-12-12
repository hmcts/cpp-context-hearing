package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;

import javax.inject.Inject;
import javax.json.JsonObject;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingAmendmentsEventProcessor {

    private static final Logger LOGGER = getLogger(HearingAmendmentsEventProcessor.class);

    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;
    @Inject
    private  ObjectToJsonObjectConverter objectToJsonObjectConverter;



    @Handles("hearing.event.result-amendments-validated")
    public void onResultAmendmentsValidated(final JsonEnvelope event) {
        process(event, "public.hearing.result-amendments-validated");
    }

    @Handles("hearing.events.result-amendments-cancelled")
    public void onResultAmendmentsCancelled(final JsonEnvelope event) {
        process(event, "public.hearing.result-amendments-cancelled");
    }

    @Handles("hearing.events.result-amendments-cancelled-v2")
    public void onResultAmendmentsCancelledV2(final JsonEnvelope event) {
        process(event, "public.hearing.result-amendments-cancelled");
    }

    private void process(final JsonEnvelope event, final String s) {
        log(event.toObfuscatedDebugString());
        final JsonObject publicEventPayload = this.objectToJsonObjectConverter.convert(event.payloadAsJsonObject());
        final MetadataBuilder metadata = metadataFrom(event.metadata()).withName(s);
        sender.send(envelopeFrom(metadata, publicEventPayload));
    }

    @Handles("hearing.event.result-amendments-rejected")
    public void onResultAmendmentsRejected(final JsonEnvelope event) {
        process(event, "public.hearing.result-amendments-rejected");
    }

    @Handles("hearing.event.result-amendments-rejected-v2")
    public void onResultAmendmentsRejectedV2(final JsonEnvelope event) {
        process(event, "public.hearing.result-amendments-rejected");
    }

    public void log(String msg){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.events.hearing-extended event received {}", msg);
        }
    }

}
