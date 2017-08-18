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
}
