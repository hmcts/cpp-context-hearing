package uk.gov.moj.cpp.hearing.domain;

import java.util.List;
import java.util.Objects;
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

    public ResultLine(ResultLineDecisionParameters resultLineDecisionParameters) {
        this.id = resultLineDecisionParameters.getId();
        this.lastSharedResultId = resultLineDecisionParameters.getLastSharedResultId();
        this.caseId = resultLineDecisionParameters.getCaseId();
        this.personId = resultLineDecisionParameters.getPersonId();
        this.offenceId = resultLineDecisionParameters.getOffenceId();
        this.level = resultLineDecisionParameters.getLevel();
        this.resultLabel = resultLineDecisionParameters.getResultLabel();
        this.prompts = resultLineDecisionParameters.getPrompts();
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResultLine that = (ResultLine) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (lastSharedResultId != null ? !lastSharedResultId.equals(that.lastSharedResultId) : that.lastSharedResultId != null) {
            return false;
        }
        if (caseId != null ? !caseId.equals(that.caseId) : that.caseId != null) {
            return false;
        }
        if (personId != null ? !personId.equals(that.personId) : that.personId != null) {
            return false;
        }
        if (offenceId != null ? !offenceId.equals(that.offenceId) : that.offenceId != null) {
            return false;
        }
        if (level != null ? !level.equals(that.level) : that.level != null) {
            return false;
        }
        if (resultLabel != null ? !resultLabel.equals(that.resultLabel) : that.resultLabel != null) {
            return false;
        }
        return prompts != null ? prompts.equals(that.prompts) : that.prompts == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLastSharedResultId(), getCaseId(), getPersonId(), getOffenceId(), getLevel(), getResultLabel(), getPrompts());
    }
}
