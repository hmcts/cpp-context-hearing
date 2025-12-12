package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseDefendantAddEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantAddEventProcessor.class);

    private static final String PUBLIC_EVENT_PROGRESSION_DEFENDANTS_ADDED_TO_HEARING = "public.progression.defendants-added-to-hearing";

    private static final String HEARING_DEFENDANT_ADDED = "hearing.defendant-added";

    private static final String HEARING_COMMAND_REGISTER_HEARING_AGAINST_DEFENDANT = "hearing.command.register-hearing-against-defendant";

    private static final String HEARING_COMMAND_REGISTER_HEARING_AGAINST_OFFENCE = "hearing.command.register-hearing-against-offence";

    private static final String EVENT_RECEIVED_LOG_TEMPLATE = "{} event received {}";

    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Handles(PUBLIC_EVENT_PROGRESSION_DEFENDANTS_ADDED_TO_HEARING)
    public void processPublicCaseDefendantAdded(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_LOG_TEMPLATE, PUBLIC_EVENT_PROGRESSION_DEFENDANTS_ADDED_TO_HEARING, event.toObfuscatedDebugString());
        }
        sender.send(enveloper.withMetadataFrom(event, "hearing.add-defendants").apply(event.payloadAsJsonObject()));
    }

    @Handles(HEARING_DEFENDANT_ADDED)
    public void registerHearing(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_LOG_TEMPLATE, HEARING_DEFENDANT_ADDED, event.toObfuscatedDebugString());
        }
        final JsonObject jsonObject = event.payloadAsJsonObject();
        final Defendant defendant = this.jsonObjectToObjectConverter.convert(jsonObject.getJsonObject("defendant"), Defendant.class);
        final UUID hearingId = UUID.fromString(jsonObject.getString("hearingId"));

        this.sender.send(this.enveloper
                .withMetadataFrom(event, HEARING_COMMAND_REGISTER_HEARING_AGAINST_DEFENDANT)
                .apply(RegisterHearingAgainstDefendantCommand.builder()
                        .withDefendantId(defendant.getId())
                        .withHearingId(hearingId)
                        .build()));

        for (final uk.gov.justice.core.courts.Offence offence : defendant.getOffences()) {
            this.sender.send(this.enveloper
                    .withMetadataFrom(event, HEARING_COMMAND_REGISTER_HEARING_AGAINST_OFFENCE)
                    .apply(RegisterHearingAgainstOffenceCommand.registerHearingAgainstOffenceDefendantCommand()
                            .setHearingId(hearingId)
                            .setOffenceId(offence.getId())
                    ));
        }
    }
}
