package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.defence-counsel-removed")
public class DefenceCounselRemoved implements Serializable {
    private static final long serialVersionUID = -5995314363348475392L;
    private final UUID id;
    private final UUID hearingId;

    public DefenceCounselRemoved(final UUID id, final UUID hearingId) {
        this.id = id;
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getId() {
        return id;
    }

}
