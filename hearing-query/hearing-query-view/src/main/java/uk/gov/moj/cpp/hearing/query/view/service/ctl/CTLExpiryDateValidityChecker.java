package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.util.Objects.isNull;
import static uk.gov.moj.cpp.hearing.query.view.service.ctl.model.ModeOfTrial.getIfPresent;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus;

public class CTLExpiryDateValidityChecker {
    private static final String GUILTY = "GUILTY";

    public boolean valid(final Offence offence, final CTLRemandStatus ctlRemandStatus) {
        if (isNull(offence) || isNull(offence.getModeOfTrial()) ||
                isNull(ctlRemandStatus) || isNull(getIfPresent(offence.getModeOfTrial()))) {
            return false;
        }
        return isNotGuiltyAndNoConvictionDate(offence) && hasHadNoFinalResultAndNoVerdict(offence);
    }

    private boolean hasHadNoFinalResultAndNoVerdict(final Offence offence) {
        return isNull(offence.getVerdict()) && !offence.isProceedingsConcluded();
    }

    private boolean isNotGuiltyAndNoConvictionDate(final Offence offence) {
        return isNoGuilyPlea(offence) && isNull(offence.getConvictionDate());
    }

    private boolean isNoGuilyPlea(final Offence offence) {
        if (isNull(offence.getPlea())) {
            return true;
        }
        return !GUILTY.equals(offence.getPlea().getPleaValue());
    }
}
