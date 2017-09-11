package uk.gov.moj.cpp.hearing.persist;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
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
    private static final boolean ALTERABLE = BOOLEAN.next();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime LAST_MODIFIED_TIME = PAST_ZONED_DATE_TIME.next();

    private static final UUID HEARING_EVENT_ID_2 = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_2 = randomUUID();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final ZonedDateTime EVENT_TIME_2 = EVENT_TIME.plusMinutes(1);
    private static final ZonedDateTime LAST_MODIFIED_TIME_2 = PAST_ZONED_DATE_TIME.next();

    private static final UUID HEARING_EVENT_ID_3 = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_3 = randomUUID();
    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final ZonedDateTime EVENT_TIME_3 = EVENT_TIME_2.plusMinutes(1);
    private static final ZonedDateTime LAST_MODIFIED_TIME_3 = PAST_ZONED_DATE_TIME.next();

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Test
    public void shouldLogAnHearingEvent() {
        givenNoHearingEventsExist();

        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID, HEARING_EVENT_DEFINITION_ID, HEARING_ID, RECORDED_LABEL, EVENT_TIME, LAST_MODIFIED_TIME, ALTERABLE));

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findOptionalById(HEARING_EVENT_ID);

        assertThat(hearingEvent.isPresent(), is(true));
        assertThat(hearingEvent.orElse(null).getHearingId(), is(HEARING_ID));
        assertThat(hearingEvent.orElse(null).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID));
        assertThat(hearingEvent.orElse(null).getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvent.orElse(null).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvent.orElse(null).getEventTime(), is(EVENT_TIME));
        assertThat(hearingEvent.orElse(null).getLastModifiedTime(), is(LAST_MODIFIED_TIME));
        assertThat(hearingEvent.orElse(null).isDeleted(), is(false));
    }

    @Test
    public void shouldNotBeAbleToSeeADeletedHearingEvent() {
        givenNoHearingEventsExist();

        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID, HEARING_EVENT_DEFINITION_ID, HEARING_ID, RECORDED_LABEL, EVENT_TIME, LAST_MODIFIED_TIME, ALTERABLE).builder().delete().build());

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findOptionalById(HEARING_EVENT_ID);

        assertThat(hearingEvent.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEventsForAHearingWhichAreNotDeleted() {
        givenHearingEventsExistWithDeletedOnes();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID);
        assertThat(hearingEvents.size(), is(2));

        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID));
        assertThat(hearingEvents.get(0).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID));
        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvents.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvents.get(0).getEventTime(), is(EVENT_TIME));
        assertThat(hearingEvents.get(0).getLastModifiedTime(), is(LAST_MODIFIED_TIME));
        assertThat(hearingEvents.get(0).isDeleted(), is(false));

        assertThat(hearingEvents.get(1).getHearingId(), is(HEARING_ID));
        assertThat(hearingEvents.get(1).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_2));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(hearingEvents.get(1).getEventTime(), is(EVENT_TIME_2));
        assertThat(hearingEvents.get(1).getLastModifiedTime(), is(LAST_MODIFIED_TIME_2));
        assertThat(hearingEvents.get(1).isDeleted(), is(false));
    }

    @Test
    public void shouldReturnEmptyEventsWhenNoHearingEventsExistForAHearing() {
        givenNoHearingEventsExist();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID);

        assertThat(hearingEvents.size(), is(0));
    }

    @Test
    public void shouldNotThrowExceptionWhenHearingEventIsRequestedWhichDoesNotExist() {
        givenNoHearingEventsExist();

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findOptionalById(HEARING_EVENT_ID);

        assertThat(hearingEvent.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEventsInChronologicalOrderByEventTime() {
        givenHearingEventsExistInRandomOrder();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID);
        assertThat(hearingEvents.size(), is(3));

        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(2).getId(), is(HEARING_EVENT_ID_3));
    }

    private void givenHearingEventsExistWithDeletedOnes() {
        final List<HearingEvent> hearingEvents = newArrayList(
                new HearingEvent(HEARING_EVENT_ID, HEARING_EVENT_DEFINITION_ID, HEARING_ID, RECORDED_LABEL, EVENT_TIME, LAST_MODIFIED_TIME, ALTERABLE),
                new HearingEvent(HEARING_EVENT_ID_2, HEARING_EVENT_DEFINITION_ID_2, HEARING_ID, RECORDED_LABEL_2, EVENT_TIME_2, LAST_MODIFIED_TIME_2, ALTERABLE),
                new HearingEvent(HEARING_EVENT_ID_3, HEARING_EVENT_DEFINITION_ID_3, HEARING_ID, RECORDED_LABEL_3, EVENT_TIME_3, LAST_MODIFIED_TIME_3, ALTERABLE).builder().delete().build());

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(3));
    }

    private void givenHearingEventsExistInRandomOrder() {
        final List<HearingEvent> hearingEvents = newArrayList(
                new HearingEvent(HEARING_EVENT_ID, HEARING_EVENT_DEFINITION_ID, HEARING_ID, RECORDED_LABEL, EVENT_TIME, LAST_MODIFIED_TIME, ALTERABLE),
                new HearingEvent(HEARING_EVENT_ID_2, HEARING_EVENT_DEFINITION_ID_2, HEARING_ID, RECORDED_LABEL_2, EVENT_TIME_2, LAST_MODIFIED_TIME_2, ALTERABLE),
                new HearingEvent(HEARING_EVENT_ID_3, HEARING_EVENT_DEFINITION_ID_3, HEARING_ID, RECORDED_LABEL_3, EVENT_TIME_3, LAST_MODIFIED_TIME_3, ALTERABLE));

        shuffle(hearingEvents, new Random(LONG.next()));

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(3));
    }

    private void givenNoHearingEventsExist() {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findAll();
        assertThat(hearingEvents.size(), is(0));
    }

}