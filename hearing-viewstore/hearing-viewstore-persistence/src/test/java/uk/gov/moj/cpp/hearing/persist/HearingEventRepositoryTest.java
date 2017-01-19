package uk.gov.moj.cpp.hearing.persist;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.LONG;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class HearingEventRepositoryTest extends BaseTransactionalTest {

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime TIMESTAMP = PAST_ZONED_DATE_TIME.next();

    private static final UUID HEARING_EVENT_ID_2 = randomUUID();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final ZonedDateTime TIMESTAMP_2 = TIMESTAMP.plusMinutes(1);

    private static final UUID HEARING_EVENT_ID_3 = randomUUID();
    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final ZonedDateTime TIMESTAMP_3 = TIMESTAMP_2.plusMinutes(1);

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Test
    public void shouldLogAnHearingEvent() {
        givenNoHearingEventsExist();

        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP));

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findById(HEARING_EVENT_ID);

        assertThat(hearingEvent.isPresent(), is(true));
        assertThat(hearingEvent.get().getHearingId(), is(HEARING_ID));
        assertThat(hearingEvent.get().getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvent.get().getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvent.get().getTimestamp(), is(TIMESTAMP));
        assertThat(hearingEvent.get().isDeleted(), is(false));
    }

    @Test
    public void shouldNotSeeADeletedHearingEvent() {
        givenNoHearingEventsExist();

        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP).builder().delete().build());

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findById(HEARING_EVENT_ID);

        assertThat(hearingEvent.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEventsForAHearingWhichAreNotDeleted() {
        givenHearingEventsExistWithDeletedOnes();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingId(HEARING_ID);
        assertThat(hearingEvents.size(), is(2));

        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID));
        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvents.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvents.get(0).getTimestamp(), is(TIMESTAMP));
        assertThat(hearingEvents.get(0).isDeleted(), is(false));

        assertThat(hearingEvents.get(1).getHearingId(), is(HEARING_ID));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(hearingEvents.get(1).getTimestamp(), is(TIMESTAMP_2));
        assertThat(hearingEvents.get(1).isDeleted(), is(false));
    }

    @Test
    public void shouldReturnEmptyEventsWhenNoHearingEventsExistForAHearing() {
        givenNoHearingEventsExist();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingId(HEARING_ID);

        assertThat(hearingEvents.size(), is(0));
    }

    @Test
    public void shouldNotThrowExceptionWhenHearingEventIsRequestedWhichDoesNotExist() {
        givenNoHearingEventsExist();

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findById(HEARING_EVENT_ID);

        assertThat(hearingEvent.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEventsInChronologicalOrder() {
        givenHearingEventsExistInRandomOrder();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingId(HEARING_ID);
        assertThat(hearingEvents.size(), is(3));

        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(2).getId(), is(HEARING_EVENT_ID_3));
    }

    private void givenHearingEventsExistWithDeletedOnes() {
        final List<HearingEvent> hearingEvents = newArrayList(
                new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP),
                new HearingEvent(HEARING_EVENT_ID_2, HEARING_ID, RECORDED_LABEL_2, TIMESTAMP_2),
                new HearingEvent(HEARING_EVENT_ID_3, HEARING_ID, RECORDED_LABEL_3, TIMESTAMP_3).builder().delete().build());

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(3));
    }

    private void givenHearingEventsExistInRandomOrder() {
        final List<HearingEvent> hearingEvents = newArrayList(
                new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP),
                new HearingEvent(HEARING_EVENT_ID_2, HEARING_ID, RECORDED_LABEL_2, TIMESTAMP_2),
                new HearingEvent(HEARING_EVENT_ID_3, HEARING_ID, RECORDED_LABEL_3, TIMESTAMP_3));

        shuffle(hearingEvents, new Random(LONG.next()));

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(3));
    }

    private void givenNoHearingEventsExist() {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findAll();
        assertThat(hearingEvents.size(), is(0));
    }

}