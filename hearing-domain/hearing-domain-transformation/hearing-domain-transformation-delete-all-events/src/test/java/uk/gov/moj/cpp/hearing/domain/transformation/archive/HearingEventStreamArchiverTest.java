package uk.gov.moj.cpp.hearing.domain.transformation.archive;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.DEACTIVATE;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;

import org.junit.Before;
import org.junit.Test;


public class HearingEventStreamArchiverTest {

    private HearingEventStreamArchiver hearingEventStreamArchiver = new HearingEventStreamArchiver();

    private Enveloper enveloper = createEnveloper();

    @Before
    public void setup() {
        hearingEventStreamArchiver.setEnveloper(enveloper);
    }

    @Test
    public void shouldCreateInstanceOfEventTransformation() {
        assertThat(hearingEventStreamArchiver, is(instanceOf(EventTransformation.class)));
    }

    @Test
    public void shouldDeactivateActionForTheEvent() {
        final JsonEnvelope event = buildEnvelope("hearing.court-assigned");
        assertThat(hearingEventStreamArchiver.actionFor(event), is(DEACTIVATE));
    }

    private JsonEnvelope buildEnvelope(final String eventName) {
        return envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName),
                createObjectBuilder().add("field", "value").build());
    }

}
