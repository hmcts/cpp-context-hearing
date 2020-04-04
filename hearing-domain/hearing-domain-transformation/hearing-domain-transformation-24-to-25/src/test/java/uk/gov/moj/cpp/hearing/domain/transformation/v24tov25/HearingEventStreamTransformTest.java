package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_EVENTS_INITIATED;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform.EventInstance;
import uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform.TransformFactory;

import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventStreamTransformTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventStreamTransformTest.class);

    private HearingEventStreamTransform underTest = new HearingEventStreamTransform();

    private Enveloper enveloper = createEnveloper();

    @Mock
    private TransformFactory factory;

    @Before
    public void setup() {
        underTest.setEnveloper(enveloper);
        underTest.setFactory(factory);
    }

    @Test
    public void shouldCreateInstanceOfEventTransformation() {
        assertThat(underTest, is(instanceOf(EventTransformation.class)));
    }

    @Test
    public void shouldSetActionToTransformForTheEventsThatMatch() {
        final JsonEnvelope event = buildEnvelope(HEARING_EVENTS_INITIATED);
        assertThat(underTest.actionFor(event), is(TRANSFORM));
    }

    @Test
    public void shouldSetActionToNoActionForTheEventsThatDoesNotMatch() {
        final JsonEnvelope event = buildEnvelope("hearing.events.other");
        assertThat(underTest.actionFor(event), is(NO_ACTION));
    }

    @Test
    public void shouldTransformHearingInitiatedEvent() {

        final EventInstance eventInstance = mock(EventInstance.class);

        final JsonObject transformedPayload = mock(JsonObject.class);

        final JsonEnvelope event = buildEnvelope(HEARING_EVENTS_INITIATED, "hearing.events.initiated.json");

        when(factory.getEventInstance(HEARING_EVENTS_INITIATED)).thenReturn(eventInstance);

        when(eventInstance.transform(Mockito.anyObject())).thenReturn(transformedPayload);

        final JsonEnvelope expected = underTest.apply(event).findFirst().get();

        verify(eventInstance).transform(Mockito.anyObject());

        assertNotNull(expected);
    }

    private JsonEnvelope buildEnvelope(final String eventName, final String payloadFileName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (final InputStream stream = loader.getResourceAsStream(payloadFileName);
             final JsonReader jsonReader = Json.createReader(stream)) {
            final JsonObject payload = jsonReader.readObject();
            return envelopeFrom(metadataBuilder().withId(randomUUID()).withName(eventName), payload);
        } catch (final IOException e) {
            LOGGER.warn("Error in reading payload {}", payloadFileName, e);
        }
        return null;
    }

    private JsonEnvelope buildEnvelope(final String eventName) {
        return envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName),
                createObjectBuilder().add("field", "value").build());
    }
}