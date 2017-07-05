package uk.gov.moj.cpp.hearing.steps.data;

import uk.gov.moj.cpp.hearing.domain.ResultPrompt;

import java.util.List;
import java.util.UUID;

public class ResultLineData {

    private final UUID id;
    private final UUID lastSharedResultId;
    private final UUID caseId;
    private final UUID personId;
    private final UUID offenceId;
    private final ResultLevel level;

    private final String resultLabel;
    private final List<ResultPrompt> prompts;

    public ResultLineData(final UUID id, final UUID lastSharedResultId, final UUID caseId, final UUID personId,
                          final UUID offenceId, final ResultLevel level, final String resultLabel,
                          final List<ResultPrompt> prompts) {
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

    public ResultLevel getLevel() {
        return level;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public List<ResultPrompt> getPrompts() {
        return prompts;
    }
}
