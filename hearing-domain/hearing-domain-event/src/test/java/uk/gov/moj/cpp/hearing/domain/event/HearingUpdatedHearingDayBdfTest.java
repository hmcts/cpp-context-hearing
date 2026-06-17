package uk.gov.moj.cpp.hearing.domain.event;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.HearingDay;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class HearingUpdatedHearingDayBdfTest {

    @Test
    public void shouldReturnCorrectValuesFromConstructor() {
        final UUID hearingId = randomUUID();
        final HearingDay hearingDay = new HearingDay(randomUUID(), randomUUID(), false, true, 45, 2, now());

        final HearingUpdatedHearingDayBdf event = new HearingUpdatedHearingDayBdf(hearingId, hearingDay);

        assertThat(event.getHearingId(), is(hearingId));
        assertThat(event.getHearingDay(), is(hearingDay));
    }
}
