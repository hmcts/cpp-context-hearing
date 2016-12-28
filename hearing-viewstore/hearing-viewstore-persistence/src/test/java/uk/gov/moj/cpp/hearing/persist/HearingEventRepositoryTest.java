package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class HearingEventRepositoryTest extends BaseTransactionalTest {

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime TIMESTAMP = PAST_ZONED_DATE_TIME.next();

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Test
    public void shouldLogAnHearingEvent() {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findAll();
        assertThat(hearingEvents.size(), is(0));

        hearingEventRepository.save(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP));

        final HearingEvent hearingEvent = hearingEventRepository.findById(HEARING_EVENT_ID);

        assertThat(hearingEvent.getId(), is(HEARING_EVENT_ID));
        assertThat(hearingEvent.getHearingId(), is(HEARING_ID));
        assertThat(hearingEvent.getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvent.getTimestamp(), is(TIMESTAMP));
    }

}