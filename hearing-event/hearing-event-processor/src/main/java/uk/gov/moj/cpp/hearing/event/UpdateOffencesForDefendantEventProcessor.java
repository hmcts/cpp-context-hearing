package uk.gov.moj.cpp.hearing.event;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand.registerHearingAgainstOffenceDefendantCommand;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand;
import uk.gov.moj.cpp.hearing.command.offence.AddOffenceCommand;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(EVENT_PROCESSOR)
public class UpdateOffencesForDefendantEventProcessor {

    private static final Logger LOGGER = getLogger(UpdateOffencesForDefendantEventProcessor.class);

    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("public.progression.defendant-offences-changed")
    public void onPublicProgressionEventsOffencesForDefendantUpdated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.defendant-offences-changed event received {}", event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.update-offences-for-defendant").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.found-hearings-for-new-offence")
    public void addCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.add-new-offence-to-hearings").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.found-hearings-for-edit-offence")
    public void updateCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.update-offence-on-hearings").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.found-hearings-for-delete-offence")
    public void deleteCaseDefendantOffence(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, "hearing.command.delete-offence-on-hearings").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.offence-added")
    public void addOffence(final JsonEnvelope event) {
        final AddOffenceCommand addOffenceCommand = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), AddOffenceCommand.class);

        final RegisterHearingAgainstOffenceCommand registerHearingAgainstOffenceCommand = registerHearingAgainstOffenceDefendantCommand()
                .setHearingId(addOffenceCommand.getHearingId())
                .setOffenceId(addOffenceCommand.getOffence().getId());

        sender.send(envelop(registerHearingAgainstOffenceCommand)
                .withName("hearing.command.register-hearing-against-offence")
                .withMetadataFrom(event));
    }
}
