package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.LocalDate.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus.REMANDED_IN_CUSTODY_PENDING_CONDITIONS;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.EITHER_WAY;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.INDICTABLE;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.SUMMARY_ONLY;

import uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial;

import java.time.LocalDate;
import java.util.Optional;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("squid:S2187")
@RunWith(MockitoJUnitRunner.class)
public class CTLExpiryDateCalculatorTest extends TestCase {
    private static final String DEFENDANT_CONSENTS_TO_SUMMARY_TRIAL = "Defendant consents to summary trial";
    private static final String COURT_DIRECTS_TRIAL_BY_JURY = "Court directs trial by jury";

    @Mock
    private AllocationDecision allocationDecision;

    @Mock
    private Defendant defendant;

    @Mock
    private Offence offence;

    @InjectMocks
    private CTLExpiryDateCalculatorImpl ctlExpiryDateCalculator;

    @Before
    public void setUp() {
        when(offence.getDefendant()).thenReturn(defendant);
    }

    @Test
    public void shouldReturnCTLExpiryDateForIndictmentOffenceType() {
        mockModeOfTrialType(INDICTABLE);
        mockYouthDefendant(null);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(182)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForIndictmentOffenceTypeAndHearingDayNotToday() {
        final int daysSinceHearingWasResulted = 3;
        final LocalDate hearingDay = now().minusDays(daysSinceHearingWasResulted);
        mockModeOfTrialType(INDICTABLE);
        mockYouthDefendant(null);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, hearingDay);

        assertThat(expiryDate.get(), is(hearingDay.plusDays(182)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForSummaryOnlyOffenceType() {
        mockModeOfTrialType(SUMMARY_ONLY);
        mockYouthDefendant(false);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForSummaryOnlyOffenceTypeAndHearingDayNotToday() {
        final int daysSinceHearingWasResulted = 3;
        final LocalDate hearingDay = now().minusDays(daysSinceHearingWasResulted);
        mockModeOfTrialType(SUMMARY_ONLY);
        mockYouthDefendant(false);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, hearingDay);

        assertThat(expiryDate.get(), is(hearingDay.plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForEitherWayOffenceTypeWithNoAllocation() {
        mockModeOfTrialType(EITHER_WAY);
        mockYouthDefendant(false);
        when(offence.getAllocationDecision()).thenReturn(null);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForEitherWayOffenceTypeWithNoAllocationAndHearingDayNotToday() {
        final int daysSinceHearingWasResulted = 3;
        final LocalDate hearingDay = now().minusDays(daysSinceHearingWasResulted);
        mockModeOfTrialType(EITHER_WAY);
        mockYouthDefendant(false);
        when(offence.getAllocationDecision()).thenReturn(null);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, hearingDay);

        assertThat(expiryDate.get(), is(hearingDay.plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForEitherWayOffenceTypeWithCrownAndAllocation() {
        mockModeOfTrialType(EITHER_WAY);
        mockYouthDefendant(false);
        mockAllocationDecision(COURT_DIRECTS_TRIAL_BY_JURY);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(182)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForEitherWayOffenceTypeWithMagsAllocation() {
        mockModeOfTrialType(EITHER_WAY);
        mockYouthDefendant(false);
        mockAllocationDecision(DEFENDANT_CONSENTS_TO_SUMMARY_TRIAL);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForSummaryOnlyOffenceTypeWithAllocationForYouth() {
        mockModeOfTrialType(SUMMARY_ONLY);
        mockYouthDefendant(true);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForEitherWayOffenceTypeWithNullAllocationForYouth() {
        mockModeOfTrialType(SUMMARY_ONLY);
        mockYouthDefendant(true);
        when(offence.getAllocationDecision()).thenReturn(null);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForIndictableOffenceTypeWithNullAllocationForYouth() {
        mockModeOfTrialType(INDICTABLE);
        mockYouthDefendant(true);
        when(offence.getAllocationDecision()).thenReturn(null);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForEitherWayOffenceTypeWithMagsAllocationForYouth() {
        mockModeOfTrialType(EITHER_WAY);
        mockYouthDefendant(true);
        mockAllocationDecision(DEFENDANT_CONSENTS_TO_SUMMARY_TRIAL);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForIndictableOffenceTypeWithMagsAllocationForYouth() {
        mockModeOfTrialType(INDICTABLE);
        mockYouthDefendant(true);
        mockAllocationDecision(DEFENDANT_CONSENTS_TO_SUMMARY_TRIAL);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(56)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForEitherWayOffenceTypeWithCrownAllocationForYouth() {
        mockModeOfTrialType(EITHER_WAY);
        mockYouthDefendant(true);
        mockAllocationDecision(COURT_DIRECTS_TRIAL_BY_JURY);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator
                .calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(182)));
    }

    @Test
    public void shouldReturnCTLExpiryDateForIndictableOffenceTypeWithCrownAllocationForYouth() {
        mockModeOfTrialType(INDICTABLE);
        mockYouthDefendant(true);
        mockAllocationDecision(COURT_DIRECTS_TRIAL_BY_JURY);

        final Optional<LocalDate> expiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, REMANDED_IN_CUSTODY_PENDING_CONDITIONS, now());

        assertThat(expiryDate.get(), is(now().plusDays(182)));
    }

    private void mockYouthDefendant(final Boolean isYouth) {
        when(defendant.getIsYouth()).thenReturn(isYouth);
    }

    private void mockModeOfTrialType(final ModeOfTrial modeOfTrial) {
        when(offence.getModeOfTrial()).thenReturn(modeOfTrial.type());
    }

    private void mockAllocationDecision(final String motReason) {
        when(offence.getAllocationDecision()).thenReturn(allocationDecision);
        when(allocationDecision.getMotReasonDescription()).thenReturn(motReason);
    }
}