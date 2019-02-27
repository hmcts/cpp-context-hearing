package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.SharedHearing;
import uk.gov.justice.core.courts.SharedVariant;

import java.time.ZonedDateTime;
import java.util.List;

public class PublicHearingResulted {
    private SharedHearing hearing;
    private List<SharedVariant> variants;
    private ZonedDateTime sharedTime;

    public static PublicHearingResulted publicHearingResulted() {
        return new PublicHearingResulted();
    }

    public SharedHearing getHearing() {
        return hearing;
    }

    public PublicHearingResulted setHearing(SharedHearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public List<SharedVariant> getVariants() {
        return variants;
    }

    public PublicHearingResulted setVariants(List<SharedVariant> variants) {
        this.variants = variants;
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
