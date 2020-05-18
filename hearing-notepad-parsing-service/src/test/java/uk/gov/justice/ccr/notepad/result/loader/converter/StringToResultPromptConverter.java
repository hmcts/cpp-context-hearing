package uk.gov.justice.ccr.notepad.result.loader.converter;


import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableMap;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.DURATION_ELEMENT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.FIXED_LIST_UUID;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.HIDDEN;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.LABEL;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.PROMPT_REFERENCE;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.PROMPT_TYPE;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.RESULT_DEFINITION_LABEL;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.RESULT_DEFINITION_UUID;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.RESULT_PROMPT_RULE;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.RESULT_PROMPT_WORD_GROUP;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.UUID;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey.values;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.isFixedListType;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.valueOf;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class StringToResultPromptConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final Pattern COMMA_SPLITTER = Pattern.compile(",");
    private static final int MAX_TOKEN = values().length;
    private final Map<String, Set<String>> resultPromptFixedListMap;

    public StringToResultPromptConverter(final Map<String, Set<String>> fixedListMap) {
        this.resultPromptFixedListMap = fixedListMap != null ? unmodifiableMap(fixedListMap) : null;
    }

    public ResultPrompt convert(final String line) {
        final String[] values = TAB_SPLITTER.split(line, -1);
        if (MAX_TOKEN == values.length) {
            final ResultPrompt resultPrompt = new ResultPrompt();
            resultPrompt.setId(values[UUID.getOrder()].trim());
            resultPrompt.setLabel(values[LABEL.getOrder()].trim());
            resultPrompt.setResultDefinitionId(fromString(values[RESULT_DEFINITION_UUID.getOrder()].trim()));
            resultPrompt.setResultDefinitionLabel(values[RESULT_DEFINITION_LABEL.getOrder()].trim());
            final String durationElement = values[DURATION_ELEMENT.getOrder()].trim();
            if (!durationElement.isEmpty()) {
                resultPrompt.setType(DURATION);
            } else {
                resultPrompt.setType(valueOf(values[PROMPT_TYPE.getOrder()].trim().toUpperCase()));
            }
            resultPrompt.setResultPromptRule(values[RESULT_PROMPT_RULE.getOrder()]);
            resultPrompt.setDurationElement(durationElement);
            resultPrompt.setKeywords(newArrayList(COMMA_SPLITTER.split(values[RESULT_PROMPT_WORD_GROUP.getOrder()]))
                    .stream()
                    .map(word -> word.replaceAll("\"", ""))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(toList())
            );
            final String fixedListId = values[FIXED_LIST_UUID.getOrder()];
            if (fixedListId != null && isFixedListType(resultPrompt.getType())) {
                resultPrompt.setFixedList(resultPromptFixedListMap.get(fixedListId));
            }
            resultPrompt.setReference(values[PROMPT_REFERENCE.getOrder()]);
            resultPrompt.setHidden(Boolean.valueOf(values[HIDDEN.getOrder()]));
            return resultPrompt;
        }
        return null;
    }
}
