package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.PublishCourtList;
import uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportFailed;
import uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportSuccessful;
import uk.gov.moj.cpp.hearing.domain.aggregate.CourtListAggregate;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

@ServiceComponent(COMMAND_HANDLER)
public class PublishCourtListStatusHandler extends AbstractCommandHandler {

    @Inject
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Handles("hearing.command.record-court-list-export-successful")
    public void recordCourtListExportSuccessful(final JsonEnvelope commandEnvelope) throws EventStreamException {
        final RecordCourtListExportSuccessful recordCourtListExportSuccessful =
                jsonObjectConverter.convert(commandEnvelope.payloadAsJsonObject(), RecordCourtListExportSuccessful.class);
        final UUID courtCentreId = recordCourtListExportSuccessful.getCourtCentreId();
        final EventStream eventStream = eventSource.getStreamById(courtCentreId);
        final CourtListAggregate aggregate = aggregateService.get(eventStream, CourtListAggregate.class);
        final Stream<Object> events = aggregate.recordCourtListExportSuccessful(
                recordCourtListExportSuccessful.getCourtCentreId(),
                recordCourtListExportSuccessful.getCourtListFileName(),
                recordCourtListExportSuccessful.getCreatedTime());
        appendEventsToStream(commandEnvelope, eventStream, events);
    }

    @Handles("hearing.command.record-court-list-export-failed")
    public void recordCourtListExportFailed(final JsonEnvelope commandEnvelope) throws EventStreamException {
        final RecordCourtListExportFailed recordCourtListExportFailed =
                jsonObjectConverter.convert(commandEnvelope.payloadAsJsonObject(), RecordCourtListExportFailed.class);
        final UUID courtCentreId = recordCourtListExportFailed.getCourtCentreId();
        final EventStream eventStream = eventSource.getStreamById(courtCentreId);
        final CourtListAggregate aggregate = aggregateService.get(eventStream, CourtListAggregate.class);
        final Stream<Object> events = aggregate.recordCourtListExportFailed(
                recordCourtListExportFailed.getCourtCentreId(),
                recordCourtListExportFailed.getCourtListFileName(),
                recordCourtListExportFailed.getCreatedTime(),
                recordCourtListExportFailed.getErrorMessage());
        appendEventsToStream(commandEnvelope, eventStream, events);
    }

    @Handles("hearing.command.publish-court-list")
    public void publishCourtList(final JsonEnvelope commandEnvelope) throws EventStreamException {
        final PublishCourtList publishCourtList =
                jsonObjectConverter.convert(commandEnvelope.payloadAsJsonObject(), PublishCourtList.class);
        final UUID courtCentreId = publishCourtList.getCourtCentreId();
        final EventStream eventStream = eventSource.getStreamById(courtCentreId);
        final CourtListAggregate courtListAggregate = aggregateService.get(eventStream, CourtListAggregate.class);
        final Stream<Object> events = courtListAggregate.recordCourtListRequested(
                courtCentreId,
                publishCourtList.getCreatedTime());
        appendEventsToStream(commandEnvelope, eventStream, events);
    }
}
