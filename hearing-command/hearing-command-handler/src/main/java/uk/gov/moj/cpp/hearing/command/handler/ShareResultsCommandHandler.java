package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ShareResultsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ShareResultsCommandHandler.class.getName());

    @Inject
    private Clock clock;

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.save-draft-result event received {}", envelope.toObfuscatedDebugString());
        }
        final SaveDraftResultCommand command = convertToObject(envelope, SaveDraftResultCommand.class);
        final Stream<Object> events = Stream.of(DraftResultSaved.builder()
                .withHearingId(command.getHearingId())
                .withTargetId(command.getTargetId())
                .withDefendantId(command.getDefendantId())
                .withOffenceId(command.getOffenceId())
                .withDraftResult(command.getDraftResult())
                .build());
        this.eventSource.getStreamById(command.getHearingId()).append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

    @Handles("hearing.command.share-results")
    public void shareResult(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.share-results event received {}", envelope.toObfuscatedDebugString());
        }
        final ShareResultsCommand command = convertToObject(envelope, ShareResultsCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.shareResults(command, clock.now()));
    }

    @Handles("hearing.command.update-result-lines-status")
    public void updateResultLinesStatus(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-result-lines-status event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateResultLinesStatusCommand command = convertToObject(envelope, UpdateResultLinesStatusCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.updateResultLinesStatus(command));
    }
}