package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.EITHER_WAY;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.INDICTABLE;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.SUMMARY_ONLY;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CTLExpiryDateCalculatorImpl implements CTLExpiryDateCalculator {

    private static final Map<String, JurisdictionType> allocationDecisionsMap = new HashMap();

    static {
        allocationDecisionsMap.put("Indictable only (previous convictions / relevant firearms offence)", CROWN);
        allocationDecisionsMap.put("Court directs trial by jury", CROWN);
        allocationDecisionsMap.put("Defendant elects trial by jury", CROWN);
        allocationDecisionsMap.put("Youth - offence triable on indictment only (homicide / relevant firearms / dangerousness)", CROWN);
        allocationDecisionsMap.put("Youth - Court directs trial by jury (grave crime)", CROWN);
        allocationDecisionsMap.put("Indictable-only offence", CROWN);
        allocationDecisionsMap.put("Offence triable only by court martial", CROWN);
        allocationDecisionsMap.put("Defendant chooses trial by court martial 1", CROWN);
        allocationDecisionsMap.put("Commanding Officer orders trial by court martial", CROWN);
        allocationDecisionsMap.put("Defendant consents to summary trial", MAGISTRATES);
        allocationDecisionsMap.put("Low value offence triable summarily only", MAGISTRATES);
        allocationDecisionsMap.put("No mode of Trial - Either way offence", MAGISTRATES);
        allocationDecisionsMap.put("Summary-only offence", MAGISTRATES);
    }

    @Override
    public Optional<LocalDate> calculateCTLExpiryDate(final Offence offence,
                                                      final CTLRemandStatus remandStatus,
                                                      final LocalDate hearingDay) {

        if (nonNull(offence.getDefendant()) && TRUE.equals(offence.getDefendant().getIsYouth()) &&
                (validModeOfTrial(offence, EITHER_WAY) || validModeOfTrial(offence, INDICTABLE))) {
            return eitherWayCTLExpiryDateCalculator(offence, hearingDay);
        }

        if (validModeOfTrial(offence, EITHER_WAY)) {
            return eitherWayCTLExpiryDateCalculator(offence, hearingDay);
        }

        if (validModeOfTrial(offence, SUMMARY_ONLY)) {
            return summaryOnlyCTLExpiryDateCalculator(hearingDay);
        }

        if (validModeOfTrial(offence, INDICTABLE)) {
            return indictableOnlyCTLExpiryDateCalculator(hearingDay);
        }

        return Optional.empty();
    }

    private boolean validModeOfTrial(final Offence offence, final ModeOfTrial modeOfTrial) {
        return modeOfTrial.type().equals(offence.getModeOfTrial());
    }

    private Optional<LocalDate> indictableOnlyCTLExpiryDateCalculator(final LocalDate hearingDay) {
        return Optional.of(hearingDay.plusDays(182));
    }

    private Optional<LocalDate> summaryOnlyCTLExpiryDateCalculator(final LocalDate hearingDay) {
        return Optional.of(hearingDay.plusDays(56));
    }

    private Optional<LocalDate> eitherWayCTLExpiryDateCalculator(final Offence offence, final LocalDate hearingDay) {
        final AllocationDecision allocationDecision = offence.getAllocationDecision();

        if (isNull(offence.getAllocationDecision()) || isEmpty(allocationDecision.getMotReasonDescription())) {
            return Optional.of(hearingDay.plusDays(56));
        }

        final String motReasonDescription = allocationDecision.getMotReasonDescription().trim();

        final JurisdictionType jurisdictionType = allocationDecisionsMap.get(motReasonDescription);
        if (CROWN.equals(jurisdictionType)) {
            return Optional.of(hearingDay.plusDays(182));
        }

        if (MAGISTRATES.equals(jurisdictionType)) {
            return Optional.of(hearingDay.plusDays(56));
        }
        return Optional.empty();
    }
}

