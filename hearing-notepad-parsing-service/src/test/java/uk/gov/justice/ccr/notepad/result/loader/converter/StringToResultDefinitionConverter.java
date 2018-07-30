package uk.gov.justice.ccr.notepad.result.loader.converter;


import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionKey.RESULT_WORD_GROUP;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionKey;

import java.util.regex.Pattern;

public class StringToResultDefinitionConverter {

    private static final Pattern TAB_SPLITTER = Pattern.compile("\t");
    private static final Pattern COMMA_SPLITTER = Pattern.compile(",");

    private static final int MAX_TOKEN = ResultDefinitionKey.values().length;

    public ResultDefinition convert(final String line) {

        final String[] values = TAB_SPLITTER.split(line, -1);

        if (MAX_TOKEN == values.length) {
            ResultDefinition resultDefinition = new ResultDefinition();
            resultDefinition.setId(values[ResultDefinitionKey.UUID.getOrder()].trim());
            resultDefinition.setLabel(values[ResultDefinitionKey.LABEL.getOrder()].trim());
            resultDefinition.setShortCode(values[ResultDefinitionKey.SHORT_CODE.getOrder()].toLowerCase().trim());
            resultDefinition.setLevel(values[ResultDefinitionKey.LEVEL.getOrder()]);
            resultDefinition.setKeywords(newArrayList(COMMA_SPLITTER.split(values[RESULT_WORD_GROUP.getOrder()]))
                    .stream()
                    .map(word -> word.replaceAll("\"", ""))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(toList())
            );
            return resultDefinition;
        }
        return null;
    }
}
