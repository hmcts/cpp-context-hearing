package uk.gov.moj.cpp.hearing.domain.transformation.ancillarydocs;

import static java.util.stream.Stream.of;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.ancillarydocs.AttendanceDayEventTransformer.eventAndJsonPaths;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;

import java.util.stream.Stream;

import javax.json.JsonObject;

import org.slf4j.Logger;

@Transformation
public class HearingEventStreamTransform implements EventTransformation {

    private static final Logger LOGGER = getLogger(HearingEventStreamTransform.class);

    private AttendanceDayEventTransformer attendanceDayEventTransformer;

    public HearingEventStreamTransform() {
        attendanceDayEventTransformer = new AttendanceDayEventTransformer();
    }

    @Override
    public Action actionFor(final JsonEnvelope event) {
        if (eventAndJsonPaths.keySet().stream().anyMatch(eventToTransform -> event.metadata().name().equalsIgnoreCase(eventToTransform))) {
            return TRANSFORM;
        }

        return NO_ACTION;
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("----------------------event name------------ {}", event.metadata().name());
        }

        final JsonObject transformedPayload = attendanceDayEventTransformer.transform(event.metadata().name(), payload);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-------------------transformedPayload---------------{}", transformedPayload);
        }

        return of(envelopeFrom(metadataFrom(event.metadata()), transformedPayload));

    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        // no need
    }

    void setAttendanceDayEventTransformer(final AttendanceDayEventTransformer attendanceDayEventTransformer) {
        this.attendanceDayEventTransformer = attendanceDayEventTransformer;
    }


}
