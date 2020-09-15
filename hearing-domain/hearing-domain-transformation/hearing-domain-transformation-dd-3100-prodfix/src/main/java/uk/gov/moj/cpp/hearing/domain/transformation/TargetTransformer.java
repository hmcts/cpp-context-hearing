package uk.gov.moj.cpp.hearing.domain.transformation;

import static java.util.stream.Stream.of;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.TargetIds.TARGET_IDS_TO_REPLACE;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;

import java.util.stream.Stream;

import javax.json.JsonValue;

import org.slf4j.Logger;

@Transformation
public class TargetTransformer implements EventTransformation {

    private static final Logger LOGGER = getLogger(TargetTransformer.class);

    private static final String EVENT_TO_TRANSFORM = "hearing.draft-result-saved";

    private EventPayloadTransformer eventPayloadTransformer;

    public TargetTransformer() {
        eventPayloadTransformer = new EventPayloadTransformer();
    }

    @Override
    public Action actionFor(final JsonEnvelope event) {
        final String eventName = event.metadata().name();
        final String payload = event.payload().toString();
        if (EVENT_TO_TRANSFORM.equalsIgnoreCase(eventName) && TARGET_IDS_TO_REPLACE.keySet().stream().anyMatch(payload::contains)) {
            LOGGER.debug("Found event '{}' with stream ID '{}'", eventName, event.metadata().streamId().orElse(null));
            return TRANSFORM;
        }

        return NO_ACTION;
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {
        final String eventName = event.metadata().name();
        LOGGER.info("Processing stream with ID '{}' and event with name '{}'", event.metadata().streamId().orElse(null), eventName);

        final JsonValue transformedPayload = eventPayloadTransformer.transform(event);
        final JsonEnvelope transformedEnvelope = envelopeFrom(event.metadata(), transformedPayload);
        return of(transformedEnvelope);
    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        // no need
    }

    public void setEventPayloadTransformer(final EventPayloadTransformer eventPayloadTransformer) {
        this.eventPayloadTransformer = eventPayloadTransformer;
    }

}
