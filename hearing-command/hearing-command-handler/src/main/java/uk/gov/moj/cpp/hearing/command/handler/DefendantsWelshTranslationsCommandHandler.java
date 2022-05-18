package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.DefendantsWithWelshTranslationsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
@SuppressWarnings("squid:S1068")
public class DefendantsWelshTranslationsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefendantsWelshTranslationsCommandHandler.class.getName());

    @Handles("hearing.command.save-defendants-welsh-translations")
    public void saveDefendantsForWelshTranslations(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.save-defendants-welsh-translations event received {}", envelope.payloadAsJsonString());
        }
        final DefendantsWithWelshTranslationsCommand defendantsWithWelshTranslationsCommand = convertToObject(envelope, DefendantsWithWelshTranslationsCommand.class);

        final UUID hearingId = defendantsWithWelshTranslationsCommand.getHearingId();
        aggregate(
                HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.recordDefendantsWelshTranslation(defendantsWithWelshTranslationsCommand));
    }

}