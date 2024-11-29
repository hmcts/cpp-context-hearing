package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

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
public class HearingDeletedEventProcessor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingDeletedEventProcessor.class.getName());
    private static final String PUBLIC_EVENT_LISTING_ALLOCATED_HEARING_DELETED = "public.events.listing.allocated-hearing-deleted";
    private static final String HEARING_EVENT_HEARING_DELETED = "hearing.events.hearing-deleted";
    private static final String HEARING_COMMAND_DELETE_HEARING = "hearing.command.delete-hearing";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_PROSECUTION_CASES = "hearing.command.delete-hearing-for-prosecution-cases";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_DEFENDANTS = "hearing.command.delete-hearing-for-defendants";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_OFFENCES = "hearing.command.delete-hearing-for-offences";
    private static final String HEARING_COMMAND_DELETE_HEARING_FOR_COURT_APPLICATIONS = "hearing.command.delete-hearing-for-court-applications";

    private static final String PROSECUTION_CASE_IDS_FIELD = "prosecutionCaseIds";
    private static final String DEFENDANT_IDS_FIELD = "defendantIds";
    private static final String OFFENCE_IDS_FIELD = "offenceIds";
    private static final String COURT_APPLICATION_IDS_FIELD = "courtApplicationIds";
    private static final String HEARING_ID_FIELD = "hearingId";

    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Sender sender;

    @Handles(PUBLIC_EVENT_LISTING_ALLOCATED_HEARING_DELETED)
    public void handleHearingDeletedPublicEvent(final JsonEnvelope jsonEnvelope) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} event received: {}",
                    PUBLIC_EVENT_LISTING_ALLOCATED_HEARING_DELETED, jsonEnvelope.toObfuscatedDebugString());
        }

        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
        jsonObjectBuilder.add(HEARING_ID_FIELD, jsonEnvelope.payloadAsJsonObject().getString(HEARING_ID_FIELD));

        sender.send(envelopeFrom(metadataFrom(jsonEnvelope.metadata()).withName(HEARING_COMMAND_DELETE_HEARING),
                jsonObjectBuilder.build()));
    }

    @Handles(HEARING_EVENT_HEARING_DELETED)
    public void handleHearingDeletedPrivateEvent(final JsonEnvelope jsonEnvelope) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} event received: {}",
                    HEARING_EVENT_HEARING_DELETED, jsonEnvelope.toObfuscatedDebugString());
        }

        sendCommandFor(HEARING_COMMAND_DELETE_HEARING_FOR_PROSECUTION_CASES, PROSECUTION_CASE_IDS_FIELD, jsonEnvelope);
        sendCommandFor(HEARING_COMMAND_DELETE_HEARING_FOR_DEFENDANTS, DEFENDANT_IDS_FIELD, jsonEnvelope);
        sendCommandFor(HEARING_COMMAND_DELETE_HEARING_FOR_OFFENCES, OFFENCE_IDS_FIELD, jsonEnvelope);

        if (jsonEnvelope.payloadAsJsonObject().containsKey(COURT_APPLICATION_IDS_FIELD)) {
            sendCommandFor(HEARING_COMMAND_DELETE_HEARING_FOR_COURT_APPLICATIONS, COURT_APPLICATION_IDS_FIELD, jsonEnvelope);
        }

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
