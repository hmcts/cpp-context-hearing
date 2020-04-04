package uk.gov.moj.cpp.hearing.domain.transformation.logevent;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Stream.of;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transformation
public class HearingEventStreamTransform implements EventTransformation {

    private final List<String> eventsToTransform = newArrayList("hearing.hearing-event-definitions-created");

    private Enveloper enveloper;

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventStreamTransform.class);

    @Override
    public boolean isApplicable(final JsonEnvelope event) {
        return false;
    }

    @Override
    public Action actionFor(final JsonEnvelope event) {
        if(eventsToTransform.stream().anyMatch(eventToTransform -> event.metadata().name().equalsIgnoreCase(eventToTransform))) {
            return TRANSFORM;
        }

        return NO_ACTION;
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {
        LOGGER.error("Old event payload - {}", event.payloadAsJsonObject());
        final JsonObject jsonObject = buildHearingEventDefinitionsPayload();
        LOGGER.error("New event payload - {}", jsonObject);
        return of(envelopeFrom(metadataFrom(event.metadata()), jsonObject));
    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }

    private JsonObject buildHearingEventDefinitionsPayload() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (final InputStream stream = loader.getResourceAsStream("hearing-event-definitions.json");
             final JsonReader jsonReader = Json.createReader(stream)) {
            return jsonReader.readObject();
        } catch (final IOException e) {
            LOGGER.error("Error in reading payload hearing-event-definitions.json", e);
        }
        return null;
    }
}
