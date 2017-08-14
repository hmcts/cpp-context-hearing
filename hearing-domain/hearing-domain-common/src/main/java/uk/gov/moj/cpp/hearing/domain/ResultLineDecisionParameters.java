package uk.gov.moj.cpp.hearing.domain;

import java.util.List;
import java.util.UUID;

public class ResultLineDecisionParameters {
    private UUID id;
    private UUID lastSharedResultId;
    private UUID caseId;
    private UUID personId;
    private UUID offenceId;
    private String level;
    private String resultLabel;
    private List<ResultPrompt> prompts;

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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setLastSharedResultId(UUID lastSharedResultId) {
        this.lastSharedResultId = lastSharedResultId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public void setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    public void setPrompts(List<ResultPrompt> prompts) {
        this.prompts = prompts;
    }
}
