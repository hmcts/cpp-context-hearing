package uk.gov.justice.ccr.notepad.result.loader;


import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedList;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultDefinitionConverter;
import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultDefinitionSynonymConverter;
import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultPromptConverter;
import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultPromptFixedListConverter;
import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultPromptSynonymConverter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class FileResultLoader implements ResultLoader {
    @Override
    public List<ResultDefinition> loadResultDefinition() {
        StringToResultDefinitionConverter converter = new StringToResultDefinitionConverter();
        return Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/result/resultDefinition", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));

    }

    @Override
    public List<ResultDefinitionSynonym> loadResultDefinitionSynonym() {
        StringToResultDefinitionSynonymConverter converter = new StringToResultDefinitionSynonymConverter();
        return Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/result/resultDefinitionSynonym", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));
    }

    @Override
    public List<ResultPrompt> loadResultPrompt() {
        Map<String, Set<String>> resultPromptFixedListMap = loadResultPromptFixedList();
        
        StringToResultPromptConverter resultPromptConverter = new StringToResultPromptConverter(resultPromptFixedListMap);
        return Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/result/resultPrompt", true)
                .stream().map(resultPromptConverter::convert).filter(Objects::nonNull).collect(toList()));
    }

    private Map<String, Set<String>> loadResultPromptFixedList() {
        StringToResultPromptFixedListConverter resultPromptFixedListConverter = new StringToResultPromptFixedListConverter();
        List<ResultPromptFixedList> resultPromptFixedLists =  Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/result/resultPromptFixedList", true)
                 .stream().map(resultPromptFixedListConverter::convert).filter(Objects::nonNull).collect(toList()));

        return resultPromptFixedLists.stream().collect(groupingBy(ResultPromptFixedList::getId, Collectors.mapping(ResultPromptFixedList::getValue, Collectors.toCollection(TreeSet::new))));
    }

    @Override
    public List<ResultPromptSynonym> loadResultPromptSynonym() {
        StringToResultPromptSynonymConverter converter = new StringToResultPromptSynonymConverter();
        return Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/result/resultPromptSynonym", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));
    }
}
