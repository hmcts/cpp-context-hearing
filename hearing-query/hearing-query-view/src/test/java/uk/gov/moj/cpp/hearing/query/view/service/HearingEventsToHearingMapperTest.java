package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent.hearingEvent;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventsToHearingMapperTest {

    @Test
    public void shouldRetrieveLatestHearingEvent() {

        final UUID hearingId = randomUUID();

        final HearingEvent hearingEvent_1 = hearingEvent()
                .setLastModifiedTime(now().minusMinutes(10))
                .setHearingId(hearingId);

        final ZonedDateTime expectedlastModifiedTime = now();
        final HearingEvent hearingEvent_2 = hearingEvent()
                .setLastModifiedTime(expectedlastModifiedTime)
                .setEventDate(LocalDate.now())
                .setHearingId(hearingId);

        final HearingEventsToHearingMapper hearingEventsToHearingMapper = new HearingEventsToHearingMapper(asList(hearingEvent_1, hearingEvent_2), asList(hearing().build()));

        final uk.gov.justice.core.courts.HearingEvent resultHearingEvent = hearingEventsToHearingMapper.getHearingEventBy(hearingId).get();

        assertThat(resultHearingEvent.getLastModifiedTime(), is(expectedlastModifiedTime));
    }
}