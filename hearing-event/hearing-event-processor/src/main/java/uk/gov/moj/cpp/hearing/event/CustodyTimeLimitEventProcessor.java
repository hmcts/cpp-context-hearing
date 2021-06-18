package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.slf4j.Logger;

@ServiceComponent(EVENT_PROCESSOR)
public class CustodyTimeLimitEventProcessor {

    private static final Logger LOGGER = getLogger(CustodyTimeLimitEventProcessor.class);

    @Inject
    private Sender sender;

    @Handles("hearing.event.custody-time-limit-clock-stopped")
    public void publishStopClockCustodyTimeLimit(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.custody-time-limit-clock-stopped event received {}", event.toObfuscatedDebugString());
        }

        sender.send(Enveloper.envelop(event.payloadAsJsonObject())
                .withName("public.events.hearing.custody-time-limit-clock-stopped")
                .withMetadataFrom(event));
    }

    @Handles("public.events.progression.custody-time-limit-extended")
    public void updateHearingsWithExtendedCustodyTimeLimit(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.events.progression.custody-time-limit-extended event received {}", event.toObfuscatedDebugString());
        }

        final JsonObject eventPayload = event.payloadAsJsonObject();

        for (final JsonString hearingId : eventPayload.getJsonArray("hearingIds").getValuesAs(JsonString.class)) {

            final JsonObject payload = createObjectBuilder()
                    .add("hearingId", hearingId.getString())
                    .add("extendedTimeLimit", eventPayload.getString("extendedTimeLimit"))
                    .add("offenceId", eventPayload.getString("offenceId"))
                    .build();

            sender.send(envelop(payload)
                    .withName("hearing.command.extend-custody-time-limit")
                    .withMetadataFrom(event));
        }
    }
}
