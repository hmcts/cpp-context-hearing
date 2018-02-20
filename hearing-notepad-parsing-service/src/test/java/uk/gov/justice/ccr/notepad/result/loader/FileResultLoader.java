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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileResultLoader implements ResultLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileResultLoader.class);


    @Override
    public List<ResultDefinition> loadResultDefinition() {
        StringToResultDefinitionConverter converter = new StringToResultDefinitionConverter();
        return Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/b6a117cb-c284-4103-958d-34ffcf200b24", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));

    }

    @Override
    public List<ResultDefinitionSynonym> loadResultDefinitionSynonym() {
        StringToResultDefinitionSynonymConverter converter = new StringToResultDefinitionSynonymConverter();
        return Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/237ece8d-95eb-4dca-a28e-9448707568b4", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));
    }

    @Override
    public List<ResultPrompt> loadResultPrompt() {
        Map<String, Set<String>> resultPromptFixedListMap = loadResultPromptFixedList();
        
        StringToResultPromptConverter resultPromptConverter = new StringToResultPromptConverter(resultPromptFixedListMap);

        List<String> lines = new ResourceFileReader().getLines("/file-store/7afc734b-ea8c-4458-8790-a8d2fb4db30f", true);
        ResultPromptsProcessor resultPromptsProcessor = new ResultPromptsProcessor(resultPromptConverter);
        Map<String, List<ResultPrompt>> promptsGroupedByResultDefinition = resultPromptsProcessor.groupByResultDefinition(lines);
        LOGGER.debug("promptsGroupedByResultDefinition:"+promptsGroupedByResultDefinition);

        Map<String, List<ResultPrompt>> orderedResultPrompts = resultPromptsProcessor.order(promptsGroupedByResultDefinition);
        LOGGER.debug("orderedResultPrompts:"+orderedResultPrompts);

        return orderedResultPrompts.values().stream().flatMap(List::stream).filter(Objects::nonNull).collect(toList());
    }

    private Map<String, Set<String>> loadResultPromptFixedList() {
        StringToResultPromptFixedListConverter resultPromptFixedListConverter = new StringToResultPromptFixedListConverter();
        List<ResultPromptFixedList> resultPromptFixedLists =  Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/8c930efb-01ae-417e-ac2b-75a19d581f36", true)
                 .stream().map(resultPromptFixedListConverter::convert).filter(Objects::nonNull).collect(toList()));

        return resultPromptFixedLists.stream().collect(groupingBy(ResultPromptFixedList::getId, Collectors.mapping(ResultPromptFixedList::getValue, Collectors.toCollection(TreeSet::new))));
    }

    @Override
    public List<ResultPromptSynonym> loadResultPromptSynonym() {
        StringToResultPromptSynonymConverter converter = new StringToResultPromptSynonymConverter();
        return Collections.unmodifiableList(new ResourceFileReader().getLines("/file-store/cbd59d34-1a5f-4788-a43c-29e1f9da329e", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));
    }
}
