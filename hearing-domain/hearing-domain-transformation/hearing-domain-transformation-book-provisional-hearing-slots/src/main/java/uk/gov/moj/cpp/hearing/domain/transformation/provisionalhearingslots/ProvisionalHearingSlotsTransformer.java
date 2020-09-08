package uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots;

import static java.util.stream.Stream.of;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.domain.EventToTransform.isEventToTransform;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;
import uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.service.EventPayloadTransformer;

import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.slf4j.Logger;

@Transformation
public class ProvisionalHearingSlotsTransformer implements EventTransformation {

    private static final Logger LOGGER = getLogger(ProvisionalHearingSlotsTransformer.class);

    private static final String SLOTS_KEYWORD = "slots";
    private static final String COURT_SCHEDULE_ID_KEYWORD = "courtScheduleId";
    private final EventPayloadTransformer eventPayloadTransformer;


    public ProvisionalHearingSlotsTransformer() {
        eventPayloadTransformer = new EventPayloadTransformer();
    }


    @Override
    public Action actionFor(final JsonEnvelope event) {
        final String payload = event.payload().toString();
        final String eventName = event.metadata().name();
        final UUID streamId = event.metadata().streamId().orElse(null);

        if (isEventToTransform(eventName)
                && payload.contains(SLOTS_KEYWORD)
                && !payload.contains(COURT_SCHEDULE_ID_KEYWORD)) {

            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found event '{}' with stream ID '{}'",
                        eventName,
                        streamId);
            }


            return TRANSFORM;
        }

        return NO_ACTION;
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {
        JsonObject payload = event.payloadAsJsonObject();
        final Metadata metadata = event.metadata();
        final String eventName = metadata.name();
        final UUID streamId = metadata.streamId().orElse(null);

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Processing stream with ID '{}' and event with name '{}'",
                    streamId,
                    eventName);
        }

        payload = eventPayloadTransformer.transform(payload);
        return of(envelopeFrom(metadata, payload));
    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        // not used
    }
}