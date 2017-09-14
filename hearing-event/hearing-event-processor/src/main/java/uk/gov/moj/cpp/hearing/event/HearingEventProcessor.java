package uk.gov.moj.cpp.hearing.event;

import static java.lang.String.format;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class HearingEventProcessor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingEventProcessor.class.getName());

    private static final String PUBLIC_HEARING_HEARING_INITIATED = "public.hearing.hearing-initiated";
    private static final String PUBLIC_HEARING_RESULTED = "public.hearing.resulted";
    private static final String PUBLIC_HEARING_RESULT_AMENDED = "public.hearing.result-amended";
    private static final String PUBLIC_HEARING_HEARING_ADJOURNED = "public.hearing.adjourn-date-updated";
    private static final String PUBLIC_HEARING_EVENT_LOGGED = "public.hearing.event-logged";
    private static final String PUBLIC_HEARING_TIMESTAMP_CORRECTED = "public.hearing.event-timestamp-corrected";

    private static final String FIELD_HEARING_DEFINITION_ID ="hearingEventDefinitionId";
    private static final String FIELD_HEARING_EVENT_ID ="hearingEventId";
    private static final String FIELD_LAST_HEARING_EVENT_ID ="lastHearingEventId";
    private static final String FIELD_EVENT_TIME ="eventTime";
    private static final String FIELD_RECORDED_LABEL ="recordedLabel";
    private static final String FIELD_ALTERABLE ="alterable";
    private static final String FIELD_PRIORITY ="priority";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles("hearing.hearing-initiated")
    public void publishHearingInitiatedPublicEvent(final JsonEnvelope event) {
        final String hearingId = event.payloadAsJsonObject().getString("hearingId");
        final JsonObject payload = Json.createObjectBuilder().add("hearingId", hearingId).build();
        sender.send(enveloper.withMetadataFrom(event, PUBLIC_HEARING_HEARING_INITIATED).apply(payload));
    }

    @Handles("hearing.results-shared")
    public void publishHearingResultsSharedPublicEvent(final JsonEnvelope event) {
        LOGGER.debug(format("'public.hearing.resulted' event received %s", event.payloadAsJsonObject()));
        sender.send(enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULTED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.adjourn-date-updated")
    public void publishHearingDateAdjournedPublicEvent(final JsonEnvelope event) {
        final String startDate = event.payloadAsJsonObject().getString("startDate");
        final JsonObject payload = Json.createObjectBuilder().add("startDate", startDate).build();
        sender.send(enveloper.withMetadataFrom(event, PUBLIC_HEARING_HEARING_ADJOURNED).apply(payload));
    }

    @Handles("hearing.result-amended")
    public void publishHearingResultAmendedPublicEvent(final JsonEnvelope event) {
        sender.send(enveloper.withMetadataFrom(event, PUBLIC_HEARING_RESULT_AMENDED).apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.hearing-event-logged")
    public void publishHearingEventLoggedPublicEvent(final JsonEnvelope event) {
        final JsonObject jsonObject = event.payloadAsJsonObject();
        final String hearingEventDefinitionId = jsonObject.getString(FIELD_HEARING_DEFINITION_ID);
        final String hearingEventId = jsonObject.getString(FIELD_HEARING_EVENT_ID);
        final String lastHearingEventId = jsonObject.getString(FIELD_LAST_HEARING_EVENT_ID, null);
        final String recordedLabel = jsonObject.getString(FIELD_RECORDED_LABEL);
        final String eventTime = jsonObject.getString(FIELD_EVENT_TIME);
        final boolean priority = jsonObject.getBoolean(FIELD_ALTERABLE);
        if (lastHearingEventId == null) {
            final JsonObject payload = Json.createObjectBuilder().add(FIELD_EVENT_TIME, eventTime)
                    .add(FIELD_RECORDED_LABEL, recordedLabel)
                    .add(FIELD_HEARING_DEFINITION_ID, hearingEventDefinitionId)
                    .add(FIELD_HEARING_EVENT_ID, hearingEventId)
                    .add(FIELD_PRIORITY, priority).build();
            LOGGER.debug(format("'public.hearing-event-logged' event published %s", jsonObject));
            sender.send(enveloper.withMetadataFrom(event, PUBLIC_HEARING_EVENT_LOGGED).apply(payload));
        } else {
            final JsonObject payload = Json.createObjectBuilder().add(FIELD_EVENT_TIME, eventTime)
                    .add(FIELD_RECORDED_LABEL, recordedLabel)
                    .add(FIELD_HEARING_DEFINITION_ID, hearingEventDefinitionId)
                    .add(FIELD_HEARING_EVENT_ID, hearingEventId)
                    .add(FIELD_LAST_HEARING_EVENT_ID, lastHearingEventId)
                    .add(FIELD_PRIORITY, priority).build();

            LOGGER.debug(format("'public.hearing-event-timestamp-corrected' event published %s", jsonObject));
            sender.send(enveloper.withMetadataFrom(event, PUBLIC_HEARING_TIMESTAMP_CORRECTED).apply(payload));
        }

    }

}
