package uk.gov.moj.cpp.hearing.common;

import java.util.List;
import java.util.UUID;

public class YouthCourtDefendantsInHearing {

    private final UUID hearingId;

    private final  List<UUID> youthCourtDefendantIds;


    public YouthCourtDefendantsInHearing(final UUID hearingId, final List<UUID> youthCourtDefendantIds) {
        this.hearingId = hearingId;
        this.youthCourtDefendantIds = youthCourtDefendantIds;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<UUID> getYouthCourtDefendantIds() {
        return youthCourtDefendantIds;
    }
}
