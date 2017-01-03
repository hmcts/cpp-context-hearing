package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException expectedException = none();

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Test
    public void shouldLogAnHearingEvent() {
        givenNoHearingEventsExist();

        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP));

        final HearingEvent hearingEvent = hearingEventRepository.findById(HEARING_EVENT_ID);

        assertThat(hearingEvent.getHearingId(), is(HEARING_ID));
        assertThat(hearingEvent.getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvent.getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvent.getTimestamp(), is(TIMESTAMP));
    }

    @Test
    public void shouldGetHearingLogForAHearing() {
        givenHearingEventsExist();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingId(HEARING_ID);
        assertThat(hearingEvents.size(), is(2));

        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID));
        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvents.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvents.get(0).getTimestamp(), is(TIMESTAMP));

        assertThat(hearingEvents.get(1).getHearingId(), is(HEARING_ID));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(hearingEvents.get(1).getTimestamp(), is(TIMESTAMP_2));
    }

    @Test
    public void shouldReturnEmptyEventsWhenNoHearingEventsExistForAHearing() {
        givenNoHearingEventsExist();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingId(HEARING_ID);

        assertThat(hearingEvents.size(), is(0));
    }

    @Test
    public void shouldThrowExceptionWhenHearingEventIsRequestedWhichDoesNotExist() {
        givenNoHearingEventsExist();
        expectedException.expect(NoResultException.class);
        expectedException.expectMessage("No entity found for query");

        hearingEventRepository.findById(HEARING_EVENT_ID);
    }

    private void givenHearingEventsExist() {
        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP));
        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID_2, HEARING_ID, RECORDED_LABEL_2, TIMESTAMP_2));

        final List<HearingEvent> hearingEvents = hearingEventRepository.findAll();
        assertThat(hearingEvents.size(), is(2));
    }

    private void givenNoHearingEventsExist() {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findAll();
        assertThat(hearingEvents.size(), is(0));
    }

}