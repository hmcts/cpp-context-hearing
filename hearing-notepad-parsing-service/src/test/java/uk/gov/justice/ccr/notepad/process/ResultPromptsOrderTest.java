package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ResultPromptsOrderTest {

    
    @Test
    public void shouldOrderResultPrompts() throws Exception {
        //given
        List<Integer> promptOrders = Arrays.asList(2, 6, 3, 1, 5, 4);
        List<ResultPrompt> resultPrompts = createResultPromptsWithPromptOrder(promptOrders);

        ResultPromptsOrder resultPromptsOrder = new ResultPromptsOrder();

        //when
        List<ResultPrompt> actual = resultPromptsOrder.process(resultPrompts);

        //then
        List<Integer> actualPromptOrders = actual.stream().map(rp -> rp.getPromptOrder()).collect(toList());
        Collections.sort(promptOrders);
        assertThat(actualPromptOrders, is(equalTo(promptOrders)));
    }

    private List<ResultPrompt> createResultPromptsWithPromptOrder(List<Integer> promptOrders ) {
       return  promptOrders.stream().map(i -> {
            ResultPrompt rp = new ResultPrompt();
            rp.setPromptOrder(i);
            return rp;
        }).collect(toList());
    }
}