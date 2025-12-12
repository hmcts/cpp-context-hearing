package uk.gov.moj.cpp.hearing.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class SessionTimeCommandApi {
    private final Sender sender;

    @Inject
    public SessionTimeCommandApi(final Sender sender) {
        this.sender = sender;
    }

    @Handles("hearing.record-session-time")
    public void recordSessionTime(final JsonEnvelope envelope) {
        this.sender.send(envelop(envelope.payloadAsJsonObject())
                .withName("hearing.command.record-session-time")
                .withMetadataFrom(envelope));
    }
}
