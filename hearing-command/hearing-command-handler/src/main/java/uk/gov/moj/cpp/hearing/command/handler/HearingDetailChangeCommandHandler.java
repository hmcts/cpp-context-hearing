package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.hearingDetails.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

@ServiceComponent(COMMAND_HANDLER)
public class HearingDetailChangeCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingDetailChangeCommandHandler.class.getName());

    @Handles("hearing.change-hearing-detail")
    public void changeHearingDetail(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.change-hearing-detail event received {}", envelope.toObfuscatedDebugString());
        }

        final HearingDetailsUpdateCommand hearingDetailsUpdateCommand = convertToObject(envelope, HearingDetailsUpdateCommand.class);

        aggregate(HearingAggregate.class, hearingDetailsUpdateCommand.getHearing().getId(), envelope, a -> a.updateHearingDetails(
                hearingDetailsUpdateCommand.getHearing().getId(),
                hearingDetailsUpdateCommand.getHearing().getType(),
                hearingDetailsUpdateCommand.getHearing().getCourtCentre(),
                hearingDetailsUpdateCommand.getHearing().getJurisdictionType(),
                hearingDetailsUpdateCommand.getHearing().getReportingRestrictionReason(),
                hearingDetailsUpdateCommand.getHearing().getHearingLanguage(),
                hearingDetailsUpdateCommand.getHearing().getHearingDays(),
                hearingDetailsUpdateCommand.getHearing().getJudiciary()
        ));
    }
}

