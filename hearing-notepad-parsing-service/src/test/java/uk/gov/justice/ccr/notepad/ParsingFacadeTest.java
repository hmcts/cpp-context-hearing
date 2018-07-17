package uk.gov.justice.ccr.notepad;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParsingFacadeTest {
    @Mock
    private Processor processor;

    @InjectMocks
    private ParsingFacade testObj;

    @Mock
    private JsonEnvelope envelope;

    @Test
    public void processParts() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp sus");

        final LocalDate hearingDate = LocalDate.now();
        testObj.processParts(parts, hearingDate);

        verify(processor, times(1)).processParts(any(), eq(hearingDate));
    }

    @Test
    public void processResultDefinition() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        final String definitionId = STRING.next();
        testObj.processPrompt(definitionId, hearingDate);

        verify(processor, times(1)).processResultPrompt(definitionId, hearingDate);
    }

    @Test
    public void shouldCallLazyLoadOfTheProcessor() throws Exception {
        final LocalDate hearingDate = LocalDate.now();

        testObj.lazyLoad(envelope, hearingDate);

        verify(processor).lazyLoad(eq(envelope), eq(hearingDate));
    }

}