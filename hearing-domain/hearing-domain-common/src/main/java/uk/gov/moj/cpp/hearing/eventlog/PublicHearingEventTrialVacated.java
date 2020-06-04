package uk.gov.moj.cpp.hearing.eventlog;

import java.util.UUID;

public class PublicHearingEventTrialVacated {

    private UUID hearingId;

    private UUID vacatedTrialReasonId;


    public static PublicHearingEventTrialVacated publicHearingEventTrialVacated() {
        return new PublicHearingEventTrialVacated();
    }

    public PublicHearingEventTrialVacated setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public PublicHearingEventTrialVacated setVacatedTrialReasonId(final UUID vacatedTrialReasonId) {
        this.vacatedTrialReasonId = vacatedTrialReasonId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

}