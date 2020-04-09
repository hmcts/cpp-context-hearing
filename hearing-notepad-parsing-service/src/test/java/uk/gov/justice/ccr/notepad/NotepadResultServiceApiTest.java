package uk.gov.justice.ccr.notepad;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.ccr.notepad.process.ChildResultDefinitionDetail;
import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.view.*;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

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

    @Captor
    private ArgumentCaptor<Knowledge> knowledgeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Boolean> excludedFromResults;

    @Captor
    private ArgumentCaptor<List<uk.gov.justice.ccr.notepad.view.ChildResultDefinition>> childResultDefinitionsCaptor;

    @Captor
    private ArgumentCaptor<List<Part>> partsCaptor;

    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private ResultDefinitionView resultDefinitionView;

    @Test
    public void getResultDefinition() throws Exception {
        final UUID resultDefinitionId = randomUUID();
        JsonObject payload = Json.createObjectBuilder()
                .add("originalText", "imp sus")
                .add("orderedDate", "2018-06-01")
                .add("excludedFromResults", true)
                .build();
        when(jsonEnvelope.payloadAsJsonObject()).thenReturn(payload);
        when(resultDefinitionViewBuilder.buildFromKnowledge(any(), any(), any(), anyBoolean())).thenReturn(resultDefinitionView);
        when(enveloper.withMetadataFrom(jsonEnvelope, NAME_RESULT_DEFINITION_RESPONSE)).thenReturn(function);
        when(resultDefinitionViewBuilder.getResultDefinitionIdFromKnowledge(any(), any())).thenReturn(resultDefinitionId.toString());
        when(parsingFacade.retrieveChildResultDefinitionDetail(any(), any())).thenReturn(mock(ChildResultDefinitionDetail.class));

        testObj.getResultDefinition(jsonEnvelope);

        verify(parsingFacade, times(1)).processParts(any(), parsingLocalDateArgumentCaptor.capture());
        verify(parsingFacade, times(1)).lazyLoad(any(JsonEnvelope.class), localDateArgumentCaptor.capture());
        final LocalDate localDate = localDateArgumentCaptor.getValue();
        assertThat(localDate, equalTo(LocalDate.parse("2018-06-01")));
        assertThat(parsingLocalDateArgumentCaptor.getValue(), equalTo(LocalDate.parse("2018-06-01")));
        verify(resultDefinitionView, times(1)).setOriginalText("imp sus");
        verify(resultDefinitionView, times(1)).setOrderedDate("2018-06-01");
        verify(resultDefinitionViewBuilder, times(1)).buildFromKnowledge(any(), any(), any(), anyBoolean());
        verify(objectToJsonObjectConverter, times(1)).convert(any());
        verify(enveloper, times(1)).withMetadataFrom(any(), any());
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
        verify(enveloper, times(1)).withMetadataFrom(any(), any());
    }

    @Test
    public void buildResultDefinitionView() {
        final UUID parentResultDefinitionId = randomUUID();
        final UUID childResultDefinitionId1 = randomUUID();
        final UUID childResultDefinitionId2 = randomUUID();
        final UUID childResultDefinitionId3 = randomUUID();

        ChildResultDefinitionDetail childResultDefinitionDetail = new ChildResultDefinitionDetail(createResultDefinition(parentResultDefinitionId, Arrays.asList(childResultDefinitionId1, childResultDefinitionId2, childResultDefinitionId3)),
                createResultDefinitions(Arrays.asList(childResultDefinitionId1, childResultDefinitionId2, childResultDefinitionId3)));

        when(resultDefinitionViewBuilder.buildFromKnowledge(any(), any(), any(), anyBoolean())).thenReturn(new ResultDefinitionView());
        when(resultDefinitionViewBuilder.getResultDefinitionIdFromKnowledge(any(), any())).thenReturn(parentResultDefinitionId.toString());
        when(parsingFacade.retrieveChildResultDefinitionDetail(any(), any())).thenReturn(childResultDefinitionDetail);

        final ResultDefinitionView resultDefinitionView = testObj.buildResultDefinitionView(
                "imp sus", LocalDate.of(2014, 06, 04), newArrayList(), new Knowledge());
        verify(resultDefinitionViewBuilder, times(1)).buildFromKnowledge(partsCaptor.capture(), knowledgeArgumentCaptor.capture(), childResultDefinitionsCaptor.capture(), excludedFromResults.capture());

        assertThat(resultDefinitionView.getOriginalText()
                , is("imp sus")
        );
        assertThat(resultDefinitionView.getOrderedDate(), is("2014-06-04"));
        assertThat(resultDefinitionView.getResultLineId().length()
                , is(36)
        );
        assertThat(childResultDefinitionsCaptor.getValue().size(), is(3));
        assertThat(childResultDefinitionsCaptor.getValue().get(0).getCode(), is(childResultDefinitionId1.toString()));
        assertThat(childResultDefinitionsCaptor.getValue().get(0).getLabel(), is("lab"));
        assertThat(childResultDefinitionsCaptor.getValue().get(0).getShortCode(), is("sho code"));
        assertThat(childResultDefinitionsCaptor.getValue().get(0).getRuleType(), is("mandatory"));
        assertThat(childResultDefinitionsCaptor.getValue().get(0).getExcludedFromResults(), is(true));


        assertThat(childResultDefinitionsCaptor.getValue().get(1).getCode(), is(childResultDefinitionId2.toString()));
        assertThat(childResultDefinitionsCaptor.getValue().get(1).getLabel(), is("lab"));
        assertThat(childResultDefinitionsCaptor.getValue().get(1).getShortCode(), is("sho code"));
        assertThat(childResultDefinitionsCaptor.getValue().get(1).getRuleType(), is("mandatory"));
        assertThat(childResultDefinitionsCaptor.getValue().get(1).getExcludedFromResults(), is(true));


        assertThat(childResultDefinitionsCaptor.getValue().get(2).getCode(), is(childResultDefinitionId3.toString()));
        assertThat(childResultDefinitionsCaptor.getValue().get(2).getLabel(), is("lab"));
        assertThat(childResultDefinitionsCaptor.getValue().get(2).getShortCode(), is("sho code"));
        assertThat(childResultDefinitionsCaptor.getValue().get(2).getRuleType(), is("mandatory"));
        assertThat(childResultDefinitionsCaptor.getValue().get(2).getExcludedFromResults(), is(true));

    }

    private List<ResultDefinition> createResultDefinitions(final List<UUID> childResultDefinitionIds) {
        return childResultDefinitionIds.stream()
                .map(uuid -> ResultDefinition.builder()
                        .withId(uuid.toString())
                        .withLabel("lab")
                        .withShortCode("sho code")
                        .withExcludedFromResults(true)
                        .build()
                )
                .collect(Collectors.toList());
    }

    private ResultDefinition createResultDefinition(final UUID resultDefinitionId, final List<UUID> childResultDefinitionIds) {

        ResultDefinition resultDefinition = new ResultDefinition();
        resultDefinition.setId(resultDefinitionId.toString());
        resultDefinition.getChildResultDefinitions().addAll(createChildResultDefinitions(childResultDefinitionIds));

        return resultDefinition;
    }

    private List<ChildResultDefinition> createChildResultDefinitions(final List<UUID> childResultDefinitionIds) {
        return childResultDefinitionIds.stream()
                .map(uuid -> createChildResultDefinition(uuid))
                .collect(Collectors.toList());
    }

    private ChildResultDefinition createChildResultDefinition(final UUID childDefinitionId) {
        return new ChildResultDefinition(childDefinitionId, "mandatory");
    }

}
