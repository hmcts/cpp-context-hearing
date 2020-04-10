package uk.gov.moj.cpp.hearing.domain.transformation.corechanges;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.moj.cpp.hearing.domain.transformation.corechanges.transform.HearingEventTransformer;
import uk.gov.moj.cpp.hearing.domain.transformation.corechanges.transform.TransformFactory;

import javax.json.JsonObject;
import java.util.Arrays;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_DEFENDANT_ADDED;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventStreamTransformTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventStreamTransformTest.class);

    private final HearingEventStreamTransform underTest = new HearingEventStreamTransform();

    @Mock
    private TransformFactory transformFactory;

    @Before
    public void setup() {
        underTest.setTransformFactory(transformFactory);
        when(transformFactory.getEventTransformer(HEARING_DEFENDANT_ADDED)).thenReturn(Arrays.asList(mock(HearingEventTransformer.class)));
    }

    @Test
    public void shouldCreateInstanceOfEventTransformation() {
        assertThat(underTest, is(instanceOf(EventTransformation.class)));
    }

    @Test
    public void shouldSetActionToTransformForTheEventsThatMatch() {
        final JsonEnvelope event = buildEnvelope(HEARING_DEFENDANT_ADDED);
        assertThat(underTest.actionFor(event), is(TRANSFORM));
    }

    @Test
    public void shouldSetActionToNoActionForTheEventsThatDoesNotMatch() {
        final JsonEnvelope event = buildEnvelope("hearing.events.other");
        assertThat(underTest.actionFor(event), is(NO_ACTION));
    }

    @Test
    public void shouldTransformHearingInitiatedEvent() {
        final JsonObject jsonObject = mock(JsonObject.class);
        final JsonEnvelope event = buildEnvelopeWithPayload(HEARING_DEFENDANT_ADDED, jsonObject);
        final HearingEventTransformer hearingEventTransformer = mock(HearingEventTransformer.class);

        when(transformFactory.getEventTransformer(HEARING_DEFENDANT_ADDED)).thenReturn(Arrays.asList(hearingEventTransformer));

        underTest.apply(event);

        verify(hearingEventTransformer).transform(event.metadata(), jsonObject);

    }

    private JsonEnvelope buildEnvelope(final String eventName) {
        return envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName),
                createObjectBuilder().add("field", "value").build());
    }


    private JsonEnvelope buildEnvelopeWithPayload(final String eventName, final JsonObject jsonObject) {
        return envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName),
                jsonObject);
    }
}