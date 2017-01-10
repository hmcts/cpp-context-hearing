package uk.gov.moj.cpp.hearing.domain.aggregate;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.Before;
import org.junit.Test;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;

public class HearingEventsLogAggregateTest {

    private static final UUID HEARING_ID = UUID.randomUUID();
    private static final UUID HEARING_EVENT_ID = UUID.randomUUID();
    private static final String RECORDED_LABEL = "Hearing Started";
    private static final ZonedDateTime TIMESTAMP = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime DIFFERENT_TIMESTAMP = PAST_ZONED_DATE_TIME.next();


    private HearingEventsLogAggregate aggregate;

    @Before
    public void setup() {
        aggregate = new HearingEventsLogAggregate();
    }

    @Test
    public void shouldGenerateOneEventIfWeLogANewHearing() {
        Stream<Object> stream = aggregate.logHearingEvent(HEARING_ID, HEARING_EVENT_ID, RECORDED_LABEL, TIMESTAMP);

        assertThat(stream.count(), is(1L));
    }

    @Test
    public void shouldLogAHearingEventIfItDoesNotExist() {
        Stream<Object> stream = aggregate.logHearingEvent(HEARING_ID, HEARING_EVENT_ID, RECORDED_LABEL, TIMESTAMP);
        HearingEventLogged loggedEvent = (HearingEventLogged)stream.findFirst().get();

        assertThat(loggedEvent.getId(), is(HEARING_EVENT_ID));
        assertThat(loggedEvent.getHearingId(), is(HEARING_ID));
        assertThat(loggedEvent.getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(loggedEvent.getTimestamp(), is(TIMESTAMP));
    }

    @Test
    public void shouldNotLogAHearingEventWithADuplicateId() {
        aggregate.logHearingEvent(HEARING_ID, HEARING_EVENT_ID, RECORDED_LABEL, TIMESTAMP);
        Stream<Object> stream = aggregate.logHearingEvent(HEARING_ID, HEARING_EVENT_ID, RECORDED_LABEL, TIMESTAMP);

        assertThat(stream.count(), is(0L));
    }

    @Test
    public void shouldGenerateOneEventIfWeCorrectAnExistingEvent() {
        aggregate.logHearingEvent(HEARING_ID, HEARING_EVENT_ID, RECORDED_LABEL, TIMESTAMP);
        Stream<Object> stream = aggregate.correctEvent(HEARING_ID, HEARING_EVENT_ID, DIFFERENT_TIMESTAMP);

        assertThat(stream.count(), is(1L));
    }

    @Test
    public void shouldCorrectAHearingEventIfTheTimeIsChanged() {
        aggregate.logHearingEvent(HEARING_ID, HEARING_EVENT_ID, RECORDED_LABEL, TIMESTAMP);
        Stream<Object> stream = aggregate.correctEvent(HEARING_ID, HEARING_EVENT_ID, DIFFERENT_TIMESTAMP);

        HearingEventCorrected loggedEvent = (HearingEventCorrected) stream.findFirst().get();

        assertThat(loggedEvent.getTimestamp(), is(DIFFERENT_TIMESTAMP));
        assertThat(loggedEvent.getHearingId(), is(HEARING_ID));
        assertThat(loggedEvent.getHearingEventId(), is(HEARING_EVENT_ID));
    }

    @Test
    public void shouldNotGenerateEventsIfWeThereAreNoChanges() {
        aggregate.logHearingEvent(HEARING_ID, HEARING_EVENT_ID, RECORDED_LABEL, TIMESTAMP);
        Stream<Object> stream = aggregate.correctEvent(HEARING_ID, HEARING_EVENT_ID, TIMESTAMP);

        assertThat(stream.count(), is(0L));
    }

    @Test
    public void shouldNotGenerateEventsIfWeAreChangingANonExistentEvent() {
        Stream<Object> stream = aggregate.correctEvent(HEARING_ID, HEARING_EVENT_ID, DIFFERENT_TIMESTAMP);

        assertThat(stream.count(), is(0L));
    }

}
