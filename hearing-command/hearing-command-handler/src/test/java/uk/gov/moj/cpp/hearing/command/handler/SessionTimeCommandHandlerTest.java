package uk.gov.moj.cpp.hearing.command.handler;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Stream.of;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.sessiontime.CourtSession;
import uk.gov.moj.cpp.hearing.command.sessiontime.RecordSessionTime;
import uk.gov.moj.cpp.hearing.common.SessionTimeUUIDService;
import uk.gov.moj.cpp.hearing.domain.aggregate.SessionTimeAggregate;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.SessionTimeRecorded;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SessionTimeCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            SessionTimeRecorded.class
    );

    @Mock
    private UtcClock utcClock;

    @Mock
    private SessionTimeUUIDService uuidService;

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private SessionTimeAggregate recordSessionTimeAggregate;

    @InjectMocks
    private SessionTimeCommandHandler recordSessionTimeHandler;

    @Test
    public void shouldCreateEventRecordSessionTimeHandler() throws EventStreamException, IOException {

        final Metadata metadata = Envelope
                .metadataBuilder().withName("hearing.command.record-session-time")
                .withId(randomUUID())
                .build();

        final UUID courtHouseId = randomUUID();
        final UUID courtRoomId = randomUUID();

        final RecordSessionTime payload = recordSessionTime(courtHouseId, courtRoomId);
        final Envelope<RecordSessionTime> envelope = envelopeFrom(metadata, payload);

        final UUID courtSessionId = randomUUID();
        final LocalDate courtSessionDate = now();
        when(utcClock.now()).thenReturn(ZonedDateTime.now());
        when(uuidService.getCourtSessionId(any(UUID.class), any(UUID.class), any(LocalDate.class))).thenReturn(courtSessionId);
        when(eventSource.getStreamById(courtSessionId)).thenReturn(eventStream);
        when(aggregateService.get(eq(eventStream), any())).thenReturn(recordSessionTimeAggregate);
        when(recordSessionTimeAggregate.recordSessionTime(courtSessionId, payload, courtSessionDate))
                .thenReturn(of(SessionTimeRecorded.sessionTimeRecorded().build()));

        recordSessionTimeHandler.recordSessionTime(envelope);

        verify(recordSessionTimeAggregate).recordSessionTime(courtSessionId, payload, courtSessionDate);
        final List<JsonEnvelope> collect = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        final JsonEnvelope actualEventProduced = collect.get(0);
        Assert.assertEquals("hearing.event.session-time-recorded", actualEventProduced.metadata().name());
    }

    private RecordSessionTime recordSessionTime(final UUID courtHouseId, final UUID courtRoomId) {

        final CourtSession courtSession = new CourtSession();
        courtSession.setCourtAssociateId(randomUUID());
        courtSession.setCourtClerkId(randomUUID());
        courtSession.setLegalAdviserId(randomUUID());
        courtSession.setEndTime("");
        courtSession.setStartTime("");

        return new RecordSessionTime(courtHouseId, courtRoomId, courtSession, courtSession);
    }

}