package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.util.Comparator;
import java.util.List;

public class ResultPromptsOrder {
    public List<ResultPrompt> process(List<ResultPrompt> resultPrompts) {
        return resultPrompts.stream().sorted(Comparator.comparingInt(ResultPrompt::getPromptOrder)).collect(toList());
    }
}
