package uk.gov.moj.cpp.hearing.command.api;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

@ServiceComponent(COMMAND_API)
public class DefendantsWelshTranslationsCommandApi {

    private final Sender sender;

    @Inject
    public DefendantsWelshTranslationsCommandApi(final Sender sender){
        this.sender = sender;
    }

    @Handles("hearing.save-defendants-welsh-translations")
    public void saveDefendantsForWelshTranslations(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.save-defendants-welsh-translations");
    }

    private void sendEnvelopeWithName(final JsonEnvelope envelope, final String name) {
        sender.send(envelop(envelope.payloadAsJsonObject())
                .withName(name)
                .withMetadataFrom(envelope));
    }
}
