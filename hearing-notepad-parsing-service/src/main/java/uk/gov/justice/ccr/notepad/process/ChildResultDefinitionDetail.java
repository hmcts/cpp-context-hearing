package uk.gov.justice.ccr.notepad.process;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.util.List;

@SuppressWarnings({"squid:S2384"})
public class ChildResultDefinitionDetail {

    private ResultDefinition parentResultDefinition;

    private List<ResultDefinition> resultDefinitions;

    public ChildResultDefinitionDetail(ResultDefinition resultDefinition, List<ResultDefinition> resultDefinitions) {
        this.parentResultDefinition = resultDefinition;
        this.resultDefinitions = resultDefinitions;
    }

    public ResultDefinition getParentResultDefinition() {
        return parentResultDefinition;
    }

    public List<ResultDefinition> getResultDefinitions() {
        return resultDefinitions;
    }

}
