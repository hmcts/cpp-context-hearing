package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2629"})
@ServiceComponent(COMMAND_HANDLER)
public class HearingVacatedTrialDetailUpdateCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingVacatedTrialDetailUpdateCommandHandler.class.getName());

    @Handles("hearing.update-vacated-trial-detail")
    public void changeHearingVacateTrialDetail(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.vacated-trial-detail-change event received {}", envelope.toObfuscatedDebugString());
        final HearingVacatedTrialDetailsUpdateCommand hearingVacateTrialDetailsChangeCommand = convertToObject(envelope, HearingVacatedTrialDetailsUpdateCommand.class);

        aggregate(HearingAggregate.class, hearingVacateTrialDetailsChangeCommand.getHearingId(), envelope, a -> a.updateHearingVacateTrialDetails(
                hearingVacateTrialDetailsChangeCommand.getHearingId(),
                hearingVacateTrialDetailsChangeCommand.getIsVacated(),
                hearingVacateTrialDetailsChangeCommand.getVacatedTrialReasonId()
        ));
    }
}

