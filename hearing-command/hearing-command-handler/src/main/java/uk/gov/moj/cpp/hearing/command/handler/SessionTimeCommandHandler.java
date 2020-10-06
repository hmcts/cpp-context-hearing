package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.command.sessiontime.RecordSessionTime;
import uk.gov.moj.cpp.hearing.common.SessionTimeUUIDService;
import uk.gov.moj.cpp.hearing.domain.aggregate.SessionTimeAggregate;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

@ServiceComponent(COMMAND_HANDLER)
public class SessionTimeCommandHandler extends AbstractCommandHandler {

    @Inject
    private UtcClock utcClock;

    @Inject
    private SessionTimeUUIDService uuidService;

    @Handles("hearing.command.record-session-time")
    public void recordSessionTime(final Envelope<RecordSessionTime> commandEnvelope) throws EventStreamException {

        final UUID courtHouseId = commandEnvelope.payload().getCourtHouseId();
        final UUID courtRoomId = commandEnvelope.payload().getCourtRoomId();
        final LocalDate courtSessionDate = ofNullable(commandEnvelope.payload().getCourtSessionDate()).orElse(LocalDate.now());
        final UUID courtSessionId = uuidService.getCourtSessionId(courtHouseId, courtRoomId, courtSessionDate);

        final EventStream eventStream = eventSource.getStreamById(courtSessionId);

        final SessionTimeAggregate aggregate
                = aggregateService.get(eventStream, SessionTimeAggregate.class);

        final Stream<Object> events = aggregate.recordSessionTime(courtSessionId, commandEnvelope.payload(), courtSessionDate);
        appendEventsToStream(commandEnvelope, eventStream, events);
    }
}
