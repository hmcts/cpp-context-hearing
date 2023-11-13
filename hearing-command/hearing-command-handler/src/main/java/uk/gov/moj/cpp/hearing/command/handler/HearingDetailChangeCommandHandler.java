package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingAddWitnessCommand;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingAmendCommand;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Handles("hearing.command.amend")
    public void amendHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.amend event received {}", envelope.toObfuscatedDebugString());
        }

        final Optional<String> userId = envelope.metadata().userId();
        final HearingAmendCommand hearingAmendCommand = convertToObject(envelope, HearingAmendCommand.class);
        aggregate(HearingAggregate.class, hearingAmendCommand.getHearingId(), envelope,
                    aggregate -> aggregate.amendHearing(hearingAmendCommand.getHearingId(), fromString(userId.get()), hearingAmendCommand.getNewHearingState()));


    }

    @Handles("hearing.command.add-witness")
    public void addWitnessToHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.add-witnes command received {}", envelope.toObfuscatedDebugString());
        }

        final HearingAddWitnessCommand hearingAddWitnessCommand = convertToObject(envelope, HearingAddWitnessCommand.class);
        aggregate(HearingAggregate.class, hearingAddWitnessCommand.getHearingId(), envelope,
                aggregate -> aggregate.addWitnessToHearing(hearingAddWitnessCommand.getHearingId(), hearingAddWitnessCommand.getWitness()));
    }

}

