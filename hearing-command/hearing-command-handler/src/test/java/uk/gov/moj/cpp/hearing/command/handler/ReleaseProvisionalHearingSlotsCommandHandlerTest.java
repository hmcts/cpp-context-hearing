package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReleaseProvisionalHearingSlotsCommandHandlerTest {

    private static final String HEARING_COMMAND = "hearing.command.release-provisional-hearing-slots";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            ReleaseProvisionalHearingSlots.class
    );

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private ReleaseProvisionalHearingSlotsCommandHandler handler;

    @Test
    public void shouldRaiseReleaseEventForTheBooking() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final String bookingId = randomUUID().toString();

        when(eventSource.getStreamById(hearingId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(new HearingAggregate());

        final JsonObject payload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("bookingId", bookingId)
                .build();

        handler.releaseProvisionalHearingSlots(envelopeFrom(
                metadataWithRandomUUID(HEARING_COMMAND), payload));

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList());

        assertThat(events, hasSize(1));
        assertThat(events.get(0).metadata().name(), is("hearing.event.release-provisional-hearing-slots"));
        assertThat(events.get(0).asJsonObject().getString("hearingId"), is(hearingId.toString()));
        assertThat(events.get(0).asJsonObject().getString("bookingId"), is(bookingId));
    }
}
