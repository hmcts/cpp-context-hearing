package uk.gov.moj.cpp.hearing.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class NotificationCommandApi {

    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Handles("hearing.upload-subscriptions")
    public void uploadSubscriptions(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper
                .withMetadataFrom(envelope, "hearing.command.upload-subscriptions")
                .apply(envelope.payloadAsJsonObject()));
    }
}
