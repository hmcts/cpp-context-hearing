package uk.gov.moj.cpp.hearing.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class ReusableInfoCommandApi {

    private final Sender sender;

    @Inject
    public ReusableInfoCommandApi(final Sender sender){
        this.sender = sender;
    }

    @Handles("hearing.reusable-info")
    public void reusableInfo(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.reusable-info");
    }

    private void sendEnvelopeWithName(final JsonEnvelope envelope, final String name) {
        sender.send(envelop(envelope.payloadAsJsonObject())
                .withName(name)
                .withMetadataFrom(envelope));
    }
}
