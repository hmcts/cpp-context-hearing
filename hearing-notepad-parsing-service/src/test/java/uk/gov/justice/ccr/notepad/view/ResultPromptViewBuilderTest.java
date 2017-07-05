package uk.gov.justice.ccr.notepad.view;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.process.ProcessorTest;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;


public class ResultPromptViewBuilderTest {
    Processor processor;
    ResultPromptViewBuilder testObj = new ResultPromptViewBuilder();
    ProcessorTest processorTest = new ProcessorTest();

    @Before
    public void setup() throws ExecutionException {
        processorTest.init();
        processor = processorTest.getProcessor();
    }

    @Test
    public void buildFromKnowledge() throws Exception {
        List<Part> parts = new PartsResolver().getParts("parp");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));
        ResultDefinitionView resultDefinitionView = new ResultDefinitionViewBuilder().buildFromKnowledge(parts,knowledge);

        ResultPromptView result= testObj.buildFromKnowledge(processor.processResultPrompt(resultDefinitionView.getResultCode()));

        assertThat(result.getPromptChoices().size()
                , is(2)
        );
        PromptChoice p1 = result.getPromptChoices().get(0);
        PromptChoice p2 = result.getPromptChoices().get(1);
        assertThat(Arrays.asList(p1.getCode().length(), p1.getLabel(), p1.getType(),p1.getRequired())
                , containsInAnyOrder(Arrays.asList(36, "Prohibited activities", TXT, true).toArray()));
        assertThat(Arrays.asList(p2.getCode().length(), p2.getLabel(), p2.getType(),p2.getRequired())
                , containsInAnyOrder(Arrays.asList(36, "Period of prohibition", DURATION, true).toArray()));
        List<Children> childrenList = p2.getChildren();
        Children c1 = childrenList.get(0);
        Children c2 = childrenList.get(1);
        Children c3 = childrenList.get(2);
        assertThat(Arrays.asList(c1.getType(), c1.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "Years").toArray()));
        assertThat(Arrays.asList(c2.getType(), c2.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "Months").toArray()));
        assertThat(Arrays.asList(c3.getType(), c3.getLabel())
                , containsInAnyOrder(Arrays.asList(INT, "Weeks").toArray()));
    }

}