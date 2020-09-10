package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.handler.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.UpdateInheritedVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.UpdateOffenceVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateVerdictCommandHandler extends AbstractCommandHandler {
    @Inject
    private ReferenceDataService referenceDataService;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdateVerdictCommandHandler.class.getName());

    @Handles("hearing.command.update-verdict")
    public void updateVerdict(final JsonEnvelope command) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-verdict event received {}", command.toObfuscatedDebugString());
        }

        final Set<String> guiltyPleaTypes = referenceDataService.retrieveGuiltyPleaTypes();

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = convertToObject(command, HearingUpdateVerdictCommand.class);

        final UUID hearingId = hearingUpdateVerdictCommand.getHearingId();

        for (final Verdict verdict : hearingUpdateVerdictCommand.getVerdicts()) {
            aggregate(HearingAggregate.class, hearingId, command,
                    hearingAggregate -> hearingAggregate.updateVerdict(hearingId, verdict, guiltyPleaTypes));
        }
    }

    @Handles("hearing.command.update-verdict-against-offence")
    public void updateOffenceVerdict(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-verdict-against-offence event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateOffenceVerdictCommand command = convertToObject(envelope, UpdateOffenceVerdictCommand.class);
        aggregate(OffenceAggregate.class, command.getVerdict().getOffenceId(), envelope,
                offenceAggregate -> offenceAggregate.updateVerdict(command.getHearingId(), command.getVerdict()));
    }

    @Handles("hearing.command.enrich-update-verdict-with-associated-hearings")
    public void updateInheritVerdict(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.enrich-update-verdict-with-associated-hearings event received {}", envelope.toObfuscatedDebugString());
        }

        final UpdateInheritedVerdictCommand command = convertToObject(envelope, UpdateInheritedVerdictCommand.class);

        for (final UUID hearingId : command.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope,
                    hearingAggregate -> hearingAggregate.inheritVerdict(hearingId, command.getVerdict()));
        }
    }
}
