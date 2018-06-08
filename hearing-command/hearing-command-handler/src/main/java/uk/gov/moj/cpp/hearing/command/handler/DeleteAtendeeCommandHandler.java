package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import javax.inject.Inject;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.AttendeeDeleted;

@ServiceComponent(COMMAND_HANDLER)
public class DeleteAtendeeCommandHandler extends AbstractCommandHandler {

    @Inject
    public DeleteAtendeeCommandHandler(final EventSource eventSource, final Enveloper enveloper, 
            final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.command.delete-attendee")
    public void onDeleteAttendee(final JsonEnvelope envelope) throws EventStreamException {
        final AttendeeDeleted attendeeDeleted = convertToObject(envelope, AttendeeDeleted.class);
        aggregate(NewModelHearingAggregate.class, attendeeDeleted.getHearingId(), envelope, a -> a.deleteAtendee(attendeeDeleted));
    }
}
