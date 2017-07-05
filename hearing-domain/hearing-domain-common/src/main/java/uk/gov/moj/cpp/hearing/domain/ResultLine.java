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

    public ResultLine(final UUID id, final UUID lastSharedResultId, final UUID caseId, final UUID personId, final UUID offenceId,
                      final String level, final String resultLabel, final List<ResultPrompt> prompts) {
        this.id = id;
        this.lastSharedResultId = lastSharedResultId;
        this.caseId = caseId;
        this.personId = personId;
        this.offenceId = offenceId;
        this.level = level;
        this.resultLabel = resultLabel;
        this.prompts = prompts;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ResultLine that = (ResultLine) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getLastSharedResultId(), that.getLastSharedResultId()) &&
                Objects.equals(getCaseId(), that.getCaseId()) &&
                Objects.equals(getPersonId(), that.getPersonId()) &&
                Objects.equals(getOffenceId(), that.getOffenceId()) &&
                Objects.equals(getLevel(), that.getLevel()) &&
                Objects.equals(getResultLabel(), that.getResultLabel()) &&
                Objects.equals(getPrompts(), that.getPrompts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLastSharedResultId(), getCaseId(), getPersonId(), getOffenceId(), getLevel(), getResultLabel(), getPrompts());
    }
}
