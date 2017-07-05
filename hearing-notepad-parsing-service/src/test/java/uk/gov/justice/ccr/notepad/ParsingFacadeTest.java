package uk.gov.justice.ccr.notepad;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;
import uk.gov.justice.services.test.utils.core.random.StringGenerator;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParsingFacadeTest {
    @Mock
    Processor processor;

    @InjectMocks
    ParsingFacade testObj;

    @Test
    public void processParts() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp sus");

        testObj.processParts(parts);

        verify(processor, times(1)).processParts(any());
    }

    @Test
    public void processResultDefinition() throws Exception {
        testObj.processPrompt(new StringGenerator().next());

        verify(processor, times(1)).processResultPrompt(any());
    }

}