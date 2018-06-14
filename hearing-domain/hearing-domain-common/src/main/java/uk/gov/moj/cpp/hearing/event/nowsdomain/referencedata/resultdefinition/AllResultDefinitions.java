package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.util.List;

public class AllResultDefinitions {

    private List<ResultDefinition> resultDefinitions;

    public static AllResultDefinitions allResultDefinitions() {
        return new AllResultDefinitions();
    }

    public List<ResultDefinition> getResultDefinitions() {
        return this.resultDefinitions;
    }

    public AllResultDefinitions setResultDefinitions(List<ResultDefinition> resultDefinitions) {
        this.resultDefinitions = resultDefinitions;
        return this;
    }
}
