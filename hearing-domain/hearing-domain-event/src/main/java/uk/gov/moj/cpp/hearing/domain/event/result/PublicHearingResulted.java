package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.Hearing;

import java.time.ZonedDateTime;

public class PublicHearingResulted {

    private Hearing hearing;

    private ZonedDateTime sharedTime;

    public static PublicHearingResulted publicHearingResulted() {
        return new PublicHearingResulted();
    }

    public Hearing getHearing() {
        return hearing;
    }

    public PublicHearingResulted setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public PublicHearingResulted setSharedTime(ZonedDateTime sharedTime) {
        this.sharedTime = sharedTime;
        return this;
    }
}
