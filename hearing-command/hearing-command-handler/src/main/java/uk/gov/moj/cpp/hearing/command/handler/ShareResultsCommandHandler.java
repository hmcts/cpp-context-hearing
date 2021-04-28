package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

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
import uk.gov.moj.cpp.hearing.command.result.SaveMultipleDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveMultipleResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateDaysResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.Optional;
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

    @Inject
    private ReferenceDataService referenceDataService;


    @Handles("hearing.command.save-draft-result")
    public void saveDraftResult(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.save-draft-result command received {}", envelope.toObfuscatedDebugString());
        }
        final Optional<String> userId = envelope.metadata().userId();
        final Target target = convertToObject(envelope, Target.class);
        if (target != null && userId.isPresent()) {
            aggregate(HearingAggregate.class, target.getHearingId(), envelope,
                    aggregate -> aggregate.saveDraftResults(fromString(userId.get()), target));
        }
    }

    @Handles("hearing.command.save-days-draft-result")
    public void saveDraftResultForHearingDay(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.save-days-draft-result command received {}", envelope.toObfuscatedDebugString());
        }
        final Optional<String> userId = envelope.metadata().userId();
        final Target target = convertToObject(envelope, Target.class);
        if (target != null && userId.isPresent()) {
            aggregate(HearingAggregate.class, target.getHearingId(), envelope,
                    aggregate -> aggregate.saveDraftResultForHearingDay(fromString(userId.get()), target));
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

    @Handles("hearing.command.save-days-draft-results")
    @SuppressWarnings("squid:S3655")
    public void saveMultipleDraftResultsForHearingDay(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.save-days-draft-results message received {}", envelope.toObfuscatedDebugString());
        }
        final SaveMultipleDaysResultsCommand saveMultipleResultsCommand = convertToObject(envelope, SaveMultipleDaysResultsCommand.class);
        final EventStream eventStream = eventSource.getStreamById(saveMultipleResultsCommand.getHearingId());
        final HearingAggregate hearingAggregate = aggregateService.get(eventStream, HearingAggregate.class);
        final Optional<String> userId = envelope.metadata().userId();
        eventStream.append(hearingAggregate.saveMultipleDraftResultsForHearingDay(saveMultipleResultsCommand.getTargets(), saveMultipleResultsCommand.getHearingDay(), fromString(userId.get())).map(enveloper.withMetadataFrom(envelope)));
    }

    @Handles("hearing.command.share-results")
    public void shareResult(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.share-results command received {}", envelope.toObfuscatedDebugString());
        }
        final ShareResultsCommand command = convertToObject(envelope, ShareResultsCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> shareResultsEnrichedWithYouthCourt(aggregate, command));
    }

    /**
     * This command will be removed as part of this ticket(DD-10609).
     * This command is replaced by {@link ShareResultsCommandHandler#shareResultForDay(JsonEnvelope)}
     *
     * @param envelope
     * @throws EventStreamException
     */
    @Handles("hearing.command.share-results-v2")
    public void shareResultV2(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.share-results-v2 command received {}", envelope.toObfuscatedDebugString());
        }
        final ShareDaysResultsCommand command = convertToObject(envelope, ShareDaysResultsCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.shareResultsV2(command.getHearingId(), command.getCourtClerk(), clock.now(), command.getResultLines()));
    }

    @Handles("hearing.command.share-days-results")
    public void shareResultForDay(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.share-days-results command received {}", envelope.toObfuscatedDebugString());
        }
        final ShareDaysResultsCommand command = convertToObject(envelope, ShareDaysResultsCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> shareDaysResultsEnrichedWithYouthCourt(aggregate, command));
    }

    @Handles("hearing.command.update-result-lines-status")
    public void updateResultLinesStatus(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-result-lines-status command received {}", envelope.toObfuscatedDebugString());
        }

        final UpdateResultLinesStatusCommand command = convertToObject(envelope, UpdateResultLinesStatusCommand.class);

        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.updateResultLinesStatus(command.getHearingId(), command.getCourtClerk(), command.getLastSharedDateTime(), command.getSharedResultLines()));

    }

    @Handles("hearing.command.update-days-result-lines-status")
    public void updateDaysResultLinesStatus(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-days-result-lines-status command received {}", envelope.toObfuscatedDebugString());
        }

        final UpdateDaysResultLinesStatusCommand command = convertToObject(envelope, UpdateDaysResultLinesStatusCommand.class);

        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.updateDaysResultLinesStatus(command.getHearingId(), command.getCourtClerk(), command.getLastSharedDateTime(), command.getSharedResultLines(), command.getHearingDay()));

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

    private Stream<Object> shareDaysResultsEnrichedWithYouthCourt(final HearingAggregate hearingAggregate, final ShareDaysResultsCommand command ) {

        final Hearing hearing = hearingAggregate.getHearing();
        final YouthCourt youthCourt;

        if (isNotEmpty(hearing.getYouthCourtDefendantIds())) {
            youthCourt = referenceDataService.getYouthCourtForMagistrateCourt(hearing.getCourtCentre().getId());
        } else {
            youthCourt = null;
        }

        return hearingAggregate.shareResultForDay(command.getHearingId(), command.getCourtClerk(), clock.now(), command.getResultLines(), command.getNewHearingState(), youthCourt, command.getHearingDay());

    }

}
