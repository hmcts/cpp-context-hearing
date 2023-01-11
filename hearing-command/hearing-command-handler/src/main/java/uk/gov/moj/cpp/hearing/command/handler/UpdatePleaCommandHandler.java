package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.handler.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdateInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdateOffencePleaCommand;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@ServiceComponent(COMMAND_HANDLER)
public class UpdatePleaCommandHandler extends AbstractCommandHandler {

    @Inject
    private ReferenceDataService referenceDataService;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdatePleaCommandHandler.class.getName());

    @Handles("hearing.hearing-offence-plea-update")
    public void updatePlea(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-offence-plea-update event received {}", envelope.toObfuscatedDebugString());
        }
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("hearing.hearing-offence-plea-update event received {}", envelope.payloadAsJsonObject());
        }

        final Set<String> guiltyPleaTypes = referenceDataService.retrieveGuiltyPleaTypes();
        final UpdatePleaCommand command = convertToObject(envelope, UpdatePleaCommand.class);
        for (final PleaModel plea : command.getPleas()) {
            aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                    hearingAggregate -> hearingAggregate.updatePlea(command.getHearingId(), plea, guiltyPleaTypes));
        }
    }

    @Handles("hearing.command.update-plea-against-offence")
    public void updateOffencePlea(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-plea-against-offence event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateOffencePleaCommand command = convertToObject(envelope, UpdateOffencePleaCommand.class);
        final PleaModel pleaModel = command.getPleaModel();
        aggregate(OffenceAggregate.class, pleaModel.getOffenceId(), envelope,
                offenceAggregate -> offenceAggregate.updatePlea(command.getHearingId(), pleaModel));
    }

    @Handles("hearing.command.enrich-update-plea-with-associated-hearings")
    public void updateInheritPlea(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.enrich-update-plea-with-associated-hearings event received {}", envelope.toObfuscatedDebugString());
        }

        final UpdateInheritedPleaCommand command = convertToObject(envelope, UpdateInheritedPleaCommand.class);

        for (final UUID hearingId : command.getHearingIds()) {
            aggregate(HearingAggregate.class, hearingId, envelope,
                    hearingAggregate -> hearingAggregate.inheritPlea(hearingId, command.getPlea()));
        }
    }
}