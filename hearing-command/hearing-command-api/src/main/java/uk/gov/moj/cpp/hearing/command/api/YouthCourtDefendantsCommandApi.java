package uk.gov.moj.cpp.hearing.command.api;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

@ServiceComponent(COMMAND_API)
public class YouthCourtDefendantsCommandApi {

    private final Sender sender;

    @Inject
    public YouthCourtDefendantsCommandApi(final Sender sender){
        this.sender = sender;
    }

    @Handles("hearing.youth-court-defendants")
    public void createYouthCourtDefendantsForHearing(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.youth-court-defendants");
    }

    private void sendEnvelopeWithName(final JsonEnvelope envelope, final String name) {
        sender.send(envelop(envelope.payloadAsJsonObject())
                .withName(name)
                .withMetadataFrom(envelope));
    }
}
