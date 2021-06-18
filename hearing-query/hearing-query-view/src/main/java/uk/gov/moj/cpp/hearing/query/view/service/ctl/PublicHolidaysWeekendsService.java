package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;

import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.PublicHoliday;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

public class PublicHolidaysWeekendsService {
    private static final String ENGLAND_AND_WALES_DIVISION = "england-and-wales";
    @Inject
    private ReferenceDataService referenceDataService;

    /**
     * Returns previous working day if the provided day is on a public holiday or bank holiday
     */
    public LocalDate getCalenderBasedCTLExpiryDate(final LocalDate ctlExpirydate) {
        final List<PublicHoliday> publicHolidays = referenceDataService.getPublicHolidays(ENGLAND_AND_WALES_DIVISION, now(), ctlExpirydate);
        return getAdjustedCTLExpiryDate(ctlExpirydate, publicHolidays.stream().map(PublicHoliday::getDate).collect(toList()));
    }

    private LocalDate getAdjustedCTLExpiryDate(final LocalDate ctlExpiryDate, final List<LocalDate> publicHolidays) {
        LocalDate adjustedCTLExpiryDate = ctlExpiryDate;
        while (isCTLExpiryDateOnAWeekend(adjustedCTLExpiryDate) || publicHolidays.contains(adjustedCTLExpiryDate)) {
            adjustedCTLExpiryDate = adjustedCTLExpiryDate.minusDays(1);
        }
        return adjustedCTLExpiryDate;
    }

    private boolean isCTLExpiryDateOnAWeekend(final LocalDate ctlExpiryDate) {
        return ctlExpiryDate.getDayOfWeek() == SUNDAY || ctlExpiryDate.getDayOfWeek() == SATURDAY;
    }

}
