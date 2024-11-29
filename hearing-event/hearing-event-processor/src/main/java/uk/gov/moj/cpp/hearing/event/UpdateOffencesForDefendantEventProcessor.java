package uk.gov.moj.cpp.hearing.event;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand.registerHearingAgainstOffenceDefendantCommand;


import javax.json.Json;
import javax.json.JsonObject;
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
    private static final String PUBLIC_EVENTS_LISTING_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING = "public.events.listing.offences-removed-from-existing-allocated-hearing";
    private static final String HEARING_COMMAND_REMOVE_OFFENCES_FROM_EXISTING_HEARING = "hearing.command.remove-offences-from-existing-hearing";
    private static final String HEARING_COMMAND_REMOVE_OFFENCES_FROM_EXISTING_ALLOCATED_HEARING = "hearing.command.remove-offences-from-existing-allocated-hearing";
    public static final String PUBLIC_PROGRESSION_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING = "public.progression.offences-removed-from-existing-allocated-hearing";
    public static final String EVENT_RECEIVED_WITH_METADATA_AND_PAYLOAD = "{} event received with metadata {} and payload {}";
    public static final String DEFENDANT_IDS = "defendantIds";
    public static final String HEARING_ID = "hearingId";


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

    @Handles(PUBLIC_EVENTS_LISTING_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING)
    public void handleOffencesRemovedFromExistingAllocatedHearingPublicEvent(final JsonEnvelope jsonEnvelope) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_WITH_METADATA_AND_PAYLOAD,
                    PUBLIC_EVENTS_LISTING_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING, jsonEnvelope.metadata(), jsonEnvelope.toObfuscatedDebugString());
        }

        sender.send(envelopeFrom(metadataFrom(jsonEnvelope.metadata()).withName(HEARING_COMMAND_REMOVE_OFFENCES_FROM_EXISTING_ALLOCATED_HEARING),
                jsonEnvelope.payloadAsJsonObject()));
    }

    @Handles(PUBLIC_PROGRESSION_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING)
    public void handleProgressionOffencesRemovedFromExistingAllocatedHearingPublicEvent(final JsonEnvelope jsonEnvelope) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(EVENT_RECEIVED_WITH_METADATA_AND_PAYLOAD,
                    PUBLIC_PROGRESSION_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING, jsonEnvelope.metadata(), jsonEnvelope.toObfuscatedDebugString());
        }

        sender.send(envelopeFrom(metadataFrom(jsonEnvelope.metadata()).withName(HEARING_COMMAND_REMOVE_OFFENCES_FROM_EXISTING_HEARING),
                jsonEnvelope.payloadAsJsonObject()));
    }

    @Handles("hearing.events.offences-removed-from-existing-hearing")
    public void handleOffenceOrDefendantRemovalToListAssist(final JsonEnvelope jsonEnvelope) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(EVENT_RECEIVED_WITH_METADATA_AND_PAYLOAD,
                    "hearing.events.offences-removed-from-existing-hearing", jsonEnvelope.metadata(), jsonEnvelope.toObfuscatedDebugString());
        }

        if( jsonEnvelope.payloadAsJsonObject().containsKey(DEFENDANT_IDS) && !jsonEnvelope.payloadAsJsonObject().getJsonArray(DEFENDANT_IDS).isEmpty() ) {
            final JsonObject cmdPayload = Json.createObjectBuilder()
                    .add(HEARING_ID, jsonEnvelope.payloadAsJsonObject().get(HEARING_ID))
                    .add(DEFENDANT_IDS, jsonEnvelope.payloadAsJsonObject().get(DEFENDANT_IDS))
                    .build();
            sender.send(Enveloper.envelop(cmdPayload).withName("hearing.command.delete-hearing-for-defendants").withMetadataFrom(jsonEnvelope));
        }

        if("Hearing".equals(jsonEnvelope.payloadAsJsonObject().getString("sourceContext", "Hearing"))) {
            final JsonObject payload = Json.createObjectBuilder()
                    .add(HEARING_ID, jsonEnvelope.payloadAsJsonObject().get(HEARING_ID))
                    .add("offenceIds", jsonEnvelope.payloadAsJsonObject().get("offenceIds"))
                    .build();
            sender.send(Enveloper.envelop(payload).withName("public.hearing.selected-offences-removed-from-existing-hearing").withMetadataFrom(jsonEnvelope));
        }
    }
}
