package uk.gov.moj.cpp.hearing.domain.aggregate;

import static org.junit.Assert.fail;

import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationAggregateTest {


    @InjectMocks
    private ApplicationAggregate applicationAggregate;

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(applicationAggregate);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void testShouldRecordHearindIdAgainstApplication() throws Exception {
        final RegisteredHearingAgainstApplication registeredHearingAgainstApplication = RegisteredHearingAgainstApplication.builder()
                .withApplicationId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .build();

        final RegisteredHearingAgainstApplication registeredHearingAgainstApplicationTwo = RegisteredHearingAgainstApplication.builder()
                .withApplicationId(registeredHearingAgainstApplication.getApplicationId())
                .withHearingId(UUID.randomUUID())
                .build();

        Stream<Object> events = applicationAggregate.registerHearingId(registeredHearingAgainstApplication.getApplicationId(), registeredHearingAgainstApplication.getHearingId());
        List<Object> lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(1, lEvents.size());
        Object event;

        event = lEvents.get(0);
        Assert.assertEquals(event.getClass(), RegisteredHearingAgainstApplication.class);
        final RegisteredHearingAgainstApplication typedEvent = (RegisteredHearingAgainstApplication) event;
        Assert.assertEquals(typedEvent.getHearingId(), registeredHearingAgainstApplication.getHearingId());
        Assert.assertEquals(typedEvent.getApplicationId(), registeredHearingAgainstApplication.getApplicationId());
        Assert.assertEquals(registeredHearingAgainstApplication.getHearingId(), applicationAggregate.getHearingIds().get(0));

        events = applicationAggregate.registerHearingId(registeredHearingAgainstApplicationTwo.getApplicationId(), registeredHearingAgainstApplicationTwo.getHearingId());
        lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(1, lEvents.size());
        event = lEvents.get(0);
        Assert.assertEquals(event.getClass(), RegisteredHearingAgainstApplication.class);
        final RegisteredHearingAgainstApplication typedEventTwo = (RegisteredHearingAgainstApplication) event;
        Assert.assertEquals(typedEventTwo.getHearingId(), registeredHearingAgainstApplicationTwo.getHearingId());
        Assert.assertEquals(typedEventTwo.getApplicationId(), registeredHearingAgainstApplicationTwo.getApplicationId());
        Assert.assertEquals(registeredHearingAgainstApplicationTwo.getHearingId(), applicationAggregate.getHearingIds().get(1));


    }
}