package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Objects.isNull;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this is a proxy for notificationnotify.email in notify context
 * which is itself a proxy for external service sendEmail uk.gov.service.notify.NotificationRouter
 */
@SuppressWarnings({"squid:S2221","squid:S1162"})
public class EmailNowNotificationChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRouter.class);

    public static final String EMAIL_TYPE = "email";
    public static final String TEMPLATE_ID_PROPERTY_NAME = "template";
    public static final String NOTIFICATIONNOTIFY_EMAIL_METADATA_TYPE = "notificationnotify.send-email-notification";
    public static final String FROM_ADDRESS_PROPERTY_NAME = "fromAddress";

    public static final String CASE_URNS_PERSONALISATION_KEY = "caseUrns";
    public static final String COURT_CLERK_NAME_PERSONALISATION_KEY = "courtClerkName";
    public static final String DEFENDANT_NAME_PERSONALISATION_KEY = "defendantName";

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private Enveloper enveloper;

    public void notify(final Sender sender, final JsonEnvelope event, String destination, Map<String, String> properties, NowsNotificationDocumentState nowsNotificationDocumentState) throws InvalidNotificationException {

        final Notification emailNotification = new Notification();
        emailNotification.setNotificationId(UUID.randomUUID());

        emailNotification.setSendToAddress(destination);
        final String templateId = properties.get(TEMPLATE_ID_PROPERTY_NAME);

        if(isNull(templateId)) {
            throw new InvalidNotificationException(String.format("Null template id for \"%s\"", emailNotification.getSendToAddress()));
        }

        final Map<String, String> personalisation = new HashMap<>();
        personalisation.put(CASE_URNS_PERSONALISATION_KEY, nowsNotificationDocumentState.getCaseUrns().stream().collect(Collectors.joining(",")));
        personalisation.put(COURT_CLERK_NAME_PERSONALISATION_KEY, nowsNotificationDocumentState.getCourtClerkName());
        personalisation.put(DEFENDANT_NAME_PERSONALISATION_KEY, nowsNotificationDocumentState.getDefendantName());
        emailNotification.setPersonalisation(personalisation);

        try {
            emailNotification.setTemplateId(UUID.fromString(templateId));
        } catch (IllegalArgumentException ex) {
            throw new InvalidNotificationException(String.format("cant notify %s invalid template id: \"%s\"", emailNotification.getSendToAddress(), templateId), ex);
        }

        final String replyToAddress = properties.get(FROM_ADDRESS_PROPERTY_NAME);
        if (StringUtils.isEmpty(replyToAddress)) {
            throw new InvalidNotificationException(String.format("cant notify %s no %s property", emailNotification.getSendToAddress(), FROM_ADDRESS_PROPERTY_NAME));
        }
        emailNotification.setReplyToAddress(replyToAddress);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("sending - {} ", toString(emailNotification));
        }
        sender.send(this.enveloper.withMetadataFrom(event, NOTIFICATIONNOTIFY_EMAIL_METADATA_TYPE)
                .apply(this.objectToJsonObjectConverter.convert(emailNotification)));
    }

    private String toString(Notification notification) {
        return String.format("to: %s from: %s templateId: %s notificationId: %s personalization: %s",
                notification.getSendToAddress(),
                notification.getReplyToAddress(),
                notification.getTemplateId(),
                notification.getNotificationId(),
                notification.getPersonalisation().entrySet().stream()
                        .map(entry -> "" + entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(","))
        );
    }

}
