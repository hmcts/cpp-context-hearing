package uk.gov.justice.ccr.notepad.result.loader.converter;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonymKey;

import java.util.regex.Pattern;

public final class StringToResultDefinitionSynonymConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final int MAX_TOKEN = ResultDefinitionSynonymKey.values().length;

    public ResultDefinitionSynonym convert(final String line) {

        final String[] values = TAB_SPLITTER.split(line, -1);

        if (MAX_TOKEN == values.length) {
            ResultDefinitionSynonym resultDefinitionSynonym = new ResultDefinitionSynonym();
            resultDefinitionSynonym.setWord(values[ResultDefinitionSynonymKey.WORD.getOrder()].replaceAll(" ", "").trim().toLowerCase());
            resultDefinitionSynonym.setSynonym(values[ResultDefinitionSynonymKey.SYNONYM.getOrder()].trim().toLowerCase());
            return resultDefinitionSynonym;
        }
        return null;
    }
}
