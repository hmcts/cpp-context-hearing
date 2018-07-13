package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionUploaded;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionsUploaded;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;

public class SubscriptionAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiateUploadSubscriptions(final UploadSubscriptionsCommand uploadSubscriptionsCommand) {
        final SubscriptionsUploaded subscriptionsUploaded = new SubscriptionsUploaded(
                uploadSubscriptionsCommand.getId(),
                uploadSubscriptionsCommand.getSubscriptions().stream()
                        .map(this::convert)
                        .collect(Collectors.toList()),
                uploadSubscriptionsCommand.getReferenceDate());

        return apply(Stream.of(subscriptionsUploaded));
    }

    private SubscriptionUploaded convert(final UploadSubscriptionCommand uploadSubscriptionCommand) {
        return new SubscriptionUploaded(
                uploadSubscriptionCommand.getId(),
                uploadSubscriptionCommand.getChannel(),
                uploadSubscriptionCommand.getChannelProperties(),
                uploadSubscriptionCommand.getUserGroups(),
                uploadSubscriptionCommand.getDestination(),
                uploadSubscriptionCommand.getCourtCentreIds(),
                uploadSubscriptionCommand.getNowTypeIds());
    }
}