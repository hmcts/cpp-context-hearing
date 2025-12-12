package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.ReusableInfo;
import uk.gov.moj.cpp.hearing.command.ReusableInfoResults;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ReusableInfoCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ReusableInfoCommandHandler.class.getName());

    @Handles("hearing.command.reusable-info")
    public void saveReusableInfo(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.reusable-info event received {}", envelope.toObfuscatedDebugString());
        }

        final JsonArray reusablePrompts = envelope.payloadAsJsonObject().getJsonArray("reusablePrompts");
        final JsonArray reusableResults = envelope.payloadAsJsonObject().getJsonArray("reusableResults");

        final List<ReusableInfo> reusableInfoCaches = reusablePrompts != null ? convertToList(reusablePrompts, ReusableInfo.class) : emptyList();
        final List<ReusableInfoResults> reusableInfoResultsCaches = reusableResults != null ? convertToList(reusableResults, ReusableInfoResults.class) : emptyList();
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final EventStream eventStream = eventSource.getStreamById(hearingId);
        final HearingAggregate aggregate = aggregateService.get(eventStream, HearingAggregate.class);
        final Stream<Object> events = aggregate.saveReusableInfo(hearingId, reusableInfoCaches, reusableInfoResultsCaches);
        appendEventsToStream(envelope, eventStream, events);

    }
}
