package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.UUID;

public class DefendantWelshInfo implements Serializable {

    private UUID defendantId;

    private boolean welshTranslation;

    @JsonCreator
    public DefendantWelshInfo(UUID defendantId, boolean welshTranslation) {
        this.defendantId = defendantId;
        this.welshTranslation = welshTranslation;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public boolean isWelshTranslation() {
        return welshTranslation;
    }
}
