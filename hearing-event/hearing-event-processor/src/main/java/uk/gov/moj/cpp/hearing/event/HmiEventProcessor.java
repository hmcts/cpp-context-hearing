package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class HmiEventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HmiEventProcessor.class);

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_OVERWRITE_WITH_RESULTS = "overwriteWithResults";

    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("public.staginghmi.hearing-updated-from-hmi")
    public void handleHearingUpdatedFromHmi(final JsonEnvelope event){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.staginghmi.hearing-updated-from-hmi event received {}", event.toObfuscatedDebugString());
        }

        if(! event.payloadAsJsonObject().containsKey("courtRoomId")){
            this.sender.send(envelopeFrom(
                    metadataFrom(event.metadata()).withName("hearing.command.delete-hearing"),
                    createObjectBuilder().add(FIELD_HEARING_ID, event.payloadAsJsonObject().getString(FIELD_HEARING_ID))));
        }

    }

    @Handles("public.staginghmi.hearing-deleted-from-hmi")
    public void handleHearingDeletedFromHmi(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.staginghmi.hearing-deleted-from-hmi event received {}", event.toObfuscatedDebugString());
        }

        this.sender.send(envelopeFrom(
                metadataFrom(event.metadata())
                        .withName("hearing.command.mark-as-duplicate"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, event.payloadAsJsonObject().getString(FIELD_HEARING_ID))
                        .add(FIELD_OVERWRITE_WITH_RESULTS, true)
                        .build()));
    }

}
