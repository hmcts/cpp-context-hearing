package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.handler.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.command.result.SaveMultipleResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class ShareResultsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ShareResultsCommandHandler.class.getName());

    @Inject
    private Clock clock;

    @Inject
    private ReferenceDataService referenceDataService;


    @Handles("hearing.command.save-draft-result")
    public void saveDraftResult(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.save-draft-result event received {}", envelope.toObfuscatedDebugString());
        }
        final Optional<String> userId = envelope.metadata().userId();
        final Target target = convertToObject(envelope, Target.class);
        if (target != null && userId.isPresent()) {

            aggregate(HearingAggregate.class, target.getHearingId(), envelope,
                    aggregate -> aggregate.saveDraftResults(fromString(userId.get()), target));
        }
    }

    @Handles("hearing.command.save-multiple-draft-results")
    @SuppressWarnings("squid:S3655")
    public void saveMultipleDraftResult(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.save-multiple-draft-results message received {}", envelope.toObfuscatedDebugString());
        }
        final SaveMultipleResultsCommand saveMultipleResultsCommand = convertToObject(envelope, SaveMultipleResultsCommand.class);
        final EventStream eventStream = eventSource.getStreamById(saveMultipleResultsCommand.getHearingId());
        final HearingAggregate hearingAggregate = aggregateService.get(eventStream, HearingAggregate.class);
        final Optional<String> userId = envelope.metadata().userId();
        eventStream.append(hearingAggregate.saveAllDraftResults(saveMultipleResultsCommand.getTargets(), fromString(userId.get())).map(Enveloper.toEnvelopeWithMetadataFrom(envelope)));
    }

    @Handles("hearing.command.share-results")
    public void shareResult(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.share-results event received {}", envelope.toObfuscatedDebugString());
        }
        final ShareResultsCommand command = convertToObject(envelope, ShareResultsCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> shareResultsEnrichedWithYouthCourt(aggregate, command));
    }

    private Stream<Object> shareResultsEnrichedWithYouthCourt(final HearingAggregate hearingAggregate, final ShareResultsCommand command ) {
        final Hearing hearing = hearingAggregate.getHearing();
        final YouthCourt youthCourt;
        if(hearing.getYouthCourtDefendantIds() != null && !hearing.getYouthCourtDefendantIds().isEmpty()) {
             youthCourt = referenceDataService.getYouthCourtForMagistrateCourt(hearing.getCourtCentre().getId());
        } else {
            youthCourt = null;
        }
        return hearingAggregate.shareResults(command.getHearingId(), command.getCourtClerk(), clock.now(), command.getResultLines(), command.getNewHearingState(), youthCourt);

    }

    @Handles("hearing.command.update-result-lines-status")
    public void updateResultLinesStatus(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-result-lines-status event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateResultLinesStatusCommand command = convertToObject(envelope, UpdateResultLinesStatusCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.updateResultLinesStatus(command.getHearingId(), command.getCourtClerk(), command.getLastSharedDateTime(), command.getSharedResultLines()));
    }
}