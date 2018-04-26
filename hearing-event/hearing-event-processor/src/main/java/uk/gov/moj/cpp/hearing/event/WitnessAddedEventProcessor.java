package uk.gov.moj.cpp.hearing.event;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonString;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class WitnessAddedEventProcessor {

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("hearing.events.witness-added")
    public void publishWitnessAddedPublicEvent(final JsonEnvelope event) {

        JsonString witnessId = event.payloadAsJsonObject().getJsonString("id");
        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.events.witness-added")
                .apply(createObjectBuilder()
                        .add("witnessId", witnessId)
                        .build()));
    }

}
