package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithApplicationDetails;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithFinancialOrderDetails;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateDefendantCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdateDefendantCommandHandler.class.getName());

    @Handles("hearing.update-case-defendant-details")
    public void initiateCaseDefendantDetailsChange(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-case-defendant-details event received {}", envelope.toObfuscatedDebugString());
        }
        final CaseDefendantDetails caseDefendantDetails = convertToObject(envelope, CaseDefendantDetails.class);

        for (final Defendant defendant : caseDefendantDetails.getDefendants()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("INV: will cause hearing.defendant-details-updated clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
            }

            final CaseDefendantDetailsCommand caseDefendantDetailsCommand =
                    CaseDefendantDetailsCommand.caseDefendantDetailsCommand().setDefendant(defendant);

            aggregate(DefendantAggregate.class,
                    defendant.getId(),
                    envelope,
                    defendantAggregate -> defendantAggregate.enrichCaseDefendantDetailsWithHearingIds(caseDefendantDetailsCommand.getDefendant()));
        }
    }

    @Handles("hearing.update-case-defendant-details-against-hearing-aggregate")
    public void updateCaseDefendantDetails(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-case-defendant-details-against-hearing-aggregate event received {}", envelope.toObfuscatedDebugString());
        }

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings = convertToObject(envelope, CaseDefendantDetailsWithHearings.class);

        for (final UUID hearingId : caseDefendantDetailsWithHearings.getHearingIds()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("INV: command handler will cause hearing.defendant-details-updated clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
            }

            final CaseDefendantDetailsWithHearingCommand defendantWithHearingCommand =
                    CaseDefendantDetailsWithHearingCommand.caseDefendantDetailsWithHearingCommand()
                            .setHearingId(hearingId)
                            .setDefendant(caseDefendantDetailsWithHearings.getDefendant());

            aggregate(HearingAggregate.class, hearingId, envelope,
                    hearingAggregate -> hearingAggregate.updateDefendantDetails(defendantWithHearingCommand.getHearingId(), defendantWithHearingCommand.getDefendant()));

        }
    }

    @Handles("hearing.command.update-defendant-with-financial-order")
    public void updateCaseDefendantWithFinancialOrderDetails(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-defendant-with-financial-order command received {}", envelope.payload());
        }

        final UpdateDefendantWithFinancialOrderDetails updateDefendantWithFinancialOrderDetails
                = convertToObject(envelope.payloadAsJsonObject(), UpdateDefendantWithFinancialOrderDetails.class);

        aggregate(DefendantAggregate.class, updateDefendantWithFinancialOrderDetails.getFinancialOrderForDefendant().getDefendantId(), envelope,
                defendantAggregate -> defendantAggregate.updateDefendantWithFinancialOrder(updateDefendantWithFinancialOrderDetails.getFinancialOrderForDefendant()));

    }

    @Handles("hearing.command.update-defendant-with-application-details")
    public void updateCaseDefendantWithApplicationDetails(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-defendant-with-application-details event received {}", envelope.payload());
        }
        final UpdateDefendantWithApplicationDetails updateDefendantWithApplicationDetails = convertToObject(envelope.payloadAsJsonObject(), UpdateDefendantWithApplicationDetails.class);

        aggregate(DefendantAggregate.class,
                updateDefendantWithApplicationDetails.getDefendantId(),
                envelope,
                defendantAggregate -> defendantAggregate.updateDefendantWithApplicationDetails(updateDefendantWithApplicationDetails));

    }


}
