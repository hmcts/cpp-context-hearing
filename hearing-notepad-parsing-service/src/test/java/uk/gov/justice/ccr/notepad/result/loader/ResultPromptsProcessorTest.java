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
    public void shouldGroupResultPromptsByResultDefinition() throws Exception {

        //given
        List<String> lines = new ArrayList<>();
        lines.add("Restraining order for period\tProtected person\tY\tTXT\t\tabc,xyz\tConviction / acquittal");
        lines.add("Restraining order for period\tProtected person\tY\tTXT\t\t\t");
        lines.add("Community order England / Wales\tEnd Date\tY\tDATE\t\t\t");

        //when
        Map<String, List<ResultPrompt>> actual = resultPromptsProcessor.groupByResultDefinition(lines);

        //then
        assertThat(actual.get("Restraining order for period").size(), is(2));
        assertThat(actual.get("Community order England / Wales").size(), is(1));
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