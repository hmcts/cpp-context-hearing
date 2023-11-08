package uk.gov.moj.cpp.hearing.domain.aggregate;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDefendantsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CourtApplicationEjected;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeletedForCourtApplication;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;

import java.util.Arrays;
import java.util.Collections;
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

    @Test
    public void testDeleteHearingForCourtApplication(){
        final UUID courtApplicationId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final HearingDeletedForCourtApplication event = (HearingDeletedForCourtApplication)applicationAggregate.deleteHearingForCourtApplication
                (courtApplicationId, hearingId).collect(Collectors.toList()).get(0);
        assertThat(event.getHearingId(), is(hearingId));
    }

    @Test
    public void testApplicationDefendantsUpdated_WhenHearingIdsNotEmpty() {
        ReflectionUtil.setField(applicationAggregate, "hearingIds", Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        final ApplicationDefendantsUpdated event = (ApplicationDefendantsUpdated)
                applicationAggregate.applicationDefendantsUpdated(new CourtApplication.Builder().build()).collect(Collectors.toList()).get(0);
        assertThat(event.getHearingIds(), is(applicationAggregate.getHearingIds()));
    }

    @Test
    public void testApplicationDefendantsUpdated_WhenHearingIdsIsEmpty() {
        final List<Object> event = applicationAggregate.applicationDefendantsUpdated(new CourtApplication.Builder().build()).collect(Collectors.toList());
        assertThat(event.size(), is(0));
    }

    @Test
    public void testEjectApplication_RaiseCourtApplicationEjected_WhenHearingIdsNotEmpty() {
        final UUID applicationId = UUID.randomUUID();
        ReflectionUtil.setField(applicationAggregate, "hearingIds", Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        final CourtApplicationEjected event = (CourtApplicationEjected)applicationAggregate.ejectApplication(applicationId, Collections.emptyList()).collect(Collectors.toList()).get(0);
        assertThat(event.getApplicationId(), is(applicationId));
    }

    @Test
    public void testEjectApplication_WhenHearingIdsIsEmpty() {
        final UUID applicationId = UUID.randomUUID();
        final List<Object> event = applicationAggregate.ejectApplication(applicationId, Collections.emptyList()).collect(Collectors.toList());
        assertThat(event.size(), is(0));
    }

    @Test
    public void testEjectApplication_RaiseCourtApplicationEjected_WhenHearingIdsPassedIsNotEmpty() {
        final UUID applicationId = UUID.randomUUID();
        final List<UUID> hearingIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        final List<Object> event = applicationAggregate.ejectApplication(applicationId, hearingIds).collect(Collectors.toList());
        final CourtApplicationEjected courtApplicationEjected = (CourtApplicationEjected)event.get(0);
        assertThat(courtApplicationEjected.getHearingIds(), is(hearingIds));
    }
}