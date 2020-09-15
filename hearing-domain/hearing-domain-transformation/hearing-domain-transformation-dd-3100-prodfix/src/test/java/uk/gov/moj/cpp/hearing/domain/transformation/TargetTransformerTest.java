package uk.gov.moj.cpp.hearing.domain.transformation;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.domain.transformation.TestTargetIds.TEST_TARGET_IDS_TO_REPLACE;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;

import java.util.UUID;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TargetTransformerTest {

    private static final String EVENT_TO_TRANSFORM = "hearing.draft-result-saved";

    private TargetTransformer targetTransformer;

    @Mock
    private EventPayloadTransformer eventPayloadTransformer;

    @Mock
    private JsonObject mockTransformedPayload;

    @Before
    public void setup() {
        targetTransformer = new TargetTransformer();
        targetTransformer.setEventPayloadTransformer(eventPayloadTransformer);
    }

    @Test
    public void actionFor_transformEventWithInvalidTargetId() {
        TEST_TARGET_IDS_TO_REPLACE.keySet().forEach(t -> {
            final JsonObject payload = getNewTarget(fromString(t));
            final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(EVENT_TO_TRANSFORM), payload);

            final Action action = targetTransformer.actionFor(jsonEnvelope);
            assertThat(action.isTransform(), is(true));
        });

    }

    @Test
    public void apply() {
        final JsonObject payload = getNewTarget(randomUUID());
        final JsonEnvelope originalEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(EVENT_TO_TRANSFORM), payload);
        when(eventPayloadTransformer.transform(originalEnvelope)).thenReturn(mockTransformedPayload);
        final Stream<JsonEnvelope> returnedEnvelopeStream = targetTransformer.apply(originalEnvelope);
        verify(eventPayloadTransformer).transform(originalEnvelope);

        final JsonEnvelope returnedEnvelope = returnedEnvelopeStream.findFirst().get();
        assertThat(returnedEnvelope.metadata(), is(originalEnvelope.metadata()));
        assertThat(returnedEnvelope.payloadAsJsonObject(), is(mockTransformedPayload));
    }

    private JsonObject getNewTarget(final UUID targetId) {
        return Json.createObjectBuilder().add("target",
                Json.createObjectBuilder()
                        .add("hearingId", randomUUID().toString())
                        .add("offenceId", randomUUID().toString())
                        .add("defendantId", randomUUID().toString())
                        .add("targetId", targetId.toString()))
                .build();
    }

}