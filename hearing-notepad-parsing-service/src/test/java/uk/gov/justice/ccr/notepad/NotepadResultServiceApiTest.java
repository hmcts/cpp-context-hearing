package uk.gov.justice.ccr.notepad;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.view.ResultDefinitionView;
import uk.gov.justice.ccr.notepad.view.ResultDefinitionViewBuilder;
import uk.gov.justice.ccr.notepad.view.ResultPromptView;
import uk.gov.justice.ccr.notepad.view.ResultPromptViewBuilder;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NotepadResultServiceApiTest {
    private static final String NAME_RESULT_DEFINITION_RESPONSE = "hearing.notepad.parse-result-definition-response";

    private static final String NAME_RESULT_PROMPT_RESPONSE = "hearing.notepad.parse-result-prompt-response";
    @InjectMocks
    private NotepadResultServiceApi testObj;

    @Mock
    private ResultDefinitionViewBuilder resultDefinitionViewBuilder;

    @Mock
    private ResultPromptViewBuilder resultPromptViewBuilder;

    @Mock
    private ParsingFacade parsingFacade;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private Enveloper enveloper;

    @Captor
    private ArgumentCaptor<LocalDate> localDateArgumentCaptor;

    @Captor
    private ArgumentCaptor<LocalDate> parsingLocalDateArgumentCaptor;


    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private JsonEnvelope responseJson;

    @Mock
    private ResultDefinitionView resultDefinitionView;

    @Test
    public void getResultDefinition() throws Exception {
        JsonObject payload = Json.createObjectBuilder()
                .add("originalText", "imp sus")
                .add("orderedDate", "2018-06-01")
                .build();
        when(jsonEnvelope.payloadAsJsonObject()).thenReturn(payload);
        when(resultDefinitionViewBuilder.buildFromKnowledge(any(), any())).thenReturn(resultDefinitionView);
        when(enveloper.withMetadataFrom(jsonEnvelope, NAME_RESULT_DEFINITION_RESPONSE)).thenReturn(function);

        testObj.getResultDefinition(jsonEnvelope);


        verify(parsingFacade, times(1)).processParts(any(), parsingLocalDateArgumentCaptor.capture());
        verify(parsingFacade, times(1)).lazyLoad(any(JsonEnvelope.class), localDateArgumentCaptor.capture());
        final LocalDate localDate = localDateArgumentCaptor.getValue();
        assertThat(localDate, equalTo(LocalDate.parse("2018-06-01")));
        assertThat(parsingLocalDateArgumentCaptor.getValue(), equalTo(LocalDate.parse("2018-06-01")));
        verify(resultDefinitionView, times(1)).setOriginalText("imp sus");
        verify(resultDefinitionView, times(1)).setOrderedDate("2018-06-01");
        verify(resultDefinitionViewBuilder, times(1)).buildFromKnowledge(any(),any());
        verify(objectToJsonObjectConverter, times(1)).convert(any());
        verify(enveloper, times(1)).withMetadataFrom(any(),any());
    }

    @Test
    public void getResultPrompt() throws Exception {
        JsonObject payload = Json.createObjectBuilder()
                .add("orderedDate", "2018-06-01")
                .add("resultCode", "123")
                .build();
        when(jsonEnvelope.payloadAsJsonObject()).thenReturn(payload);
        when(resultPromptViewBuilder.buildFromKnowledge(any())).thenReturn(new ResultPromptView());
        when(enveloper.withMetadataFrom(jsonEnvelope, NAME_RESULT_PROMPT_RESPONSE)).thenReturn(function);

        testObj.getResultPrompt(jsonEnvelope);

        verify(parsingFacade, times(1)).lazyLoad(any(JsonEnvelope.class), localDateArgumentCaptor.capture());
        verify(parsingFacade, times(1)).processPrompt(any(String.class), parsingLocalDateArgumentCaptor.capture());
        final LocalDate localDate = localDateArgumentCaptor.getValue();
        assertThat(localDate, equalTo(LocalDate.parse("2018-06-01")));
        assertThat(parsingLocalDateArgumentCaptor.getValue(), equalTo(LocalDate.parse("2018-06-01")));
        verify(resultPromptViewBuilder, times(1)).buildFromKnowledge(any());
        verify(objectToJsonObjectConverter, times(1)).convert(any());
        verify(enveloper, times(1)).withMetadataFrom(any(),any());
    }

    @Test
    public void buildResultDefinitionView() throws Exception {
        when(resultDefinitionViewBuilder.buildFromKnowledge(any(), any())).thenReturn(new ResultDefinitionView());

        final ResultDefinitionView resultDefinitionView = testObj.buildResultDefinitionView(
                "imp sus", "2014-06-04", newArrayList(), new Knowledge());

        assertThat(resultDefinitionView.getOriginalText()
                , is("imp sus")
        );
        assertThat(resultDefinitionView.getOrderedDate(), is("2014-06-04"));
        assertThat(resultDefinitionView.getResultLineId().length()
                , is(36)
        );

    }

}
