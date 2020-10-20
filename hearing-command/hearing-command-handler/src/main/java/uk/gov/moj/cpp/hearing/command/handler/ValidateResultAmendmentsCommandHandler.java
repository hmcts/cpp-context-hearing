package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ValidateResultAmendmentsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ValidateResultAmendmentsCommandHandler.class.getName());

    @Handles("hearing.command.validate-result-amendments")
    public void validateResultAmendments(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.request-approval event received {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("id"));
        final UUID userId = UUID.fromString(envelope.payloadAsJsonObject().getString("userId"));
        final ZonedDateTime validateResultAmendmentsTime = ZonedDateTimes.fromJsonString(envelope.payloadAsJsonObject().getJsonString("validateAmendmentsTime"));

        aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.validateResultsAmendments(hearingId, userId, validateResultAmendmentsTime));
    }
}

