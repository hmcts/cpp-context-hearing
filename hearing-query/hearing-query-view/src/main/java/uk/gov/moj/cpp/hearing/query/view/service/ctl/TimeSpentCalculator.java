package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Objects.nonNull;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;

import java.time.LocalDate;

/**
 * This service is to calculate how many days spent in custody per offence
 */
public class TimeSpentCalculator {


    /**
     * Time spent in custody is previousDaysHeldInCustody plus days between
     * getDateHeldInCustodySince and today's date
     *
     * @param offence
     * @param hearingDay
     * @return
     */
    public Integer timeSpent(final Offence offence, final LocalDate hearingDay) {
        int previousDaysHeldInCustody = nonNull(offence.getPreviousDaysHeldInCustody()) ? offence.getPreviousDaysHeldInCustody() : 0;
        if (nonNull(offence.getDateHeldInCustodySince())) {
            return previousDaysHeldInCustody + (int) DAYS.between(offence.getDateHeldInCustodySince(), hearingDay);
        }
        return previousDaysHeldInCustody;
    }
}
