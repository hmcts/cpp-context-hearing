package uk.gov.justice.ccr.notepad;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;
import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class ParsingFacadeTest {
    @Mock
    private Processor processor;

    @Mock
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Mock
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private ParsingFacade testObj;

    @Mock
    private JsonEnvelope envelope;

    @Before
    public void before() throws Exception {
        FieldUtils.writeField(testObj, "redisCommonCacheEnabled", "false", true);
        FieldUtils.writeField(testObj, "redisContextHearingCacheEnabled", "false", true);
    }

    @Test
    public void processParts() {
        List<Part> parts = new PartsResolver().getParts("imp sus");

        final LocalDate hearingDate = LocalDate.now();
        testObj.processParts(parts, hearingDate);

        verify(processor, times(1)).processParts(any(), eq(hearingDate));
    }

    @Test
    public void processResultDefinition() {
        final LocalDate hearingDate = LocalDate.now();
        final String definitionId = STRING.next();
        testObj.processPrompt(definitionId, hearingDate);

        verify(processor, times(1)).processResultPrompt(definitionId, hearingDate);
    }

    @Test
    public void retrieveChildResultDefinitionDetail()  {
        final LocalDate hearingDate = LocalDate.now();
        final String definitionId = STRING.next();
        testObj.retrieveChildResultDefinitionDetail(definitionId, hearingDate);

        verify(processor, times(1)).retrieveChildResultDefinitionDetail(definitionId, hearingDate);
    }

    @Test
    public void shouldCallLazyLoadOfTheProcessor() {
        final LocalDate hearingDate = LocalDate.now();

        testObj.lazyLoad(envelope, hearingDate);

        verify(processor).lazyLoad(eq(envelope), eq(hearingDate));
    }

    @Test
    public void retrieveResultDefinition() {
        final LocalDate orderedDate = LocalDate.now();
        final String resultDefinitionId = STRING.next();
        testObj.retrieveResultDefinitionById(resultDefinitionId,orderedDate);
        verify(processor, times(1)).retrieveResultDefinitionById(resultDefinitionId, orderedDate);
    }

}