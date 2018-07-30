package uk.gov.justice.ccr.notepad.result.loader;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultPromptConverter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResultPromptsProcessor {

    private final StringToResultPromptConverter resultPromptConverter;

    public ResultPromptsProcessor(StringToResultPromptConverter resultPromptConverter) {
        this.resultPromptConverter = resultPromptConverter;
    }

    public Map<String, List<ResultPrompt>> groupByResultDefinition(List<String> lines) {
      List<ResultPrompt> rps=  lines.stream()
                .map(resultPromptConverter::convert).filter(Objects::nonNull).collect(toList());

      return rps.stream().collect(groupingBy(prompt -> prompt.getResultDefinitionId().toString(), LinkedHashMap::new, toList()));
    }

    public Map<String, List<ResultPrompt>> order(Map<String, List<ResultPrompt>> resultPromptsByResultDefinition) {
        return resultPromptsByResultDefinition.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        elem -> IntStream.range(0, elem.getValue().size())
                                .mapToObj(i -> {
                                    ResultPrompt rp = elem.getValue().get(i);
                                    rp.setPromptOrder(i+1);
                                    return rp;
                                }).collect(toList())));
    }
}
