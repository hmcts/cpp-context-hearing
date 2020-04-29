package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.enveloper.Enveloper.toEnvelopeWithMetadataFrom;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.AddRequestForOutstandingFines;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesRequested;

import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AddRequestForOutstandingFinesCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddRequestForOutstandingFinesCommandHandler.class.getName());

    @Handles("hearing.command.add-request-for-outstanding-fines")
    public void addRequestForOutstandingFines(final JsonEnvelope envelope) throws EventStreamException {

        final AddRequestForOutstandingFines addRequestForOutstandingFines = convertToObject(envelope, AddRequestForOutstandingFines.class);

        LOGGER.info("hearing.command.add-request-for-outstanding-fines with hearingDate {}", addRequestForOutstandingFines.getHearingDate());

        final EventStream eventStream = eventSource.getStreamById(UUID.randomUUID());

        final Stream<JsonEnvelope> newEvents =
                Stream.of(OutstandingFinesRequested.newBuilder()
                        .withHearingDate(addRequestForOutstandingFines.getHearingDate())
                        .build())
                        .map(toEnvelopeWithMetadataFrom(envelope));

        eventStream.append(newEvents);

    }
}