package uk.gov.moj.cpp.hearing.event;


import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicHearingDraftResultDeleted {
    private UUID hearingId;
    private LocalDate haringDay;


    public static PublicHearingDraftResultDeleted publicHearingDraftResultSaved() {
        return new PublicHearingDraftResultDeleted();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public PublicHearingDraftResultDeleted setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public LocalDate getHaringDay() {
        return haringDay;

    }

    public PublicHearingDraftResultDeleted setHaringDay(final LocalDate haringDay) {
        this.haringDay = haringDay;
        return this;
    }
}