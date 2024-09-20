package uk.gov.moj.cpp.hearing.query.view.service.ctl;


import static java.time.LocalDate.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class TimeSpentCalculatorTest {

    @InjectMocks
    private TimeSpentCalculator timeSpentCalculator;

    @Test
    public void shouldReturnZeroWhenPreviousDaysHeldInCustodyAndAndDateHeldInCustodySinceAreNull() {

        final Offence offence = new Offence();
        final int timeSpent = timeSpentCalculator.timeSpent(offence, now());

        assertThat(timeSpent, is(0));
    }

    @Test
    public void shouldCalculateTimeSpentWhenDateHeldInCustodySinceIsNotNullAndHasPreviousDaysHeld() {

        final Offence offence = new Offence();
        final int previousDaysHeldInCustody = 5;
        offence.setPreviousDaysHeldInCustody(previousDaysHeldInCustody);
        offence.setDateHeldInCustodySince(now().minusDays(6));
        final int timeSpent = timeSpentCalculator.timeSpent(offence, now());

        assertThat(timeSpent, is(11));
    }

    @Test
    public void shouldCalculateTimeSpentWhenDateHeldInCustodySinceIsNotNullAndPreviousDaysHeldInCustodyIsNull() {

        final Offence offence = new Offence();
        offence.setDateHeldInCustodySince(now().minusDays(6));
        final int timeSpent = timeSpentCalculator.timeSpent(offence, now());

        assertThat(timeSpent, is(6));
    }

    @Test
    public void shouldCalculateTimeSpentWhenDateHeldInCustodySinceIsNotNullAndPreviousDaysHeldInCustodyIsNullAndHearingDayIsNotToday() {
        final int daysSinceHearingWasResulted = 3;
        final LocalDate hearingDay = now().minusDays(daysSinceHearingWasResulted);
        final Offence offence = new Offence();
        offence.setDateHeldInCustodySince(now().minusDays(6));
        final int timeSpent = timeSpentCalculator.timeSpent(offence, hearingDay);

        assertThat(timeSpent, is(3));
    }

    @Test
    public void shouldReturnPreviousDaysHeldInCustodyWhenDateHeldInCustodySinceIsNullAndHasPreviousDaysHeld() {

        final Offence offence = new Offence();
        final int previousDaysHeldInCustody = 5;
        offence.setPreviousDaysHeldInCustody(previousDaysHeldInCustody);
        final int timeSpent = timeSpentCalculator.timeSpent(offence, now());

        assertThat(timeSpent, is(previousDaysHeldInCustody));
    }

}
