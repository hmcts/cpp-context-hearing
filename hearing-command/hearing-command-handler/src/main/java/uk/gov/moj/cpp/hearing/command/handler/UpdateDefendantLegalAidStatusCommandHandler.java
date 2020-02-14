package uk.gov.moj.cpp.hearing.command.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import javax.json.JsonObject;
import java.util.UUID;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateDefendantLegalAidStatusCommandHandler extends AbstractCommandHandler {

    private static final String DEFENDANT_ID = "defendantId";
    private static final String LEGAL_AID_STATUS = "legalAidStatus";
    public static final String HEARING_ID = "hearingId";


    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdateDefendantLegalAidStatusCommandHandler.class.getName());

    @Handles("hearing.command.update-defendant-legalaid-status")
    public void updateDefendantLegalAidStatus (final JsonEnvelope envelope) throws EventStreamException  {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-defendant-legalaid-status event received {}", envelope.toObfuscatedDebugString());
        }
        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID defendantId = fromString(payload.getString(DEFENDANT_ID));
        final String legalAidStatus = payload.getString(LEGAL_AID_STATUS);

        aggregate(DefendantAggregate.class,
                defendantId,
                envelope,
                defendantAggregate -> defendantAggregate.updateDefendantLegalAidStatus(defendantId, legalAidStatus));

    }

    @Handles("hearing.command.update-defendant-legalaid-status-for-hearing")
    public void updateDefendantLegalAidStatusForHearing (final JsonEnvelope envelope) throws EventStreamException  {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-defendant-legalaid-status-for-hearing event received {}", envelope.toObfuscatedDebugString());
        }
        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID defendantId = fromString(payload.getString(DEFENDANT_ID));
        final String legalAidStatus = payload.getString(LEGAL_AID_STATUS);
        final UUID hearingId = fromString(payload.getString(HEARING_ID));

        aggregate(HearingAggregate.class,
                hearingId,
                envelope,
                hearingAggregate -> hearingAggregate.updateDefendantLegalAidStatusForHearing(hearingId, defendantId, legalAidStatus));

    }
}
