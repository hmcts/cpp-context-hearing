package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class GenerateNowsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GenerateNowsCommandHandler.class.getName());

    @Handles("hearing.command.generate-nows")
    public void generateNows(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.generate-nows event received {}", envelope.toObfuscatedDebugString());
        }
        final NowsRequested nowsRequested = convertToObject(envelope, NowsRequested.class);
        aggregate(HearingAggregate.class, nowsRequested.getHearing().getId(), envelope, a -> a.generateNows(nowsRequested));
    }

    @Handles("hearing.command.save-nows-variants")
    public void saveNowsVariants(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.save-nows-variants event received {}", envelope.toObfuscatedDebugString());
        }
        final SaveNowsVariantsCommand saveNowsVariantsCommand = convertToObject(envelope, SaveNowsVariantsCommand.class);
        aggregate(HearingAggregate.class, saveNowsVariantsCommand.getHearingId(), envelope, a -> a.saveNowsVariants(saveNowsVariantsCommand.getHearingId(), saveNowsVariantsCommand.getVariants()));
    }

    @Handles("hearing.command.update-nows-material-status")
    public void nowsGenerated(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-nows-material-status {}", envelope.toObfuscatedDebugString());
        }
        final NowsMaterialStatusUpdated nowsRequested = convertToObject(envelope, NowsMaterialStatusUpdated.class);
        aggregate(HearingAggregate.class, nowsRequested.getHearingId(), envelope, a -> a.nowsMaterialStatusUpdated(nowsRequested));
    }
}
