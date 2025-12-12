package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.DefenceCounsel;


import java.io.Serializable;
import java.util.UUID;

@Event("hearing.defence-counsel-added")
public class DefenceCounselAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private final DefenceCounsel defenceCounsel;
    private final UUID hearingId;

    public DefenceCounselAdded(final DefenceCounsel defenceCounsel, final UUID hearingId) {
        this.defenceCounsel = defenceCounsel;
        this.hearingId = hearingId;
    }


    public UUID getHearingId() {
        return hearingId;
    }

    public DefenceCounsel getDefenceCounsel() {
        return defenceCounsel;
    }
}
