package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CourtApplicationHearingDeleted;


import java.util.UUID;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteCourtApplicationHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CourtApplicationHearingDeleted.class);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private Stream<Object> events;

    @Mock
    private HearingAggregate hearingAggregate;

    @InjectMocks
    private DeleteCourtApplicationHandler deleteCourtApplicationHandler;

    @Test
    public void shouldHandleDeleteCourtApplication() throws EventStreamException {
        final UUID hearingId = randomUUID();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(hearingAggregate.deleteCourtApplicationHearing(hearingId)).thenReturn(mock(Stream.class));
        final JsonObject payload = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build();
        final JsonEnvelope commandEnvelope = createEnvelope("hearing.command.delete-court-application-hearing", payload);


        deleteCourtApplicationHandler.handleDeleteCourtApplicationHearing(commandEnvelope);

        verify(hearingAggregate).deleteCourtApplicationHearing(hearingId);
    }

}
