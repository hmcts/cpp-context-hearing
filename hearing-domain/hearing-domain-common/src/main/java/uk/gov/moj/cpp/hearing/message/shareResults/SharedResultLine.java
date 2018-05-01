package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SharedResultLine {

    private UUID id;
    private UUID lastSharedResultId;
    private UUID caseId;
    private UUID personId;
    private UUID offenceId;
    private String level;
    private String label;
    private Integer rank;

    private List<Prompt> prompts = new ArrayList<>();

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

    public String getLabel() {
        return label;
    }

    public Integer getRank() {
        return rank;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public SharedResultLine setId(UUID id) {
        this.id = id;
        return this;
    }

    public SharedResultLine setLastSharedResultId(UUID lastSharedResultId) {
        this.lastSharedResultId = lastSharedResultId;
        return this;
    }

    public SharedResultLine setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public SharedResultLine setPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

    public SharedResultLine setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public SharedResultLine setLevel(String level) {
        this.level = level;
        return this;
    }

    public SharedResultLine setLabel(String label) {
        this.label = label;
        return this;
    }

    public SharedResultLine setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public SharedResultLine setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public static SharedResultLine sharedResultLine() {
        return new SharedResultLine();
    }
}

