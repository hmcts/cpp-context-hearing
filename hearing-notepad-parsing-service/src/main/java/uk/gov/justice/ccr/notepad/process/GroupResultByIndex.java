package uk.gov.justice.ccr.notepad.process;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class GroupResultByIndex {

    public Map<Long, Long> run(final List<Long> resultDefinitionIndexes) {
        return resultDefinitionIndexes.stream().collect(Collectors.groupingBy(
                resultDefinition -> resultDefinition
                , Collectors.counting()
        ));
    }
}
