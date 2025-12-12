package uk.gov.moj.cpp.hearing.query.view.response;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary.TimelineHearingSummaryBuilder;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TimelineTest {

    @Test
    public void shouldSortSummariesByDate() {
        final TimelineHearingSummary hearingYesterday = new TimelineHearingSummaryBuilder().withHearingDate(now().minusDays(1L)).build();
        final TimelineHearingSummary hearingToday = new TimelineHearingSummaryBuilder().withHearingDate(now()).build();
        final TimelineHearingSummary hearingTomorrow = new TimelineHearingSummaryBuilder().withHearingDate(now().plusDays(1L)).build();
        final List<TimelineHearingSummary> hearingSummaries = asList(hearingToday, hearingTomorrow, hearingYesterday);
        final Timeline timeline = new Timeline(hearingSummaries);
        final List<TimelineHearingSummary> hearingSummaries1 = timeline.getHearingSummaries();
        assertThat(hearingSummaries1.get(0), is(hearingTomorrow));
        assertThat(hearingSummaries1.get(1), is(hearingToday));
        assertThat(hearingSummaries1.get(2), is(hearingYesterday));
    }
}