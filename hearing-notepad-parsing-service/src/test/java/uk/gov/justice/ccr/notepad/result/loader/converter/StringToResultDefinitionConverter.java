package uk.gov.justice.ccr.notepad.result.loader.converter;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionKey;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

public class StringToResultDefinitionConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final Pattern COMMA_SPLITTER = Pattern.compile(",");

    private static final int MAX_TOKEN = ResultDefinitionKey.values().length;

    public ResultDefinition convert(final String line) {

        final String[] values = TAB_SPLITTER.split(line, -1);

        if (MAX_TOKEN == values.length) {
            ResultDefinition resultDefinition = new ResultDefinition();
            resultDefinition.setId(UUID.randomUUID().toString());
            resultDefinition.setLabel(values[ResultDefinitionKey.LABEL.getOrder()].trim());
            resultDefinition.setShortCode(values[ResultDefinitionKey.SHORT_CODE.getOrder()].toLowerCase().trim());
            resultDefinition.setLevel(values[ResultDefinitionKey.LEVEL.getOrder()]);
            resultDefinition.setKeywords(Arrays.asList(COMMA_SPLITTER.split(values[ResultDefinitionKey.KEYWORDS.getOrder()].replaceAll(" ", "").toLowerCase())));
            return resultDefinition;
        }
        return null;
    }
}
