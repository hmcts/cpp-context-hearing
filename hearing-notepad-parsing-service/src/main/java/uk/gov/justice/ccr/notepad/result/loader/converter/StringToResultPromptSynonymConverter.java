package uk.gov.justice.ccr.notepad.result.loader.converter;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonymKey;

import java.util.regex.Pattern;

public final class StringToResultPromptSynonymConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final int MAX_TOKEN = ResultPromptSynonymKey.values().length;

    public ResultPromptSynonym convert(final String line) {

        final String[] values = TAB_SPLITTER.split(line, -1);

        if (MAX_TOKEN == values.length) {
            ResultPromptSynonym resultPromptSynonym = new ResultPromptSynonym();
            resultPromptSynonym.setWord(values[ResultPromptSynonymKey.WORD.getOrder()].replaceAll(" ", "").trim().toLowerCase());
            resultPromptSynonym.setSynonym(values[ResultPromptSynonymKey.SYNONYM.getOrder()].trim().toLowerCase());
            return resultPromptSynonym;
        }

        return null;
    }
}
