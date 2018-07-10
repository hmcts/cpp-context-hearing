package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.SubscriptionAggregate;

import javax.inject.Inject;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class UploadSubscriptionsCommandHandler extends AbstractCommandHandler {

    private static final String STREAM_ID = "03136e43-566d-48f5-96f1-38a4c75cca71";

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UploadSubscriptionsCommandHandler.class.getName());

    @Handles("hearing.command.upload-subscriptions")
    public void uploadSubscriptions(final JsonEnvelope envelope) throws EventStreamException {

        LOGGER.debug("hearing.command.upload-subscriptions {}", envelope.payloadAsJsonObject());

        final UploadSubscriptionsCommand uploadSubscriptionsCommand = convertToObject(envelope, UploadSubscriptionsCommand.class);

        if (!isValidReferenceDate(uploadSubscriptionsCommand.getReferenceDate())) {

            LOGGER.error("Invalid reference date - {} ", uploadSubscriptionsCommand.getReferenceDate());

            return;
        }

        uploadSubscriptionsCommand.setId(UUID.randomUUID());

        uploadSubscriptionsCommand.getSubscriptions().forEach(s -> s.setId(UUID.randomUUID()));

        aggregate(SubscriptionAggregate.class,
                UUID.fromString(STREAM_ID),
                envelope,
                aggregate -> aggregate.initiateUploadSubscriptions(uploadSubscriptionsCommand));
    }

    private boolean isValidReferenceDate(final String referenceDate) {

        try {

            LocalDate.parse(referenceDate.trim(), DateTimeFormatter.ofPattern("ddMMyyyy"));

            return true;

        } catch (DateTimeException e) {

            LOGGER.error(String.format("Invalid reference date - %s ", referenceDate), e);

            return false;
        }
    }
}
