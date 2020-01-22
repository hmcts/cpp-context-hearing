package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.PublishCourtList;
import uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.PublishCourtListFields;
import uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportFailed;
import uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportSuccessful;
import uk.gov.moj.cpp.hearing.command.handler.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.domain.aggregate.CourtListAggregate;

import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class PublishCourtListStatusHandler extends AbstractCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishCourtListStatusHandler.class);

    @Inject
    private ReferenceDataService referenceDataService;

    @Inject
    private UtcClock clock;

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

    @Handles("hearing.command.publish-hearing-lists-for-crown-courts")
    public void publishHearingListsForCrownCourts(final JsonEnvelope commandEnvelope) {
        referenceDataService.getAllCrownCourtCentres(commandEnvelope)
                .forEach(courtCentreId -> publishFinalCourtList(commandEnvelope.metadata(), courtCentreId));
    }

    private void publishFinalCourtList(final Metadata commandMetaData, final UUID courtCentreId) {
        try {
            publishCourtList(envelopeFrom(commandMetaData, asJson(generatePublishCourtListCommand(courtCentreId))));
        } catch (EventStreamException | RuntimeException e) {
            // This should be robust, so to allow subsequent attempts.
            if (LOGGER.isErrorEnabled()) {
                final String message
                        = String.format(
                        "Exception thrown While trying to publish the final court list for Court Centre [%s]", courtCentreId);
                LOGGER.error(message, e);
            }
        }
    }

    private PublishCourtList generatePublishCourtListCommand(final UUID courtCentreId) {
        return PublishCourtList.publishCourtList()
                .withCourtCentreId(courtCentreId)
                .withRequestedTime(clock.now())
                .build();
    }

    public static JsonValue asJson(final PublishCourtList publishCourtList) {
        return Json.createObjectBuilder()
                .add(PublishCourtListFields.COURT_CENTRE_ID.getInternalName(), publishCourtList.getCourtCentreId().toString())
                .add(PublishCourtListFields.CREATED_TIME.getInternalName(), publishCourtList.getCreatedTime().toString())
                .build();
    }


}
