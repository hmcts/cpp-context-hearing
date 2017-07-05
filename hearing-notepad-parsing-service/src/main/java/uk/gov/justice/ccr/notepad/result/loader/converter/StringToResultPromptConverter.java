package uk.gov.justice.ccr.notepad.result.loader.converter;


import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.valueOf;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptKey;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class StringToResultPromptConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final Pattern COMMA_SPLITTER = Pattern.compile(",");
    private static final int MAX_TOKEN = ResultPromptKey.values().length;
    private final Map<String, Set<String>> resultPromptFixedListMap;

    public StringToResultPromptConverter(Map<String, Set<String>> fixedListMap) {
        this.resultPromptFixedListMap = fixedListMap;
    }

    public ResultPrompt convert(final String line) {
        final String[] values = TAB_SPLITTER.split(line, -1);
        if (MAX_TOKEN == values.length) {
            ResultPrompt resultPrompt = new ResultPrompt();
            resultPrompt.setLabel(values[ResultPromptKey.LABEL.getOrder()].trim());
            resultPrompt.setResultDefinitionLabel(values[ResultPromptKey.RESULT_DEFINITION_LABEL.getOrder()].trim());
            String durationElement = values[ResultPromptKey.DURATION_ELEMENT.getOrder()].trim();
            if (!durationElement.isEmpty()) {
                resultPrompt.setType(DURATION);
            } else {
                resultPrompt.setType(valueOf(values[ResultPromptKey.TYPE.getOrder()].trim().toUpperCase()));
            }
            resultPrompt.setMandatory(values[ResultPromptKey.MANDATORY.getOrder()]);
            resultPrompt.setDurationElement(durationElement);
            resultPrompt.setKeywords(Arrays.asList(COMMA_SPLITTER.split(values[ResultPromptKey.KEYWORDS.getOrder()].replaceAll(" ", "").toLowerCase())));
            String fixedListId = values[ResultPromptKey.FIXED_LIST_ID.getOrder()];
            if(fixedListId != null && ResultType.FIXL == resultPrompt.getType()){
                resultPrompt.setFixedList(resultPromptFixedListMap.get(fixedListId));
            }
            return resultPrompt;
        }
        return null;
    }
}
