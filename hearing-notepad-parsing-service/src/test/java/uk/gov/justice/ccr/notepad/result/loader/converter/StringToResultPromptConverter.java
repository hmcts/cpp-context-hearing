package uk.gov.justice.ccr.notepad.result.loader.converter;


import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableMap;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.valueOf;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class StringToResultPromptConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final Pattern COMMA_SPLITTER = Pattern.compile(",");
    private static final int MAX_TOKEN = ResultPromptKey.values().length;
    private final Map<String, Set<String>> resultPromptFixedListMap;

    public StringToResultPromptConverter(final Map<String, Set<String>> fixedListMap) {
        this.resultPromptFixedListMap = fixedListMap != null ? unmodifiableMap(fixedListMap) : null;
    }

    public ResultPrompt convert(final String line) {
        final String[] values = TAB_SPLITTER.split(line, -1);
        if (MAX_TOKEN == values.length) {
            ResultPrompt resultPrompt = new ResultPrompt();
            resultPrompt.setId(values[ResultPromptKey.UUID.getOrder()].trim());
            resultPrompt.setLabel(values[ResultPromptKey.LABEL.getOrder()].trim());
            resultPrompt.setResultDefinitionId(fromString(values[ResultPromptKey.RESULT_DEFINITION_UUID.getOrder()].trim()));
            resultPrompt.setResultDefinitionLabel(values[ResultPromptKey.RESULT_DEFINITION_LABEL.getOrder()].trim());
            String durationElement = values[ResultPromptKey.DURATION_ELEMENT.getOrder()].trim();
            if (!durationElement.isEmpty()) {
                resultPrompt.setType(DURATION);
            } else {
                resultPrompt.setType(valueOf(values[ResultPromptKey.PROMPT_TYPE.getOrder()].trim().toUpperCase()));
            }
            resultPrompt.setMandatory(values[ResultPromptKey.MANDATORY.getOrder()].equalsIgnoreCase("Y"));
            resultPrompt.setDurationElement(durationElement);
            resultPrompt.setKeywords(newArrayList(COMMA_SPLITTER.split(values[ResultPromptKey.RESULT_PROMPT_WORD_GROUP.getOrder()]))
                    .stream()
                    .map(word -> word.replaceAll("\"", ""))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(toList())
            );
            String fixedListId = values[ResultPromptKey.FIXED_LIST_UUID.getOrder()];
            if(fixedListId != null && ResultType.FIXL == resultPrompt.getType()){
                resultPrompt.setFixedList(resultPromptFixedListMap.get(fixedListId));
            }
            return resultPrompt;
        }
        return null;
    }
}
