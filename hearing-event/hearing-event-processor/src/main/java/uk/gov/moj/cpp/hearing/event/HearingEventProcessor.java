package uk.gov.moj.cpp.hearing.event;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    private static final String PUBLIC_HEARING_HEARING_INITIATED = "public.hearing.hearing-initiated";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("hearing.hearing-initiated")
    public void publishHearingInitiatedPublicEvent(final JsonEnvelope event) {
        final String hearingId = event.payloadAsJsonObject().getString("hearingId");
        final JsonObject payload = Json.createObjectBuilder().add("hearingId", hearingId).build();
        sender.send(enveloper.withMetadataFrom(event, PUBLIC_HEARING_HEARING_INITIATED).apply(payload));
    }


}
