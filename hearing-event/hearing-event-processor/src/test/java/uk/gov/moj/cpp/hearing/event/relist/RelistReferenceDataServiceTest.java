package uk.gov.moj.cpp.hearing.event.relist;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher;
import uk.gov.moj.cpp.hearing.event.CommandEventTestBase;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RelistReferenceDataServiceTest {

    private static final String REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING = "referencedata.get-result-definition-next-hearing";
    private static final String REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN = "referencedata.get-result-definition-withdrawn";
    public static final String JSON_RESULT_DEFINITION_JSON = "json/result-definitions.json";
    private static final String RESULT_QUERY = "referencedata.query-result-definitions";

    @InjectMocks
    private RelistReferenceDataService relistReferenceDataService;

    @Spy
    private Enveloper enveloper = EnveloperFactory.createEnveloper();

    @Mock
    private Requester requester;

    @Captor
    private ArgumentCaptor<JsonEnvelope> captor;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Test
    public void shouldGetWithdrawnResultDefinitionUuids() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN,
                createObjectBuilder().build());
        when(requester.request(captor.capture())).thenReturn(getArbitraryWithdrawnResult());

        List<UUID> withdrawnUuids = relistReferenceDataService.getWithdrawnResultDefinitionUuids(command, LocalDate.now());

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, is(new JsonEnvelopeMatcher().withMetadataOf(metadata().withName(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN))));
        assertThat(withdrawnUuids.size(), is(1));
    }

    @Test
    public void shouldGetNextHearingResultDefinitions() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING,
                createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(getArbitraryNextHearingResult());

        Map<UUID, NextHearingResultDefinition> nextHearingResult = relistReferenceDataService.getNextHearingResultDefinitions(command, LocalDate.now());

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, is(new JsonEnvelopeMatcher().withMetadataOf(metadata().withName(REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING))));
        assertThat(nextHearingResult.size(), is(1));
    }

    @Test
    public void shouldReturnValidResultDefinition() {
        final JsonObject jsonObjectPayload = CommandEventTestBase.readJson(JSON_RESULT_DEFINITION_JSON, JsonObject.class);
        final Metadata metadata = CommandEventTestBase.metadataFor(RESULT_QUERY, randomUUID().toString());
        final Envelope envelope = Envelope.envelopeFrom(metadata, jsonObjectPayload);

        when(requester.requestAsAdmin(any(), any())).thenReturn(envelope);
        final ResultDefinition results = relistReferenceDataService.getResults(envelope, "DDCH");

        assertThat(results.getId().toString(), is("8c67b30a-418c-11e8-842f-0ed5f89f718b"));
    }


    @Test
    public void shouldReturnEmptyResultDefinition() {
        final JsonObject jsonObjectPayload = Json.createObjectBuilder().add("resultDefinitions", Json.createArrayBuilder().add(Json.createObjectBuilder().build())).build();
        final Metadata metadata = CommandEventTestBase.metadataFor(RESULT_QUERY, randomUUID().toString());
        final Envelope envelope = Envelope.envelopeFrom(metadata, jsonObjectPayload);

        when(requester.requestAsAdmin(any(), any())).thenReturn(envelope);
        final ResultDefinition results = relistReferenceDataService.getResults(envelope, "DDCH");
        assertThat(results.getId(), nullValue());
    }


    private JsonEnvelope getArbitraryWithdrawnResult() {
        return createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN,
                createObjectBuilder()
                        .add("resultDefinitions",
                                Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("id", UUID.randomUUID().toString())
                                                .build())
                                        .build())
                        .build());
    }

    private JsonEnvelope getArbitraryNextHearingResult() {
        return createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN,
                createObjectBuilder()
                        .add("resultDefinitions",
                                Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("id", UUID.randomUUID().toString())
                                                .add("prompts", Json.createArrayBuilder()
                                                        .add(Json.createObjectBuilder()
                                                                .add("id", UUID.randomUUID().toString())
                                                                .add("reference", "FOO")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                        .build());
    }

}
