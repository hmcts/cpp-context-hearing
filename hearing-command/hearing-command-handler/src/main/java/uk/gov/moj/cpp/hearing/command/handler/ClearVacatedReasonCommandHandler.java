package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.HearingVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ClearVacatedReasonCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClearVacatedReasonCommandHandler.class.getName());

    @Handles("hearing.command.clear-vacated-trial")
    public void vacatedTrialCleared(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.clear-vacated-trial{}", envelope.toObfuscatedDebugString());
        }

        final HearingVacatedTrialCleared hearingVacatedTrialCleared = convertToObject(envelope, HearingVacatedTrialCleared.class);

        aggregate(HearingAggregate.class, hearingVacatedTrialCleared.getHearingId(), envelope, a -> a.clearVacatedTrial(
                hearingVacatedTrialCleared.getHearingId()));
    }
}

