package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateVerdictCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GenerateNowsCommandHandler.class.getName());

    @Handles("hearing.command.update-verdict")
    public void updateVerdict(final JsonEnvelope command) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-verdict event received {}", command.toObfuscatedDebugString());
        }

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = convertToObject(command, HearingUpdateVerdictCommand.class);

        final UUID hearingId = hearingUpdateVerdictCommand.getHearingId();

        for (final Verdict verdict : hearingUpdateVerdictCommand.getVerdicts()) {

            aggregate(HearingAggregate.class, hearingId, command,
                    hearingAggregate -> hearingAggregate.updateVerdict(hearingId, verdict)
            );

        }
    }
}
