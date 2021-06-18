package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.lang.Boolean.*;
import static java.util.Optional.of;

import static java.util.Objects.nonNull;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class CTLExpiryDateCalculatorService {

    private static final String VOLUNTARY_BILL = "Voluntary bill";

    @Inject
    private CTLExpiryDateValidityChecker ctlExpiryDateValidityChecker;

    @Inject
    private CTLExpiryDateCalculatorImpl ctlExpiryDateCalculator;

    @Inject
    private TimeSpentCalculator timeSpentCalculator;

    @Inject
    private PublicHolidaysWeekendsService publicHolidaysWeekendsService;

    /**
     * This method is to do auto CTL expiry date calculation based on offence and remand status
     * Checks first if calculation has to be performed based on offence and remand status
     * Uses the TimeSpentCalculator to find out the previous time in custody
     * Uses the PublicHolidaysWeekendsService to find out if the date falls on weekend or bank holiday
     * If CTL is extended, it will return existed CTL expiry time
     */
    public Optional<LocalDate> calculateCTLExpiryDate(final Offence offence, final LocalDate hearingDay) {

        if(TRUE.equals(offence.isCtlExtended())){
            return Optional.of(offence.getCtlTimeLimit());
        }

        Optional<LocalDate> ctlExpiryDate = Optional.empty();
        final CTLRemandStatus remandStatus = CTLRemandStatus.getCTLRemandStatusFrom(offence.getBailStatusCode());

        if (ctlExpiryDateValidityChecker.valid(offence, remandStatus)) {
            ctlExpiryDate = ctlExpiryDateCalculator.calculateCTLExpiryDate(offence, remandStatus, hearingDay);
            if (ctlExpiryDate.isPresent()) {
                final Integer timeSpent = timeSpentCalculator.timeSpent(offence, hearingDay);
                final LocalDate calenderBasedCTLExpiryDate = publicHolidaysWeekendsService.getCalenderBasedCTLExpiryDate(ctlExpiryDate.get().minusDays(timeSpent));
                ctlExpiryDate = of(calenderBasedCTLExpiryDate);
            }
        }

        return ctlExpiryDate;
    }

    /**
     * This method is to avoid auto CTL calculation.
     * @param hearing
     * @param offenceId
     * @return
     */
    public boolean avoidCalculation(final Hearing hearing, final UUID offenceId) {
        return avoidDueToVoluntaryBail(hearing, offenceId) || avoidDueToCTLClockStopped(hearing, offenceId);
    }

    /**
     * If trial receipt type in the case which is associated with offence is Voluntary Bail, it
     * returns true else returns false
     */
    private boolean avoidDueToVoluntaryBail(final Hearing hearing, final UUID offenceId) {
        return hearing.getProsecutionCases().stream()
                .filter(pc -> pc.getDefendants().stream()
                        .flatMap(defendant -> defendant.getOffences().stream())
                        .anyMatch(offence -> offence.getId().getId().equals(offenceId)))
                .anyMatch(pc -> VOLUNTARY_BILL.equalsIgnoreCase(pc.getTrialReceiptType()));
    }

    /**
     * If custody time limit clock is stopped, it returns true else returns false
     */
    private boolean avoidDueToCTLClockStopped(final Hearing hearing, final UUID offenceId) {
        return hearing.getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .anyMatch(offence -> offence.getId().getId().equals(offenceId) && nonNull(offence.isCtlClockStopped())
                        && offence.isCtlClockStopped());
    }

}
