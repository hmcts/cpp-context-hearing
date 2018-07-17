package uk.gov.justice.ccr.notepad.result.loader;


import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileResultLoader implements ResultLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileResultLoader.class);

    @Override
    public List<ResultDefinition> loadResultDefinition(final LocalDate hearingDate) {
        final StringToResultDefinitionConverter converter = new StringToResultDefinitionConverter();
        return unmodifiableList(new ResourceFileReader().getLines("/file-store/result-definitions.tdf", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));
    }

    @Override
    public List<ResultDefinitionSynonym> loadResultDefinitionSynonym(final LocalDate hearingDate) {
        final StringToResultDefinitionSynonymConverter converter = new StringToResultDefinitionSynonymConverter();
        return unmodifiableList(new ResourceFileReader().getLines("/file-store/result-definition-synonyms.tdf", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));
    }

    @Override
    public List<ResultPrompt> loadResultPrompt(final LocalDate hearingDate) {
        Map<String, Set<String>> resultPromptFixedListMap = loadResultPromptFixedList();

        StringToResultPromptConverter resultPromptConverter = new StringToResultPromptConverter(resultPromptFixedListMap);

        List<String> lines = new ResourceFileReader().getLines("/file-store/result-prompts.tdf", true);
        ResultPromptsProcessor resultPromptsProcessor = new ResultPromptsProcessor(resultPromptConverter);
        Map<String, List<ResultPrompt>> promptsGroupedByResultDefinition = resultPromptsProcessor.groupByResultDefinition(lines);
        LOGGER.debug("promptsGroupedByResultDefinition:" + promptsGroupedByResultDefinition);

        Map<String, List<ResultPrompt>> orderedResultPrompts = resultPromptsProcessor.order(promptsGroupedByResultDefinition);
        LOGGER.debug("orderedResultPrompts:" + orderedResultPrompts);

        return orderedResultPrompts.values().stream().flatMap(List::stream).filter(Objects::nonNull).collect(toList());
    }

    private Map<String, Set<String>> loadResultPromptFixedList() {
        StringToResultPromptFixedListConverter resultPromptFixedListConverter = new StringToResultPromptFixedListConverter();
        final List<ResultPromptFixedList> resultPromptFixedLists = unmodifiableList(new ResourceFileReader().getLines("/file-store/result-prompt-fixed-list.tdf", true)
                .stream().map(resultPromptFixedListConverter::convert).filter(Objects::nonNull).collect(toList()));

        return resultPromptFixedLists.stream().collect(groupingBy(ResultPromptFixedList::getId, mapping(ResultPromptFixedList::getValue, toCollection(TreeSet::new))));
    }

    @Override
    public List<ResultPromptSynonym> loadResultPromptSynonym(final LocalDate hearingDate) {
        StringToResultPromptSynonymConverter converter = new StringToResultPromptSynonymConverter();
        return unmodifiableList(new ResourceFileReader().getLines("/file-store/result-prompt-synonyms.tdf", true)
                .stream().map(converter::convert).filter(Objects::nonNull).collect(toList()));
    }
}
