package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("squid:S2187")
@RunWith(MockitoJUnitRunner.class)
public class HearingDaySharedResultsTest extends TestCase {
    @Mock
    private Hearing hearing;

    @InjectMocks
    private HearingDaySharedResults hearingDaySharedResults;

    @Test
    public void shouldSetMatchingV2HearingDayHasSharedResultsToTrueWhenHearingResultsSharedIsTrue() {
        final HearingDay hearingDay1 = HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build();
        final HearingDay hearingDay2 = HearingDay.hearingDay().withSittingDay(ZonedDateTime.now().minusDays(3)).build();
        final List<HearingDay> hearingDays = new ArrayList();
        hearingDays.add(hearingDay1);
        hearingDays.add(hearingDay2);
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearing.getHasSharedResults()).thenReturn(true);
        final LocalDate hearingDay = LocalDate.now();
        final Hearing hearing1 = hearingDaySharedResults.setHasSharedResults(hearing, hearingDay);
        final List<HearingDay> hearingDays1 = hearing1.getHearingDays();
        assertThat(hearingDays1.size(), is(2));
        assertThat(hearingDays1.get(0).getHasSharedResults(), is(true));
        assertThat(hearingDays1.get(1).getHasSharedResults(), is(false));
    }

    @Test
    public void shouldSetMatchingV2HearingDayHasSharedResultsToFalseWhenHearingResultsSharedIsNull() {
        final HearingDay hearingDay1 = HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build();
        final HearingDay hearingDay2 = HearingDay.hearingDay().withSittingDay(ZonedDateTime.now().minusDays(3)).build();
        final List<HearingDay> hearingDays = new ArrayList();
        hearingDays.add(hearingDay1);
        hearingDays.add(hearingDay2);
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearing.getHasSharedResults()).thenReturn(null);
        final LocalDate hearingDay = LocalDate.now().minusDays(5);
        final Hearing hearing1 = hearingDaySharedResults.setHasSharedResults(hearing, hearingDay);
        final List<HearingDay> hearingDays1 = hearing1.getHearingDays();
        assertThat(hearingDays1.size(), is(2));
        assertThat(hearingDays1.get(0).getHasSharedResults(), is(false));
        assertThat(hearingDays1.get(1).getHasSharedResults(), is(false));
    }

    @Test
    public void shouldSetNonMatchingV2HearingDayHasSharedResultsToFalseWhenHearingResultsSharedIsTrue() {
        final HearingDay hearingDay1 = HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build();
        final HearingDay hearingDay2 = HearingDay.hearingDay().withSittingDay(ZonedDateTime.now().minusDays(3)).build();
        final List<HearingDay> hearingDays = new ArrayList();
        hearingDays.add(hearingDay1);
        hearingDays.add(hearingDay2);
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearing.getHasSharedResults()).thenReturn(null);
        final LocalDate hearingDay = LocalDate.now().minusDays(5);
        final Hearing hearing1 = hearingDaySharedResults.setHasSharedResults(hearing, hearingDay);
        final List<HearingDay> hearingDays1 = hearing1.getHearingDays();
        assertThat(hearingDays1.size(), is(2));
        assertThat(hearingDays1.get(0).getHasSharedResults(), is(false));
        assertThat(hearingDays1.get(1).getHasSharedResults(), is(false));
    }
}