package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.util.List;
import java.util.UUID;

public class ResultDefinitionRule {
    private final UUID parentResultDefinitionId;
    private final List<ResultDefinitionRuleType> childResultDefinitions;

    public ResultDefinitionRule(final UUID parentResultDefinitionId, final List<ResultDefinitionRuleType> resultDefinitionRuleTypeList) {
        this.parentResultDefinitionId = parentResultDefinitionId;
        this.childResultDefinitions = resultDefinitionRuleTypeList;
    }

    public UUID getParentResultDefinitionId() {
        return parentResultDefinitionId;
    }

    public List<ResultDefinitionRuleType> getChildResultDefinitions() {
        return childResultDefinitions;
    }
}
