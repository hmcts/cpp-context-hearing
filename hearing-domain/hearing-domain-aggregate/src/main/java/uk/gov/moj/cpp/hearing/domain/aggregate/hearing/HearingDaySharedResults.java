package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;

import java.time.LocalDate;
import java.util.List;

public class HearingDaySharedResults {

    public Hearing setHasSharedResults(final Hearing hearing, final LocalDate hearingDay) {
        if (nonNull(hearing)) {
            final List<HearingDay> hearingDays = hearing.getHearingDays();

            if (nonNull(hearing.getHasSharedResults()) && hearing.getHasSharedResults()) {
                hearingDays.stream().filter(hd -> nonNull(hearingDay)
                        && hearingDay.equals(hd.getSittingDay().toLocalDate())).forEach(hd -> hd.setHasSharedResults(true));
            }
            hearingDays.stream().filter(hd -> nonNull(hearingDay) && !TRUE.equals(hd.getHasSharedResults()))
                    .forEach(hd -> hd.setHasSharedResults(false));
        }
        return hearing;
    }
}
