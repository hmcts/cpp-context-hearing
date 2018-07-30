package uk.gov.moj.cpp.hearing.domain.transformation.archive;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.DEACTIVATE;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import java.util.List;
import java.util.stream.Stream;


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
    public void shouldCreateRightActionForTheEventToBeArchievedThatIsAlreadyArchived() {
        final JsonEnvelope event = buildEnvelope("hearing.old-event-occurred.archived.1.9.release");
        assertThat(hearingEventStreamArchiver.actionFor(event), is(new Action(true, true, false)));
    }

    @Test
    public void shouldCreateRightActionForTheEventToBeArchivedThatIsListedNow() {
        final JsonEnvelope event = buildEnvelope("hearing.court-assigned");
        assertThat(hearingEventStreamArchiver.actionFor(event), is(DEACTIVATE));
    }

    @Test
    public void shouldCreateRightActionForTheEventNotToBeArchived() {
        final JsonEnvelope event = buildEnvelope("hearing.event-not-to-be-archived");
        assertThat(hearingEventStreamArchiver.actionFor(event), is(NO_ACTION));
    }

    @Test
    public void shouldCreateTransformationForAlreadyArchivedEvent() {
        final JsonEnvelope event = buildEnvelope("hearing.old-event-occurred.archived.1.9.release");

        final Stream<JsonEnvelope> transformedStream = hearingEventStreamArchiver.apply(event);

        final List<JsonEnvelope> transformedEvents = transformedStream.collect(toList());
        assertThat(transformedEvents, hasSize(1));
        assertThat(transformedEvents.get(0).metadata().name(), is("hearing.old-event-occurred"));
        assertThat(transformedEvents.get(0).payloadAsJsonObject().getString("field"),
                is(event.payloadAsJsonObject().getString("field")));
    }

    @Test
    public void shouldCreateTransformationForNewEvent() {
        final JsonEnvelope event = buildEnvelope("hearing.new-event-occurred");

        final Stream<JsonEnvelope> transformedStream = hearingEventStreamArchiver.apply(event);

        final List<JsonEnvelope> transformedEvents = transformedStream.collect(toList());
        assertThat(transformedEvents, hasSize(1));
        assertThat(transformedEvents.get(0).metadata().name(), is("hearing.new-event-occurred"));
        assertThat(transformedEvents.get(0).payloadAsJsonObject().getString("field"),
                is(event.payloadAsJsonObject().getString("field")));
    }

    private JsonEnvelope buildEnvelope(final String eventName) {
        return envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName),
                createObjectBuilder().add("field", "value").build());
    }

}
