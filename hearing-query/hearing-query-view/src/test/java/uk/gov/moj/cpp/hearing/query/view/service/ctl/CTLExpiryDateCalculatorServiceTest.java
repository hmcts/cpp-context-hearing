package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.LocalDate.now;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus.REMANDED_IN_CUSTODY_PENDING_CONDITIONS;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.PublicHoliday;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("squid:S2187")
@RunWith(MockitoJUnitRunner.class)
public class CTLExpiryDateCalculatorServiceTest extends TestCase {

    @Mock
    private ReferenceDataService referenceData;

    @Mock
    private Offence offence;

    @Mock
    private TimeSpentCalculator timeSpentCalculator;

    @Spy
    private CTLExpiryDateValidityChecker ctlExpiryDateValidityChecker;

    @Mock
    private PublicHolidaysWeekendsService publicHolidaysWeekendsService;

    @Mock
    private CTLExpiryDateCalculatorImpl ctlExpiryDateCalculator;

    @InjectMocks
    private CTLExpiryDateCalculatorService ctlExpiryDateCalculatorService;

    @Test
    public void shouldReturnCTLExpiryDate() {
        final LocalDate ctlExpiryDate = LocalDate.now().plusDays(182);

        when(offence.getModeOfTrial()).thenReturn(ModeOfTrial.INDICTABLE.type());
        when(ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now())).thenReturn(Optional.of(ctlExpiryDate));
        when(publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryDate)).thenReturn(ctlExpiryDate);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculatorService
                .calculateCTLExpiryDate(offence, LocalDate.now(), "P");

        assertThat(expiryDate.get(), is(ctlExpiryDate));
    }

    @Test
    public void shouldReturnCTLExpiryDateAsFridayIfItCtlExpiryAutoCalculatesToSundayAndNotABankHoliday() {
        final LocalDate ctlExpiryDate = LocalDate.now().plusDays(182);
        final int daysToWeekendSunday = 7 - ctlExpiryDate.getDayOfWeek().getValue();
        final LocalDate ctlExpiryOnSunday = ctlExpiryDate.plusDays(daysToWeekendSunday);
        final List<PublicHoliday> publicHolidays = new ArrayList<>();
        final String division = "division";

        when(referenceData.getPublicHolidays(division, LocalDate.now(), ctlExpiryOnSunday)).thenReturn(publicHolidays);
        when(offence.getModeOfTrial()).thenReturn(ModeOfTrial.INDICTABLE.type());
        when(offence.getBailStatusCode()).thenReturn(REMANDED_IN_CUSTODY_PENDING_CONDITIONS.getCode());
        when(ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now())).thenReturn(Optional.of(ctlExpiryOnSunday));
        final LocalDate adjustedDaysForWeekend = ctlExpiryOnSunday.minusDays(2);
        when(publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryOnSunday)).thenReturn(adjustedDaysForWeekend);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculatorService.calculateCTLExpiryDate(offence, LocalDate.now(), "P");

        assertThat(expiryDate.get(), is(adjustedDaysForWeekend));
    }

    @Test
    public void shouldReturnCTLExpiryDateAsFridayIfItCtlExpiryAutoCalculatesToPublicHoliday() {
        final String division = "division";
        final LocalDate ctlExpiryDate = now();
        final int daysToWeekendSunday = 7 - ctlExpiryDate.getDayOfWeek().getValue();
        final LocalDate ctlExpiryOnSunday = ctlExpiryDate.plusDays(daysToWeekendSunday);
        final LocalDate ctlExpiryOnMonday = ctlExpiryOnSunday.plusDays(1);
        final List<PublicHoliday> publicHolidays = new ArrayList<>();
        publicHolidays.add(new PublicHoliday(UUID.randomUUID(), "", "", ctlExpiryOnMonday));

        when(referenceData.getPublicHolidays(division, LocalDate.now(), ctlExpiryOnMonday)).thenReturn(publicHolidays);
        when(offence.getModeOfTrial()).thenReturn(ModeOfTrial.INDICTABLE.type());
        when(ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now())).thenReturn(Optional.of(ctlExpiryOnMonday));
        final LocalDate adjustedDaysForPublicHolidays = ctlExpiryOnMonday.minusDays(4);
        when(publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryOnMonday)).thenReturn(adjustedDaysForPublicHolidays);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculatorService.calculateCTLExpiryDate(offence, LocalDate.now(), "P");

        assertThat(expiryDate.get(), is(adjustedDaysForPublicHolidays));
    }


    @Test
    public void shouldReturnNullForCTLExpiryDateWhenRemandStatusIsNull() {
        when(ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, null, now())).thenReturn(Optional.empty());

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculatorService
                .calculateCTLExpiryDate(offence, LocalDate.now(), "C");

        assertThat(expiryDate, is(empty()));
    }

    @Test
    public void shouldReturnNullForCTLExpiryDateWhenOffenceIsNull() {
        when(ctlExpiryDateCalculator.calculateCTLExpiryDate(null, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now())).thenReturn(Optional.empty());

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculatorService
                .calculateCTLExpiryDate(offence, LocalDate.now(), "C");

        assertThat(expiryDate, is(empty()));
    }

    @Test
    public void shouldAvoidCTLCalculationWhenTrialReceiptTypeIsVoluntaryBill() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));
        prosecutionCase.setTrialReceiptType("Voluntary bill");

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(singleton(prosecutionCase));

        final boolean isAvoid = ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId);

        assertThat(isAvoid, is(true));

    }

    @Test
    public void shouldNotAvoidCTLCalculationWhenTrialReceiptTypeIsNotVoluntaryBill() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));
        prosecutionCase.setTrialReceiptType("Transfer");

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(singleton(prosecutionCase));

        final boolean isAvoid = ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId);

        assertThat(isAvoid, is(false));

    }

    @Test
    public void shouldNotAvoidCTLCalculationWhenTrialReceiptTypeIsNull() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(singleton(prosecutionCase));

        final boolean isAvoid = ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId);

        assertThat(isAvoid, is(false));
    }

    @Test
    public void shouldNotAvoidCTLCalculationWhenOtherCasesTrialReceiptTypeIsVoluntaryBill() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Offence offence1 = new Offence();
        offence1.setId(new HearingSnapshotKey(randomUUID(), hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final Defendant defendant1 = new Defendant();
        defendant1.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        defendant1.setOffences(singleton(offence1));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final ProsecutionCase prosecutionCase1 = new ProsecutionCase();
        prosecutionCase1.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        prosecutionCase1.setTrialReceiptType("Voluntary bill");
        prosecutionCase1.setDefendants(singleton(defendant1));

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(new HashSet<>(Arrays.asList(prosecutionCase, prosecutionCase1)));

        final boolean isAvoid = ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId);

        assertThat(isAvoid, is(false));
    }

    @Test
    public void shouldNotAvoidCTLCalculationWhenCTLClockIsNotStopped() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Offence offence1 = new Offence();
        offence1.setId(new HearingSnapshotKey(randomUUID(), hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final Defendant defendant1 = new Defendant();
        defendant1.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        defendant1.setOffences(singleton(offence1));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final ProsecutionCase prosecutionCase1 = new ProsecutionCase();
        prosecutionCase1.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        prosecutionCase1.setDefendants(singleton(defendant1));

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(new HashSet<>(Arrays.asList(prosecutionCase, prosecutionCase1)));

        final boolean isAvoid = ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId);

        assertThat(isAvoid, is(false));

    }

    @Test
    public void shouldAvoidCTLCalculationWhenCTLClockIsStopped() {
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Offence offence1 = new Offence();
        offence1.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        offence1.setCtlClockStopped(true);

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final Defendant defendant1 = new Defendant();
        defendant1.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        defendant1.setOffences(singleton(offence1));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final ProsecutionCase prosecutionCase1 = new ProsecutionCase();
        prosecutionCase1.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        prosecutionCase1.setDefendants(singleton(defendant1));

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(new HashSet<>(Arrays.asList(prosecutionCase, prosecutionCase1)));

        final boolean isAvoid = ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId);

        assertThat(isAvoid, is(false));
    }

    @Test
    public void shouldReturnCTLExpiryDateWhenTimeSpentIsNotZero() {
        final LocalDate ctlExpiryDate = LocalDate.now().plusDays(182);
        final int timeSpent = 2;

        when(offence.getModeOfTrial()).thenReturn(ModeOfTrial.INDICTABLE.type());
        when(ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now())).thenReturn(Optional.of(ctlExpiryDate));
        when(publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryDate.minusDays(timeSpent))).thenReturn(ctlExpiryDate.minusDays(timeSpent));

        when(timeSpentCalculator.timeSpent(offence, now())).thenReturn(timeSpent);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculatorService
                .calculateCTLExpiryDate(offence, LocalDate.now(), "P");

        assertThat(expiryDate.get(), is(ctlExpiryDate.minusDays(timeSpent)));
    }

    @Test
    public void shouldReturnExistedCTLExpiryDateWhenCTLIsExtended() {
        final LocalDate ctlExpiryDate = LocalDate.now().plusDays(100);

        when(offence.isCtlExtended()).thenReturn(Boolean.TRUE);
        when(offence.getCtlTimeLimit()).thenReturn(ctlExpiryDate);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculatorService
                .calculateCTLExpiryDate(offence, LocalDate.now(), null);

        assertThat(expiryDate.get(), is(ctlExpiryDate));
    }

}