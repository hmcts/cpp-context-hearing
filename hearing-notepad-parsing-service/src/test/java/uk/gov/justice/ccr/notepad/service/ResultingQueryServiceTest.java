package uk.gov.justice.ccr.notepad.service;

import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultingQueryServiceTest {

    private static final String REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS = "referencedata.get-all-result-definitions";

    private static final String REFERENCEDATA_RESULT_GET_ALL_DEFINITION_KEYWORD_SYNONYMS = "referencedata.result.get-all-definition-keyword-synonyms";

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
    public void shouldGetAllResultDefinition() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS,
                createObjectBuilder().build());
        when(requester.request(captor.capture())).thenReturn(null);

        resultingQueryService.getAllDefinitions(command);

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName(REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS)));
    }

    @Test
    public void shouldGetAllResultDefinitionSynonyms() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_RESULT_GET_ALL_DEFINITION_KEYWORD_SYNONYMS,
                createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(null);

        resultingQueryService.getAllDefinitionKeywordSynonyms(command);

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName("referencedata.result.get-all-definition-keyword-synonyms")));
    }

    @Test
    public void shouldGetAllResultPromptSynonyms() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_RESULT_GET_ALL_PROMPT_KEYWORD_SYNONYMS,
                createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(null);

        resultingQueryService.getAllPromptKeywordSynonyms(command);

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName("referencedata.result.get-all-prompt-keyword-synonyms")));
    }
}
