package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingUnallocatedEventProcessor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingUnallocatedEventProcessor.class.getName());
    private static final String PUBLIC_EVENTS_LISTING_HEARING_UNALLOCATED = "public.events.listing.hearing-unallocated";
    private static final String HEARING_EVENT_HEARING_UNALLOCATED = "hearing.events.hearing-unallocated";
    private static final String HEARING_COMMAND_UNALLOCATE_HEARING = "hearing.command.unallocate-hearing";
    private static final String HEARING_COMMAND_REMOVE_HEARING_FOR_PROSECUTION_CASES = "hearing.command.remove-hearing-for-prosecution-cases";
    private static final String HEARING_COMMAND_REMOVE_HEARING_FOR_DEFENDANTS = "hearing.command.remove-hearing-for-defendants";
    private static final String HEARING_COMMAND_REMOVE_HEARING_FOR_OFFENCES = "hearing.command.remove-hearing-for-offences";

    private static final String PROSECUTION_CASE_IDS_FIELD = "prosecutionCaseIds";
    private static final String DEFENDANT_IDS_FIELD = "defendantIds";
    private static final String OFFENCE_IDS_FIELD = "offenceIds";
    private static final String HEARING_ID_FIELD = "hearingId";

    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles(PUBLIC_EVENTS_LISTING_HEARING_UNALLOCATED)
    public void handleHearingUnallocatedPublicEvent(final JsonEnvelope jsonEnvelope) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} event received with metadata {} and payload {}",
                    PUBLIC_EVENTS_LISTING_HEARING_UNALLOCATED, jsonEnvelope.metadata(), jsonEnvelope.payloadAsJsonObject());
        }

        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
        jsonObjectBuilder.add(HEARING_ID_FIELD, jsonEnvelope.payloadAsJsonObject().getString(HEARING_ID_FIELD));
        jsonObjectBuilder.add(OFFENCE_IDS_FIELD, jsonEnvelope.payloadAsJsonObject().getJsonArray(OFFENCE_IDS_FIELD));

        sender.send(envelopeFrom(metadataFrom(jsonEnvelope.metadata()).withName(HEARING_COMMAND_UNALLOCATE_HEARING),
                jsonObjectBuilder.build()));
    }

    @Handles(HEARING_EVENT_HEARING_UNALLOCATED)
    public void handleHearingUnallocatedPrivateEvent(final JsonEnvelope jsonEnvelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} event received with metadata {} and payload {}",
                    HEARING_EVENT_HEARING_UNALLOCATED, jsonEnvelope.metadata(), jsonEnvelope.payloadAsJsonObject());
        }

        if (!jsonEnvelope.payloadAsJsonObject().getJsonArray(PROSECUTION_CASE_IDS_FIELD).isEmpty()) {
            sendCommandFor(HEARING_COMMAND_REMOVE_HEARING_FOR_PROSECUTION_CASES, PROSECUTION_CASE_IDS_FIELD, jsonEnvelope);
        }

        if (!jsonEnvelope.payloadAsJsonObject().getJsonArray(DEFENDANT_IDS_FIELD).isEmpty()) {
            sendCommandFor(HEARING_COMMAND_REMOVE_HEARING_FOR_DEFENDANTS, DEFENDANT_IDS_FIELD, jsonEnvelope);
        }

        sendCommandFor(HEARING_COMMAND_REMOVE_HEARING_FOR_OFFENCES, OFFENCE_IDS_FIELD, jsonEnvelope);
    }

    private void sendCommandFor(final String commandName, final String arrayField, final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        if (payload.containsKey(arrayField)) {
            sender.send(envelop(createObjectBuilder()
                    .add(HEARING_ID_FIELD, payload.getString(HEARING_ID_FIELD))
                    .add(arrayField, payload.getJsonArray(arrayField))
                    .build())
                    .withName(commandName)
                    .withMetadataFrom(event));
        }
    }
}
