package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionAggregateTest {

    @InjectMocks
    private SubscriptionAggregate subscriptionAggregate;

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(subscriptionAggregate);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void initiateUploadSubscriptions() {

        final UploadSubscriptionsCommand uploadSubscriptionsCommand = buildUploadSubscriptionsCommand();

        subscriptionAggregate.initiateUploadSubscriptions(uploadSubscriptionsCommand);
    }

    private UploadSubscriptionsCommand buildUploadSubscriptionsCommand() {

        final Map<String, String> properties = new HashMap<>();
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());

        final List<UUID> courtCentreIds = asList(randomUUID(), randomUUID());

        final List<UUID> nowTypeIds = asList(randomUUID(), randomUUID());

        final UploadSubscriptionCommand command = new UploadSubscriptionCommand();
        command.setChannel(STRING.next());
        command.setChannelProperties(properties);
        command.setDestination(STRING.next());
        command.setUserGroups(asList(STRING.next(), STRING.next()));
        command.setCourtCentreIds(courtCentreIds);
        command.setNowTypeIds(nowTypeIds);

        final UploadSubscriptionsCommand uploadSubscriptionsCommand = new UploadSubscriptionsCommand();
        uploadSubscriptionsCommand.setId(randomUUID());
        uploadSubscriptionsCommand.setSubscriptions(asList(command));
        uploadSubscriptionsCommand.setReferenceDate("01012018");
        return uploadSubscriptionsCommand;

    }
}