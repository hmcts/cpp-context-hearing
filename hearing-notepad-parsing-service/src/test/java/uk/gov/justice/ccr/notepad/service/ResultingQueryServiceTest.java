package uk.gov.justice.ccr.notepad.service;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import java.time.LocalDate;

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

    public static final String REFERENCEDATA_GET_ALL_FIXED_LIST = "referencedata.get-all-fixed-list";
    private static final String REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS = "referencedata.get-all-result-definitions";
    private static final String REFERENCEDATA_GET_ALL_RESULT_WORD_SYNONYMS = "referencedata.get-all-result-word-synonyms";
    private static final String REFERENCEDATA_RESULT_GET_ALL_PROMPT_KEYWORD_SYNONYMS = "referencedata.get-all-prompt-synonyms";
    private static final String REFERENCEDATA_GET_ALL_RESULT_PROMPT_WORD_SYNONYMS = "referencedata.get-all-result-prompt-word-synonyms";
    private final LocalDate hearingDate = LocalDate.parse("2018-05-01");
    @InjectMocks
    private ResultingQueryService resultingQueryService;
    @Spy
    private Enveloper enveloper = EnveloperFactory.createEnveloper();
    @Mock
    private Requester requester;
    @Captor
    private ArgumentCaptor<JsonEnvelope> captor;

    @Test
    public void shouldGetAllResultDefinitionForAGivenDate() {
        //Given
        final JsonEnvelope envelope = createEnvelope(REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS,
                createObjectBuilder().build());

        resultingQueryService.getAllDefinitions(envelope, hearingDate);

        verify(requester).request(captor.capture());

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName(REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }

    @Test
    public void shouldGetAllResultDefinitionSynonyms() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_GET_ALL_RESULT_WORD_SYNONYMS,
                createObjectBuilder()
                        .build());

        resultingQueryService.getAllDefinitionWordSynonyms(command, hearingDate);

        verify(requester).request(captor.capture());
        final JsonEnvelope jsonEnvelope = captor.getValue();
        assertThat(jsonEnvelope, is(jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName(REFERENCEDATA_GET_ALL_RESULT_WORD_SYNONYMS),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }

    @Test
    public void shouldGetAllResultPromptSynonyms() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCEDATA_RESULT_GET_ALL_PROMPT_KEYWORD_SYNONYMS,
                createObjectBuilder()
                        .build());

        resultingQueryService.getAllResultPromptWordSynonyms(command, hearingDate);

        verify(requester).request(captor.capture());

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, is(jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName(REFERENCEDATA_GET_ALL_RESULT_PROMPT_WORD_SYNONYMS),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }

    @Test
    public void shouldGetFixedLists() {
        //Given
        final JsonEnvelope command = createEnvelope(REFERENCEDATA_GET_ALL_FIXED_LIST,
                createObjectBuilder()
                        .build());

        resultingQueryService.getAllFixedLists(command, hearingDate);

        verify(requester).request(captor.capture());

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, is(jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName(REFERENCEDATA_GET_ALL_FIXED_LIST),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }
}
