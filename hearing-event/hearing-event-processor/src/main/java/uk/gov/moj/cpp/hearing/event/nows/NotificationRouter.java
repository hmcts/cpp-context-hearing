package uk.gov.moj.cpp.hearing.event.nows;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;

import javax.inject.Inject;


public class NotificationRouter {

    private final EmailNowNotificationChannel emailNowNotificationChannel;

    @Inject
    NotificationRouter(final EmailNowNotificationChannel emailNowNotificationChannel) {
        this.emailNowNotificationChannel = emailNowNotificationChannel;
    }

    public void notify(final Sender sender, final JsonEnvelope event, String destination,
                       String channelType, Map<String, String> properties, NowsNotificationDocumentState nowsNotificationDocumentState) throws InvalidNotificationException {

        if (EmailNowNotificationChannel.EMAIL_TYPE.equals(channelType)) {
            emailNowNotificationChannel.notify(sender, event, destination, properties, nowsNotificationDocumentState);
        } else {
            throw new IllegalArgumentException("invalid channel type: " + channelType);
        }
    }


}
