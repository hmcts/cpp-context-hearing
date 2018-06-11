package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateVerdictCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GenerateNowsCommandHandler.class.getName());

    @Handles("hearing.command.update-verdict")
    public void updateVerdict(final JsonEnvelope command) throws EventStreamException {
        LOGGER.debug("hearing.command.update-verdict event received {}", command.payloadAsJsonObject());

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = convertToObject(command, HearingUpdateVerdictCommand.class);

        for (Defendant defendant : hearingUpdateVerdictCommand.getDefendants()) {
            for (Offence offence : defendant.getOffences()) {

                aggregate(NewModelHearingAggregate.class, hearingUpdateVerdictCommand.getHearingId(), command,
                        hearingAggregate ->
                                hearingAggregate.updateVerdict(
                                        hearingUpdateVerdictCommand.getHearingId(),
                                        hearingUpdateVerdictCommand.getCaseId(),
                                        offence.getId(),
                                        offence.getVerdict()
                                )
                );
            }
        }
    }
}
