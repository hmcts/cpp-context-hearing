package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.enveloper.Enveloper.toEnvelopeWithMetadataFrom;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;


import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class DeleteCourtApplicationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCourtApplicationHandler.class);

    @Inject
    private EventSource eventSource;

    @Inject
    private AggregateService aggregateService;

    @Handles("hearing.command.delete-court-application-hearing")
    public void handleDeleteCourtApplicationHearing(final JsonEnvelope command) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'command.command.delete-court-application-hearing' received with payload {}", command.toObfuscatedDebugString());
        }

        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString("hearingId"));

        updateHearingEventStream(command, hearingId, (HearingAggregate hearing) ->
                hearing.deleteCourtApplicationHearing(hearingId));
    }

    private void updateHearingEventStream(final JsonEnvelope command, final UUID applicationId,
                                              final Function<HearingAggregate, Stream<Object>> aggregatorFunction) throws EventStreamException {
        final EventStream eventStream = eventSource.getStreamById(applicationId);
        final HearingAggregate hearing = aggregateService.get(eventStream, HearingAggregate.class);

        final Stream<Object> events = aggregatorFunction.apply(hearing);
        eventStream.append(events.map(toEnvelopeWithMetadataFrom(command)));
    }

}
