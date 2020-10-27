package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a hearing being marked as a duplicate. Issues commands back to update internal state and
 * issues public method.
 */
@ServiceComponent(EVENT_PROCESSOR)
public class DuplicateHearingEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateHearingEventProcessor.class);

    private static final String PUBLIC_HEARING_MARKED_AS_DUPLICATE_EVENT = "public.events.hearing.marked-as-duplicate";
    private static final String HEARING_MARKED_AS_DUPLICATE_FOR_CASES_COMMAND = "hearing.command.mark-as-duplicate-for-cases";
    private static final String HEARING_MARKED_AS_DUPLICATE_FOR_DEFENDANTS_COMMAND = "hearing.command.mark-as-duplicate-for-defendants";
    private static final String HEARING_MARKED_AS_DUPLICATE_FOR_OFFENCES_COMMAND = "hearing.command.mark-as-duplicate-for-offences";

    private static final String PROSECUTION_CASE_IDS_FIELD = "prosecutionCaseIds";
    private static final String DEFENDANT_IDS_FIELD = "defendantIds";
    private static final String OFFENCE_IDS_FIELD = "offenceIds";
    private static final String HEARING_ID_FIELD = "hearingId";

    @Inject
    private Sender sender;

    @Handles("hearing.events.marked-as-duplicate")
    public void handleHearingMarkedAsDuplicate(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.marked-as-duplicate event received {}", event.toObfuscatedDebugString());
        }

        sendCommandFor(HEARING_MARKED_AS_DUPLICATE_FOR_CASES_COMMAND, PROSECUTION_CASE_IDS_FIELD, event);
        sendCommandFor(HEARING_MARKED_AS_DUPLICATE_FOR_DEFENDANTS_COMMAND, DEFENDANT_IDS_FIELD, event);
        sendCommandFor(HEARING_MARKED_AS_DUPLICATE_FOR_OFFENCES_COMMAND, OFFENCE_IDS_FIELD, event);

        final JsonObject payload = event.payloadAsJsonObject();

        sender.send(envelop(payload)
                .withName(PUBLIC_HEARING_MARKED_AS_DUPLICATE_EVENT)
                .withMetadataFrom(event));
    }

    /**
     * Sends the appropriate command (with the array field as part of the payload) if the original
     * event contains the array.
     *
     * @param commandName - the name of the command to send.
     * @param arrayField  - the array field (group of ids) to be sent with the command.
     * @param event       - the original event being processed (to pass on metadata).
     */
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
