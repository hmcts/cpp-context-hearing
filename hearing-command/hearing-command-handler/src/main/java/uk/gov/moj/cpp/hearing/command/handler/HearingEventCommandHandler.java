package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.UpdateHearingEventsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventDefinitionAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(COMMAND_HANDLER)
public class HearingEventCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventCommandHandler.class.getName());

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope jsonEnvelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.create-hearing-event-definitions event received {}", jsonEnvelope.toObfuscatedDebugString());
        }

        final CreateHearingEventDefinitionsCommand createHearingEventDefinitionsCommand = convertToObject(jsonEnvelope, CreateHearingEventDefinitionsCommand.class);

        aggregate(HearingEventDefinitionAggregate.class, createHearingEventDefinitionsCommand.getId(), jsonEnvelope, a -> a.createEventDefinitions(createHearingEventDefinitionsCommand));
    }

    @Handles("hearing.command.log-hearing-event")
    public void logHearingEvent(final JsonEnvelope jsonEnvelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.log-hearing-event event received {}", jsonEnvelope.toObfuscatedDebugString());
        }

        final LogEventCommand logEventCommand = convertToObject(jsonEnvelope, LogEventCommand.class);

        aggregate(HearingAggregate.class, logEventCommand.getHearingId(), jsonEnvelope, a -> a.logHearingEvent(logEventCommand));
    }

    @Handles("hearing.command.update-hearing-events")
    public void updateHearingEvents(final JsonEnvelope jsonEnvelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-hearing-events event received {}", jsonEnvelope.toObfuscatedDebugString());
        }

        final UpdateHearingEventsCommand updateHearingEventsCommand = convertToObject(jsonEnvelope, UpdateHearingEventsCommand.class);

        aggregate(HearingAggregate.class, updateHearingEventsCommand.getHearingId(), jsonEnvelope, a -> a.updateHearingEvents(updateHearingEventsCommand));
    }

    @Handles("hearing.command.correct-hearing-event")
    public void correctEvent(final JsonEnvelope jsonEnvelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.correct-hearing-event event received {}", jsonEnvelope.toObfuscatedDebugString());
        }

        final CorrectLogEventCommand correctLogEventCommand = convertToObject(jsonEnvelope, CorrectLogEventCommand.class);

        aggregate(HearingAggregate.class, correctLogEventCommand.getHearingId(), jsonEnvelope, a -> a.correctHearingEvent(correctLogEventCommand));
    }
}
