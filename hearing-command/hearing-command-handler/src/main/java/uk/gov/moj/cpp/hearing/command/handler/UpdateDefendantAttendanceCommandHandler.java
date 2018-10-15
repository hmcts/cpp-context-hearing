package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateDefendantAttendanceCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdateDefendantAttendanceCommandHandler.class.getName());

    @Handles("hearing.update-defendant-attendance-on-hearing-day")
    public void updateDefendantAttendance(final JsonEnvelope command) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-defendant-attendance-on-hearing-day {}", command.toObfuscatedDebugString());
        }

        final UpdateDefendantAttendanceCommand updateDefendantAttendanceCommand = convertToObject(command, UpdateDefendantAttendanceCommand.class);
        final UUID hearingId = updateDefendantAttendanceCommand.getHearingId();

        aggregate(HearingAggregate.class, hearingId, command,
                hearingAggregate -> hearingAggregate.updateDefendantAttendance(updateDefendantAttendanceCommand.getHearingId(),
                        updateDefendantAttendanceCommand.getDefendantId(),
                        updateDefendantAttendanceCommand.getAttendanceDay())
        );

    }
}
