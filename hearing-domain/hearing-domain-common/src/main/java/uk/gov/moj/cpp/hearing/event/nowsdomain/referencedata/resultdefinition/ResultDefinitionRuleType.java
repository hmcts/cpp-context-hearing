package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.util.UUID;

public class ResultDefinitionRuleType {

    private UUID childResultDefinitionId;
    private String ruleType;

    public ResultDefinitionRuleType(final UUID childResultDefinitionId, final String ruleType) {
        this.childResultDefinitionId = childResultDefinitionId;
        this.ruleType = ruleType;
    }

    public UUID getChildResultDefinitionId() {
        return childResultDefinitionId;
    }

    public String getRuleType() {
        return ruleType;
    }
}
