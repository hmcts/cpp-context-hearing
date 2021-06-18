package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.DayOfWeek.SUNDAY;
import static java.time.LocalDate.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.PublicHoliday;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("squid:S2187")
@RunWith(MockitoJUnitRunner.class)
public class PublicHolidaysWeekendsServiceTest extends TestCase {
    private static final String ENGLAND_AND_WALES_DIVISION = "england-and-wales";

    @Mock
    private JsonObject publicHoliday;

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    PublicHolidaysWeekendsService publicHolidaysWeekendsService;

    @Test
    public void shouldReturnFridayAsPreviousDayIfExpiryDateOnASundayAndNotAPublicHoliday() {
        final LocalDate ctlExpiryOnSunday = getCTLExpiryOnSunday();

        when(referenceDataService.getPublicHolidays(ENGLAND_AND_WALES_DIVISION, now(), ctlExpiryOnSunday)).thenReturn(getPublicHolidaysMonday(ctlExpiryOnSunday));

        final LocalDate adjustedCTLExpiryDate = publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryOnSunday);
        assertThat(adjustedCTLExpiryDate, is(ctlExpiryOnSunday.minusDays(2)));
    }

    @Test
    public void shouldReturnThursdayAsPreviousDayIfExpiryDateOnASundayAndOnPublicHolidayFriday() {
        final LocalDate ctlExpiryOnSunday = getCTLExpiryOnSunday();

        when(referenceDataService.getPublicHolidays(ENGLAND_AND_WALES_DIVISION, now(), ctlExpiryOnSunday)).thenReturn(getPublicHolidaysFriday(ctlExpiryOnSunday));

        final LocalDate adjustedCTLExpiryDate = publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryOnSunday);
        assertThat(adjustedCTLExpiryDate, is(ctlExpiryOnSunday.minusDays(3)));
    }

    @Test
    public void shouldReturnPreviousDayIfExpiryDateOnAPublicHolidayMondayNotAWeekend() {
        final LocalDate ctlExpiryOnSunday = getCTLExpiryOnSunday();
        final LocalDate ctlExpiryOnMonday = ctlExpiryOnSunday.plusDays(1);

        when(referenceDataService.getPublicHolidays(ENGLAND_AND_WALES_DIVISION, now(), ctlExpiryOnMonday)).thenReturn(getPublicHolidaysMonday(ctlExpiryOnSunday));

        final LocalDate adjustedCTLExpiryDate = publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryOnMonday);
        assertThat(adjustedCTLExpiryDate, is(ctlExpiryOnSunday.minusDays(2)));
    }


    @Test
    public void shouldReturnPreviousDayIfExpiryDateNotOnAPublicHolidayAndNotOnAWeekend() {
        final LocalDate ctlExpiryOnSunday = getCTLExpiryOnSunday();
        final LocalDate ctlExpiryOnThursday = ctlExpiryOnSunday.minusDays(4);

        when(referenceDataService.getPublicHolidays(ENGLAND_AND_WALES_DIVISION, now(), ctlExpiryOnThursday)).thenReturn(getPublicHolidaysMonday(ctlExpiryOnSunday));

        final LocalDate adjustedCTLExpiryDate = publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryOnThursday);
        assertThat(adjustedCTLExpiryDate, is(ctlExpiryOnThursday));
    }

    private List<PublicHoliday> getPublicHolidaysMonday(final LocalDate ctlExpiryDate) {
        final List<PublicHoliday> publicHolidays = new ArrayList();
        final LocalDate ctlExpiryOnMonday = ctlExpiryDate.plusDays(1);
        publicHolidays.add(publicHoliday(ctlExpiryOnMonday));
        return publicHolidays;
    }

    private List<PublicHoliday> getPublicHolidaysFriday(final LocalDate ctlExpiryDate) {
        final List<PublicHoliday> publicHolidays = new ArrayList();
        final LocalDate ctlExpiryOnFriday = ctlExpiryDate.minusDays(2);
        publicHolidays.add(publicHoliday(ctlExpiryOnFriday));
        return publicHolidays;
    }
    private LocalDate getCTLExpiryOnSunday() {
        final LocalDate ctlExpiryDate = now();
        final int daysToWeekendSunday = SUNDAY.getValue() - ctlExpiryDate.getDayOfWeek().getValue();
        return ctlExpiryDate.plusDays(daysToWeekendSunday);
    }

    private PublicHoliday publicHoliday(final LocalDate localDate) {
        return new PublicHoliday(UUID.randomUUID(), "division", "title", localDate);
    }

}