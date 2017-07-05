package uk.gov.moj.cpp.hearing.persist.entity;

import java.util.UUID;

public class DefenceCounselToDefendant {

    private final UUID personId;
    private final UUID defendantId;

    public DefenceCounselToDefendant(final UUID personId, final UUID defendantId) {
        this.personId = personId;
        this.defendantId = defendantId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }
}
