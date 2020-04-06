package uk.gov.justice.ccr.notepad.result.cache.model;

import java.util.UUID;

public class ChildResultDefinition {

    private final UUID childResultDefinitionId;
    private final String ruleType;

    public ChildResultDefinition(final UUID childResultDefinitionId, final String ruleType) {
        this.childResultDefinitionId = childResultDefinitionId;
        this.ruleType = ruleType;
    }

    public UUID getChildResultDefinitionId() {
        return childResultDefinitionId;
    }

    public String getRuleType() {
        return ruleType;
    }

    @Override
    public String toString() {
        return String.format("ChildResultDefinition{childResultDefinitionId='%s', ruleType='%s}", childResultDefinitionId, ruleType);
    }
}
