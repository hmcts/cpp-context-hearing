package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;

public class GroupResultByIndex {

    public Map<Long, Long> run(final List<Long> resultDefinitionIndexes) {
        return resultDefinitionIndexes.stream().collect(groupingBy(
                resultDefinition -> resultDefinition, counting()
        ));
    }
}
