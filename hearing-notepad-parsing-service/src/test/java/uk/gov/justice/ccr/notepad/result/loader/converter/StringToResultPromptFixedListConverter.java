package uk.gov.justice.ccr.notepad.result.loader.converter;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedList;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedListKey;

import java.util.regex.Pattern;

public final class StringToResultPromptFixedListConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final int MAX_TOKEN = ResultPromptFixedListKey.values().length;

    public ResultPromptFixedList convert(final String line) {
        final String[] values = TAB_SPLITTER.split(line, -1);
        if (MAX_TOKEN == values.length) {
            ResultPromptFixedList resultPromptFixedList = new ResultPromptFixedList();
            resultPromptFixedList.setId(values[ResultPromptFixedListKey.UUID.getOrder()].trim());
            resultPromptFixedList.setValue(values[ResultPromptFixedListKey.VALUE.getOrder()].trim());

            return resultPromptFixedList;
        }
        return null;
    }
}
