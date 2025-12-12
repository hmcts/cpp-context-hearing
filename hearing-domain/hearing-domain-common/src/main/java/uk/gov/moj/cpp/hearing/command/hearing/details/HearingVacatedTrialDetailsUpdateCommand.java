package uk.gov.moj.cpp.hearing.command.hearing.details;

import java.io.Serializable;
import java.util.UUID;

public class HearingVacatedTrialDetailsUpdateCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final Boolean isVacated;
    private final UUID vacatedTrialReasonId;
    private final Boolean allocated;


    public HearingVacatedTrialDetailsUpdateCommand(UUID hearingId, UUID vacatedTrialReasonId, Boolean isVacated, Boolean allocated) {
        this.hearingId = hearingId;
        this.vacatedTrialReasonId = vacatedTrialReasonId;
        this.isVacated = isVacated;
        this.allocated = allocated;

    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Boolean getIsVacated() {
        return isVacated;
    }

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

    public Boolean getAllocated() {
        return allocated;
    }

}
