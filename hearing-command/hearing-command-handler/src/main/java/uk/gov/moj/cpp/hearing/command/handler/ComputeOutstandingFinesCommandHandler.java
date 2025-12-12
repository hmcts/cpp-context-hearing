package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.enveloper.Enveloper.toEnvelopeWithMetadataFrom;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OutstandingFinesQuery;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesQueried;

import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ComputeOutstandingFinesCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ComputeOutstandingFinesCommandHandler.class.getName());

    @Handles("hearing.command.compute-outstanding-fines")
    public void computeOutstandingFines(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.compute-outstanding-fines {}", envelope.toObfuscatedDebugString());
        }

        final OutstandingFinesQuery outstandingFinesQuery = convertToObject(envelope, OutstandingFinesQuery.class);

        final EventStream eventStream = eventSource.getStreamById(UUID.randomUUID());

        final Stream<JsonEnvelope> newEvents = Stream.of(outstandingFinesQuery)
                .map(this::createOutstandingFinesQueried)
                .map(toEnvelopeWithMetadataFrom(envelope));

        eventStream.append(newEvents);

    }

    private OutstandingFinesQueried createOutstandingFinesQueried(final OutstandingFinesQuery query) {
        return OutstandingFinesQueried.newBuilder()
                .withCourtCentreId(query.getCourtCentreId())
                .withCourtRoomIds(query.getCourtRoomIds())
                .withHearingDate(query.getHearingDate())
                .build();
    }

}