package uk.gov.justice.ccr.notepad.result.loader;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.loader.converter.StringToResultPromptConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.Test;

public class ResultPromptsProcessorTest {
    private ResultPromptsProcessor resultPromptsProcessor = new ResultPromptsProcessor(new StringToResultPromptConverter(new HashMap<> ()));

    @Test
    public void shouldGroupResultPromptsByResultDefinition() {

        //given
        List<String> lines = new ArrayList<>();
        lines.add("2\tb81ac80b-81a9-48b6-87b0-cc34610eec97\tRestraining order for period\tabc9bb61-cb5b-4cf7-be24-8866bcd2fc69\tProtected person\tY\tTXT\t\t\t\t1\t250\t\t\t\tN\t10\t\t\t\t\n");
        lines.add("2\tb81ac80b-81a9-48b6-87b0-cc34610eec97\tRestraining order for period\t3054909b-15b6-499f-b44f-67b2b1215c76\tProtected person's address\tY\tTXT\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n");
        lines.add("6\t418b3aa7-65ab-4a4a-bab9-2f96b698118c\tCommunity order England / Wales\td6caa3c4-ec9d-41ec-8f86-2c617ef0d5d9\tEnd Date\tY\tDATE\t\t\t\t\t\t\t\t\tN\t\t\t\t\t");

        //when
        Map<String, List<ResultPrompt>> actual = resultPromptsProcessor.groupByResultDefinition(lines);

        //then
        assertThat(actual.get("b81ac80b-81a9-48b6-87b0-cc34610eec97").size(), is(2));
        assertThat(actual.get("418b3aa7-65ab-4a4a-bab9-2f96b698118c").size(), is(1));
    }

    @Test
    public void shouldOrderResultPrompts() throws Exception {
        Map<String, List<ResultPrompt>> resultPromptsByResultDefinition = new HashMap<>();
        addRowForResultPromptsByResultDefinition(resultPromptsByResultDefinition, "one", 1);
        addRowForResultPromptsByResultDefinition(resultPromptsByResultDefinition, "four", 4);
        addRowForResultPromptsByResultDefinition(resultPromptsByResultDefinition, "twentyFour", 24);

        Map<String, List<ResultPrompt>> actual = resultPromptsProcessor.order(resultPromptsByResultDefinition);

        assertOrderOnList(actual.get("one"));
        assertOrderOnList(actual.get("four"));
        assertOrderOnList(actual.get("twentyFour"));

    }

    private void assertOrderOnList(List<ResultPrompt> resultPrompts) {
        IntStream.rangeClosed(1, resultPrompts.size())
                .forEach(i -> {
                            assertThat(resultPrompts.get(i - 1).getPromptOrder(), is(i));
                            assertThat(resultPrompts.get(i - 1).getLabel(), is(String.valueOf(i)));
                        }
                );
    }

    private void addRowForResultPromptsByResultDefinition(Map<String, List<ResultPrompt>> resultPromptsByResultDefinition, String key, int range) {
        List<ResultPrompt> resultPrompts = IntStream.range(1,range+1).mapToObj(i -> createResultPrompt(key,i)).collect(toList());
        resultPromptsByResultDefinition.put(key, resultPrompts);
    }

    private ResultPrompt createResultPrompt(String resultDefinitionLabel, int order) {
        ResultPrompt resultPrompt = new ResultPrompt();
        resultPrompt.setResultDefinitionLabel(resultDefinitionLabel);
        resultPrompt.setLabel(""+order);
        return resultPrompt;
    }
}
