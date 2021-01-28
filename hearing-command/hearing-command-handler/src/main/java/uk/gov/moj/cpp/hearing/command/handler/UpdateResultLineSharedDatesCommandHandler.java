package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingResultLineSharedDatesUpdated;

import javax.json.JsonObject;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.enveloper.Enveloper.toEnvelopeWithMetadataFrom;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateResultLineSharedDatesCommandHandler extends AbstractCommandHandler {

    @Handles("hearing.command.update-resultline-shared-dates")
    public void updateResultLineSharedDates(final JsonEnvelope commandEnvelope) throws EventStreamException {

        final JsonObject payload = commandEnvelope.payloadAsJsonObject();
        final HearingResultLineSharedDatesUpdated hearingResultLineSharedDatesUpdated = convertToObject(payload, HearingResultLineSharedDatesUpdated.class);
        final Stream<Object> events = streamOf(hearingResultLineSharedDatesUpdated);

        final UUID streamId = hearingResultLineSharedDatesUpdated.getHearingId();
        final EventStream eventStream = eventSource.getStreamById(streamId);

        eventStream.append(events.map(toEnvelopeWithMetadataFrom(commandEnvelope)));
    }
}
