package uk.gov.moj.cpp.hearing.query.view;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class HearingTestUtils {

    public static final LocalDate startDate = LocalDate.now();

    public static Optional<Hearing> getHearing() {

        final Hearing hearingA = new Hearing();
        hearingA.setHearingId(UUID.randomUUID());
        hearingA.setStartdate(startDate);
        hearingA.setDuration(1);
        return Optional.of(hearingA);
    }
}
