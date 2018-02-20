package uk.gov.moj.cpp.hearing.domain;

import java.util.List;
import java.util.UUID;

public class ResultLine {

    private final UUID id;
    private final UUID lastSharedResultId;
    private final UUID caseId;
    private final UUID personId;
    private final UUID offenceId;
    private final String level;
    private final String resultLabel;
    private final List<ResultPrompt> prompts;
    private final String court;
    private final String courtRoom;
    private final UUID clerkOfTheCourtId;
    private final String clerkOfTheCourtFirstName;
    private final String clerkOfTheCourtLastName;

    public ResultLine(final UUID id, final UUID lastSharedResultId, final UUID caseId, final UUID personId, final UUID offenceId,
                      final String level, final String resultLabel, final List<ResultPrompt> prompts, String court, String courtRoom, UUID clerkOfTheCourtId, String clerkOfTheCourtFirstName, String clerkOfTheCourtLastName) {
        this.id = id;
        this.lastSharedResultId = lastSharedResultId;
        this.caseId = caseId;
        this.personId = personId;
        this.offenceId = offenceId;
        this.level = level;
        this.resultLabel = resultLabel;
        this.prompts = prompts;
        this.court = court;
        this.courtRoom = courtRoom;
        this.clerkOfTheCourtId = clerkOfTheCourtId;
        this.clerkOfTheCourtFirstName = clerkOfTheCourtFirstName;
        this.clerkOfTheCourtLastName = clerkOfTheCourtLastName;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLastSharedResultId() {
        return lastSharedResultId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public String getLevel() {
        return level;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public List<ResultPrompt> getPrompts() {
        return prompts;
    }

    public String getCourt() {
        return court;
    }

    public String getCourtRoom() {
        return courtRoom;
    }

    public UUID getClerkOfTheCourtId() {
        return clerkOfTheCourtId;
    }

    public String getClerkOfTheCourtFirstName() {
        return clerkOfTheCourtFirstName;
    }

    public String getClerkOfTheCourtLastName() {
        return clerkOfTheCourtLastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof ResultLine)) { return false; }

        ResultLine that = (ResultLine) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (lastSharedResultId != null ? !lastSharedResultId.equals(that.lastSharedResultId) : that.lastSharedResultId != null) {
            return false;
        }
        if (caseId != null ? !caseId.equals(that.caseId) : that.caseId != null) { return false; }
        if (personId != null ? !personId.equals(that.personId) : that.personId != null) {
            return false;
        }
        if (offenceId != null ? !offenceId.equals(that.offenceId) : that.offenceId != null) {
            return false;
        }
        if (level != null ? !level.equals(that.level) : that.level != null) { return false; }
        if (resultLabel != null ? !resultLabel.equals(that.resultLabel) : that.resultLabel != null) {
            return false;
        }
        if (prompts != null ? !prompts.equals(that.prompts) : that.prompts != null) {
            return false;
        }
        if (court != null ? !court.equals(that.court) : that.court != null) { return false; }
        if (courtRoom != null ? !courtRoom.equals(that.courtRoom) : that.courtRoom != null) {
            return false;
        }
        if (clerkOfTheCourtId != null ? !clerkOfTheCourtId.equals(that.clerkOfTheCourtId) : that.clerkOfTheCourtId != null) {
            return false;
        }
        if (clerkOfTheCourtFirstName != null ? !clerkOfTheCourtFirstName.equals(that.clerkOfTheCourtFirstName) : that.clerkOfTheCourtFirstName != null) {
            return false;
        }
        return clerkOfTheCourtLastName != null ? clerkOfTheCourtLastName.equals(that.clerkOfTheCourtLastName) : that.clerkOfTheCourtLastName == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (lastSharedResultId != null ? lastSharedResultId.hashCode() : 0);
        result = 31 * result + (caseId != null ? caseId.hashCode() : 0);
        result = 31 * result + (personId != null ? personId.hashCode() : 0);
        result = 31 * result + (offenceId != null ? offenceId.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (resultLabel != null ? resultLabel.hashCode() : 0);
        result = 31 * result + (prompts != null ? prompts.hashCode() : 0);
        result = 31 * result + (court != null ? court.hashCode() : 0);
        result = 31 * result + (courtRoom != null ? courtRoom.hashCode() : 0);
        result = 31 * result + (clerkOfTheCourtId != null ? clerkOfTheCourtId.hashCode() : 0);
        result = 31 * result + (clerkOfTheCourtFirstName != null ? clerkOfTheCourtFirstName.hashCode() : 0);
        result = 31 * result + (clerkOfTheCourtLastName != null ? clerkOfTheCourtLastName.hashCode() : 0);
        return result;
    }
}
