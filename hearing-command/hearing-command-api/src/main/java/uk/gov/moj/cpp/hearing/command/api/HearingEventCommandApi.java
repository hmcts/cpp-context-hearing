package uk.gov.moj.cpp.hearing.command.api;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@SuppressWarnings({"WeakerAccess", "unused"})
@ServiceComponent(COMMAND_API)
public class HearingEventCommandApi {

    private static final String COMMAND_LOG_HEARING_EVENT = "hearing.log-hearing-event";
    private static final String COMMAND_CORRECT_HEARING_EVENT = "hearing.correct-hearing-event";
    private static final String COMMAND_UPDATE_HEARING_EVENTS = "hearing.update-hearing-events";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_ALTERABLE = "alterable";

    @Inject
    private Sender sender;

    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles(COMMAND_LOG_HEARING_EVENT)
    public void logHearingEvent(final JsonEnvelope command) {
        final JsonObject responsePayload = getEventDefinition(command);
        enrichAndSendCommand(command, responsePayload, "hearing.command.log-hearing-event");
    }

    @Handles(COMMAND_CORRECT_HEARING_EVENT)
    public void correctEvent(final JsonEnvelope command) {
        final JsonObject responsePayload = getEventDefinition(command);
        enrichAndSendCommand(command, responsePayload, "hearing.command.correct-hearing-event");
    }

    @Handles(COMMAND_UPDATE_HEARING_EVENTS)
    public void updateHearingEvents(final JsonEnvelope command) {
        sender.send(enveloper.withMetadataFrom(command, "hearing.command.update-hearing-events")
                        .apply(command.payloadAsJsonObject()));
    }

    private void enrichAndSendCommand(final JsonEnvelope command, final JsonObject eventDefinitionDetails, final String eventName) {
        final JsonObject enrichedPayload = enrichIncomingPayload(command.payloadAsJsonObject(), eventDefinitionDetails.getBoolean(FIELD_ALTERABLE));
        sender.send(enveloper.withMetadataFrom(command, eventName).apply(enrichedPayload));
    }

    private JsonObject getEventDefinition(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();

        final JsonEnvelope query = enveloper.withMetadataFrom(command, "hearing.get-hearing-event-definition")
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_ID, payload.getString(FIELD_HEARING_ID))
                        .add(FIELD_HEARING_EVENT_DEFINITION_ID, payload.getString(FIELD_HEARING_EVENT_DEFINITION_ID))
                        .build()
                );
        return requester.request(query).payloadAsJsonObject();
    }

    private JsonObject enrichIncomingPayload(final JsonObject source, final boolean alterable) {
        final JsonObjectBuilder builder = createObjectBuilder();
        builder.add(FIELD_ALTERABLE, alterable);
        source.forEach(builder::add);
        return builder.build();
    }
}
