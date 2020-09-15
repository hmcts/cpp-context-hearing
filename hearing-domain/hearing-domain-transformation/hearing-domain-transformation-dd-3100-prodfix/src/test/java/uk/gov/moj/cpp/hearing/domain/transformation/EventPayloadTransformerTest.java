package uk.gov.moj.cpp.hearing.domain.transformation;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.domain.transformation.TestTargetIds.TEST_TARGET_IDS_TO_REPLACE;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class EventPayloadTransformerTest {

    private static final String EVENT_TO_TRANSFORM = "hearing.draft-result-saved";

    private EventPayloadTransformer transformer = new EventPayloadTransformer();

    @Test
    public void shouldTransformInvalidTargetIdToValid() {
        TEST_TARGET_IDS_TO_REPLACE.entrySet().forEach(entry -> {

            final JsonObject payload = getNewTarget(fromString(entry.getKey()));
            final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID(EVENT_TO_TRANSFORM), payload);

            final JsonObject transformedPayload = transformer.transform(jsonEnvelope);
            assertThat(transformedPayload.getJsonObject("target").getString("targetId"), is(entry.getValue()));
        });
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