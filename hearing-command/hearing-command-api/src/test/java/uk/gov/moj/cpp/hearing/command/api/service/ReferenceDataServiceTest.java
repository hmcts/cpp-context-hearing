package uk.gov.moj.cpp.hearing.command.api.service;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.api.CommandAPITestBase;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class ReferenceDataServiceTest {

    private static final String RESULT_QUERY = "referencedata.query-result-definitions";
    private static final String REF_DATA_HEARING_TYPES_QUERY = "referencedata.query.hearing-types";

    private static final String JSON_RESULT_DEFINITION_JSON = "json/resultDefinitions.json";
    private static final String REF_DATA_HEARING_TYPES_JSON = "json/hearing-types.json";


    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter= new JsonObjectConvertersFactory().objectToJsonObjectConverter();

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

    @Test
    public void shouldReturnTrialTypeIds() {
        final JsonObject jsonObjectPayload = CommandAPITestBase.readJson(REF_DATA_HEARING_TYPES_JSON, JsonObject.class);
        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(REF_DATA_HEARING_TYPES_QUERY), jsonObjectPayload);

        when(requester.request(any())).thenReturn(jsonEnvelope);
        final List<UUID> results = referenceDataService.getTrialHearingTypes(jsonEnvelope);

        assertThat(results.size(), is(2));
        assertThat(results.get(0), is(fromString("06b0c2bf-3f98-46ed-ab7e-56efaf9ecced")));
        assertThat(results.get(1), is(fromString("9cc41e45-b594-4ba6-906e-1a4626b08fed")));
    }


}