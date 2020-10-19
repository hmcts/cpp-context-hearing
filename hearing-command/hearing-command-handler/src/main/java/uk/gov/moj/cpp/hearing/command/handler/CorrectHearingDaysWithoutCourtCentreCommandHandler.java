package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;

import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

@ServiceComponent(COMMAND_HANDLER)
@SuppressWarnings("squid:CallToDeprecatedMethod")
public class CorrectHearingDaysWithoutCourtCentreCommandHandler extends AbstractCommandHandler {

    @Handles("hearing.command.correct-hearing-days-without-court-centre")
    public void correctHearingDaysWithoutCourtCentre(final JsonEnvelope commandEnvelope) throws EventStreamException {

        final JsonObject payload = commandEnvelope.payloadAsJsonObject();
        final HearingDaysWithoutCourtCentreCorrected hearingDaysWithoutCourtCentreCorrected = convertToObject(payload, HearingDaysWithoutCourtCentreCorrected.class);
        final Stream<Object> events = streamOf(hearingDaysWithoutCourtCentreCorrected);

        final UUID streamId = hearingDaysWithoutCourtCentreCorrected.getId();
        final EventStream eventStream = eventSource.getStreamById(streamId);

        eventStream.append(events.map(enveloper.withMetadataFrom(commandEnvelope)));
    }

}
