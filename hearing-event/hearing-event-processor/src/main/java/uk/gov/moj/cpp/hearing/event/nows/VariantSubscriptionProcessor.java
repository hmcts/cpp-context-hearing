package uk.gov.moj.cpp.hearing.event.nows;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.notification.Subscription;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantSubscriptionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariantSubscriptionProcessor.class);

    private final SubscriptionClient subscriptionClient;

    private final NotificationRouter notificationRouter;

    @Inject
    public VariantSubscriptionProcessor(final SubscriptionClient subscriptionClient, final NotificationRouter notificationRouter) {
        this.subscriptionClient = subscriptionClient;
        this.notificationRouter = notificationRouter;
    }

    public void notifyVariantCreated(Sender sender, JsonEnvelope context, NowsNotificationDocumentState nowsNotificationDocumentState) {

        final List<Subscription> subscriptions = subscriptionClient.getAll(context, nowsNotificationDocumentState.getNowsTypeId(), LocalDate.now()).getSubscriptions();

        subscriptions.stream()
                .filter(subscription -> userGroupMatch(subscription, nowsNotificationDocumentState.getUsergroups()))
                .filter(subscription -> courtCentreMatch(subscription, nowsNotificationDocumentState.getOriginatingCourtCentreId()))
                .forEach(subscription -> notify(sender, context, nowsNotificationDocumentState, subscription));
    }

    private void notify(Sender sender, JsonEnvelope context, NowsNotificationDocumentState nowsNotificationDocumentState, Subscription subscription) {
        try {
            notificationRouter.notify(sender, context, subscription.getDestination(), subscription.getChannel(),
                    subscription.getChannelProperties(), nowsNotificationDocumentState);
        } catch (InvalidNotificationException ex) {
            LOGGER.error("failed to send notification to " + subscription.getDestination(), ex);
        }
    }

    private boolean userGroupMatch(final Subscription subscription, List<String> usergroups) {
        if (subscription.getUserGroups() == null || subscription.getUserGroups().isEmpty()) {
            return true;
        } else {
            return usergroups.stream().anyMatch(ug -> subscription.getUserGroups().contains(ug));
        }
    }

    private boolean courtCentreMatch(final Subscription subscription, final UUID courtCentreId) {
        if (subscription.getCourtCentreIds() == null || subscription.getCourtCentreIds().isEmpty()) {
            return true;
        } else {
            return subscription.getCourtCentreIds().contains(courtCentreId);
        }
    }

}
