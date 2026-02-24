package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.core.courts.Hearing;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class PublicHearingResultedV2 {

    private List<UUID> shadowListedOffences;

    private Hearing hearing;

    private ZonedDateTime sharedTime;

    private Boolean isReshare;

    private LocalDate hearingDay;

    public static PublicHearingResultedV2 publicHearingResultedV2() {
        return new PublicHearingResultedV2();
    }

    public Hearing getHearing() {
        return hearing;
    }

    public PublicHearingResultedV2 setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public PublicHearingResultedV2 setSharedTime(ZonedDateTime sharedTime) {
        this.sharedTime = sharedTime;
        return this;
    }

    public List<UUID> getShadowListedOffences() {
        return shadowListedOffences;
    }

    public PublicHearingResultedV2 setShadowListedOffences(final List<UUID> shadowListedOffences) {
        this.shadowListedOffences = shadowListedOffences;
        return this;
    }

    public Boolean getIsReshare() {
        return isReshare;
    }

    public PublicHearingResultedV2 setIsReshare(final Boolean isReshare) {
        this.isReshare = isReshare;
        return this;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public PublicHearingResultedV2 setHearingDay(final LocalDate hearingDay) {
        this.hearingDay = hearingDay;
        return this;
    }
}
