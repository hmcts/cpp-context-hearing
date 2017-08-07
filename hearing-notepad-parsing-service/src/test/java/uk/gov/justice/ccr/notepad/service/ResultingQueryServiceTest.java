package uk.gov.justice.ccr.notepad.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher;

import javax.json.Json;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;

@RunWith(MockitoJUnitRunner.class)
public class ResultingQueryServiceTest {

    private static final String REFERENCEDATA_RESULT_GET_ALL_DEFINITIONS = "referencedata.result.get-all-definitions";

    private static final String REFERENCEDATA_RESULT_GET_ALL_DEFINITION_KEYWORD_SYNONYMS = "referencedata.result.get-all-definition-keyword-synonyms";

    private static final String REFERENCEDATA_RESULT_GET_ALL_PROMPTS = "referencedata.result.get-all-prompts";

    private static final String REFERENCEDATA_RESULT_GET_ALL_PROMPT_KEYWORD_SYNONYMS = "referencedata.result.get-all-prompt-keyword-synonyms";

    @InjectMocks
    private ResultingQueryService resultingQueryService;

    @Spy
    private Enveloper enveloper = EnveloperFactory.createEnveloper();

    @Mock
    private Requester requester;

    @Captor
    private ArgumentCaptor<JsonEnvelope> captor;

    @Test
    public void shouldGetAllResultDefinition() throws Exception {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_RESULT_GET_ALL_DEFINITIONS,
                Json.createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(null);

        resultingQueryService.getAllDefinitions(command);

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName("referencedata.result.get-all-definitions")));
    }

    @Test
    public void shouldGetAllResultDefinitionSynonyms() throws Exception {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_RESULT_GET_ALL_DEFINITION_KEYWORD_SYNONYMS,
                Json.createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(null);

        resultingQueryService.getAllDefinitionKeywordSynonyms(command);

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName("referencedata.result.get-all-definition-keyword-synonyms")));
    }

    @Test
    public void shouldGetAllResultPrompts() throws Exception {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_RESULT_GET_ALL_PROMPTS,
                Json.createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(null);

        resultingQueryService.getAllPrompts(command);

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName("referencedata.result.get-all-prompts")));
    }

    @Test
    public void shouldGetAllResultPromptSynonyms() throws Exception {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_RESULT_GET_ALL_PROMPT_KEYWORD_SYNONYMS,
                Json.createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(null);

        resultingQueryService.getAllPromptKeywordSynonyms(command);

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName("referencedata.result.get-all-prompt-keyword-synonyms")));
    }
}