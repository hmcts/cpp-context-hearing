package uk.gov.moj.cpp.hearing.persist.entity;

import java.io.Serializable;
import java.util.UUID;

public class DefenceCounselDefendantCompositeKey implements Serializable {

    private UUID defenceCounselAttendeeId;
    private UUID defendantId;

    public DefenceCounselDefendantCompositeKey(final UUID defenceCounselAttendeeId,
                                               final UUID defendantId) {
        this.defenceCounselAttendeeId = defenceCounselAttendeeId;
        this.defendantId = defendantId;
    }

    public DefenceCounselDefendantCompositeKey(){
    }

    public UUID getDefenceCounselAttendeeId() {
        return defenceCounselAttendeeId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

}
