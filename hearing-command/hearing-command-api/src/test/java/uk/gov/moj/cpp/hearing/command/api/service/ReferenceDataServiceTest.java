package uk.gov.moj.cpp.hearing.command.api.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.api.CommandAPITestBase;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

    private static final String RESULT_QUERY = "referencedata.query-result-definitions";
    public static final String JSON_RESULT_DEFINITION_JSON = "json/resultDefinitions.json";


    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Mock
    private Requester requester;

    @Test
    public void shouldReturnValidResultDefinition() {
        final JsonObject jsonObjectPayload = CommandAPITestBase.readJson(JSON_RESULT_DEFINITION_JSON, JsonObject.class);
        final Metadata metadata = CommandAPITestBase.metadataFor(RESULT_QUERY, randomUUID().toString());
        final Envelope envelope = Envelope.envelopeFrom(metadata, jsonObjectPayload);

        when(requester.requestAsAdmin(any(), any())).thenReturn(envelope);
        final ResultDefinition results = referenceDataService.getResults(envelope, "DDCH");

        assertThat(results.getId().toString(), is("8c67b30a-418c-11e8-842f-0ed5f89f718b"));
    }


    @Test
    public void shouldReturnEmptyResultDefinition() {
        final JsonObject jsonObjectPayload = Json.createObjectBuilder().add("resultDefinitions", Json.createArrayBuilder().add(Json.createObjectBuilder().build())).build();
        final Metadata metadata = CommandAPITestBase.metadataFor(RESULT_QUERY, randomUUID().toString());
        final Envelope envelope = Envelope.envelopeFrom(metadata, jsonObjectPayload);

        when(requester.requestAsAdmin(any(), any())).thenReturn(envelope);
        final ResultDefinition results = referenceDataService.getResults(envelope, "DDCH");
        assertThat(results.getId(), nullValue());
    }


}