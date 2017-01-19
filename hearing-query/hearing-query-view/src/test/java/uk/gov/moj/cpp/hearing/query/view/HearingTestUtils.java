package uk.gov.moj.cpp.hearing.query.view;

import static java.util.UUID.randomUUID;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

import java.time.LocalDate;
import java.util.Optional;

public class HearingTestUtils {

    public static final LocalDate startDate = LocalDate.now();

    public static Optional<Hearing> getHearing() {
        final Hearing hearingA = new Hearing(randomUUID(), startDate, null, 1,
                null, null, null, null, null);
        return Optional.of(hearingA);
    }
}
