package uk.gov.moj.cpp.hearing.command.api;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.api.service.ReferenceDataService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@SuppressWarnings({"WeakerAccess", "unused"})
@ServiceComponent(COMMAND_API)
public class HearingEventCommandApi {

    private static final String COMMAND_LOG_HEARING_EVENT = "hearing.log-hearing-event";
    private static final String COMMAND_CORRECT_HEARING_EVENT = "hearing.correct-hearing-event";
    private static final String COMMAND_UPDATE_HEARING_EVENTS = "hearing.update-hearing-events";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_ACTIVE_HEARINGS = "activeHearings";
    private static final String FIELD_OVERRIDE = "override";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_HEARING_TYPE_IDS = "hearingTypeIds";

    @Inject
    private Sender sender;

    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private ReferenceDataService referenceDataService;

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles(COMMAND_LOG_HEARING_EVENT)
    public void logHearingEvent(final JsonEnvelope command) {
        final JsonObject responsePayload = getEventDefinition(command);
        final boolean overrideCourtRoom = command.payloadAsJsonObject().getBoolean(FIELD_OVERRIDE, false);
        final List<UUID> hearingTypeIds = referenceDataService.getTrialHearingTypes(command);

        if (overrideCourtRoom) {
            final JsonObject activeHearingsDetails = getActiveHearingsForCourtRoom(command);
            final JsonObject payloadWithActiveHearings = createObjectBuilder(command.payloadAsJsonObject())
                    .add(FIELD_ACTIVE_HEARINGS, activeHearingsDetails.getJsonArray(FIELD_ACTIVE_HEARINGS))
                    .build();
            enrichAndSendCommandWithHearingTypeIds(command, responsePayload, "hearing.command.log-hearing-event", payloadWithActiveHearings, hearingTypeIds);
        } else {
            enrichAndSendCommandWithHearingTypeIds(command, responsePayload, "hearing.command.log-hearing-event", hearingTypeIds);
        }
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

    private JsonObject getActiveHearingsForCourtRoom(final JsonEnvelope command) {
        final String FIELD_EVENT_DATE = "eventDate";
        final JsonObject payload = command.payloadAsJsonObject();
        final LocalDate eventDate = ZonedDateTimes.fromString(payload.getString(FIELD_EVENT_TIME)).toLocalDate();

        final JsonEnvelope query = enveloper.withMetadataFrom(command, "hearing.get-active-hearings-for-court-room")
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_ID, payload.getString(FIELD_HEARING_ID))
                        .add(FIELD_EVENT_DATE, eventDate.toString())
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

    private void enrichAndSendCommandWithHearingTypeIds(final JsonEnvelope command, final JsonObject eventDefinitionDetails, final String eventName, final List<UUID> hearingTypeIds) {
        final JsonObject enrichedPayload = enrichIncomingPayloadWithHearingTypeIds(command.payloadAsJsonObject(), eventDefinitionDetails.getBoolean(FIELD_ALTERABLE), hearingTypeIds);
        sender.send(enveloper.withMetadataFrom(command, eventName).apply(enrichedPayload));
    }


    private void enrichAndSendCommandWithHearingTypeIds(final JsonEnvelope command, final JsonObject eventDefinitionDetails, final String eventName, final JsonObject payloadWithActiveHearings, final List<UUID> hearingTypeIds) {
        final JsonObject enrichedPayload = enrichIncomingPayloadWithHearingTypeIds(payloadWithActiveHearings, eventDefinitionDetails.getBoolean(FIELD_ALTERABLE), hearingTypeIds);
        sender.send(enveloper.withMetadataFrom(command, eventName).apply(enrichedPayload));
    }

    private JsonObject enrichIncomingPayloadWithHearingTypeIds(final JsonObject source, final boolean alterable, final List<UUID> hearingTypeIds) {

        final JsonArrayBuilder hearingTypesArray = createArrayBuilder();
        hearingTypeIds.stream().map(UUID::toString).forEach(hearingTypesArray::add);

        final JsonObjectBuilder builder = createObjectBuilder();
        builder.add(FIELD_ALTERABLE, alterable);
        builder.add(FIELD_HEARING_TYPE_IDS, hearingTypesArray.build());
        source.forEach(builder::add);

        return builder.build();
    }
}
