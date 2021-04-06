package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.event.defendants-in-youthcourt-updated")
public class DefendantsInYouthCourtUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<UUID> youthCourtDefendantIds;
    private final UUID hearingId;

    @SuppressWarnings("squid:S2384")
    public DefendantsInYouthCourtUpdated(final List<UUID> youthCourtDefendantIds, final UUID hearingId) {
        this.youthCourtDefendantIds = youthCourtDefendantIds;
        this.hearingId = hearingId;
    }

    @SuppressWarnings("squid:S2384")
    public List<UUID> getYouthCourtDefendantIds() {
        return youthCourtDefendantIds;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
