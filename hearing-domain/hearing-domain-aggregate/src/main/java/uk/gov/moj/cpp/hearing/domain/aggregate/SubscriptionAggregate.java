package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscription;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionUploaded;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionsUploaded;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubscriptionAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiateUploadSubscriptions(final UUID id, final List<UploadSubscription> subscriptions, final String referenceDate) {
        final SubscriptionsUploaded subscriptionsUploaded = new SubscriptionsUploaded(
                id,
                subscriptions.stream()
                        .map(this::convert)
                        .collect(Collectors.toList()),
                referenceDate);

        return apply(Stream.of(subscriptionsUploaded));
    }

    private SubscriptionUploaded convert(final UploadSubscription uploadSubscription) {
        return new SubscriptionUploaded(
                uploadSubscription.getId(),
                uploadSubscription.getChannel(),
                uploadSubscription.getChannelProperties(),
                uploadSubscription.getUserGroups(),
                uploadSubscription.getDestination(),
                uploadSubscription.getCourtCentreIds(),
                uploadSubscription.getNowTypeIds());
    }
}