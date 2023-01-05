package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import javax.json.JsonObject;

import java.util.UUID;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateCaseDefendantsHandler  extends  AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdateCaseDefendantsHandler.class.getName());
    private static final String HEARING_ID = "hearingId";

    @Handles("hearing.command.update-case-defendants")
    public void updateCaseDefendants(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-case-defendants event received {}", envelope.toObfuscatedDebugString());
        }
        final JsonObject payload = envelope.payloadAsJsonObject();
        final JsonObject prosecutionCaseJsonObject = payload.getJsonObject("prosecutionCase");
        final ProsecutionCase prosecutionCase = convertToObject(prosecutionCaseJsonObject, ProsecutionCase.class);

        final UUID caseId = fromString(prosecutionCaseJsonObject.getString("id"));
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.case-defendants-updated-for-hearing clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        aggregate(CaseAggregate.class, caseId, envelope,
                caseAggregate -> caseAggregate.caseDefendantsUpdated(prosecutionCase));
    }

    @Handles("hearing.command.update-case-defendants-for-hearing")
    public void updateCaseDefendantsForHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-case-defendants-for-hearing event received {}", envelope.toObfuscatedDebugString());
        }
        final JsonObject payload = envelope.payloadAsJsonObject();
        final ProsecutionCase prosecutionCase = convertToObject(payload.getJsonObject("prosecutionCase"), ProsecutionCase.class);

        final UUID hearingId = fromString(payload.getString(HEARING_ID));

        aggregate(HearingAggregate.class, hearingId, envelope,
                hearingAggregate -> hearingAggregate.updateCaseDefendantsForHearing(hearingId, prosecutionCase));
    }
}
