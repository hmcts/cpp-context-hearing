package uk.gov.moj.cpp.hearing.query.view.service.ctl;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.CTLRemandStatus;

import java.time.LocalDate;
import java.util.Optional;

public interface CTLExpiryDateCalculator {
    Optional<LocalDate> calculateCTLExpiryDate(final Offence offence, final CTLRemandStatus remandStatus, final LocalDate hearingDay);
}