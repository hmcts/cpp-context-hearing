package uk.gov.moj.cpp.hearing.eventlog;

import java.io.Serializable;
import java.util.UUID;

public class HearingDefendantDetail implements Serializable {

    private UUID defendantId;
    private String defendantFirstName;
    private String defendantLastName;
    private String defendantRemandStatus;
    private String interpreterLanguageNeeds;

    public String getDefendantFirstName() {
        return defendantFirstName;
    }

    public void setDefendantFirstName(final String defendantFirstName) {
        this.defendantFirstName = defendantFirstName;
    }

    public String getDefendantLastName() {
        return defendantLastName;
    }

    public void setDefendantLastName(final String defendantLastName) {
        this.defendantLastName = defendantLastName;
    }

    public String getDefendantRemandStatus() {
        return defendantRemandStatus;
    }

    public void setDefendantRemandStatus(final String defendantRemandStatus) {
        this.defendantRemandStatus = defendantRemandStatus;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
    }

    public String getInterpreterLanguageNeeds() {
        return interpreterLanguageNeeds;
    }

    public void setInterpreterLanguageNeeds(final String interpreterLanguageNeeds) {
        this.interpreterLanguageNeeds = interpreterLanguageNeeds;
    }
}
