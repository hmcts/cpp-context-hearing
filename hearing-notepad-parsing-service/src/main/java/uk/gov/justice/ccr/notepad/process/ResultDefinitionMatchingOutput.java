package uk.gov.justice.ccr.notepad.process;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

class ResultDefinitionMatchingOutput {

    private ResultDefinition resultDefinition;

    private MatchingType matchingType = MatchingType.UNKNOWN;

    public ResultDefinition getResultDefinition() {
        return resultDefinition;
    }

    public final void setResultDefinition(final ResultDefinition value) {
        resultDefinition = value;
    }

    public MatchingType getMatchingType() {
        return matchingType;
    }

    public final void setMatchingType(final MatchingType value) {
        matchingType = value;
    }

    enum MatchingType {
        EQUALS, SHORT_CODE, CONTAINS, UNKNOWN
    }

}
