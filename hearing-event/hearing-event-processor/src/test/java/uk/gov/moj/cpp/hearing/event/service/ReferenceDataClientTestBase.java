package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;

public class ReferenceDataClientTestBase {

    @Mock
    protected Requester requester;

    protected JsonEnvelope context;

    @Spy
    protected JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    protected ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Captor
    protected ArgumentCaptor<Envelope<JsonValue>> orgUnitQueryEnvelopeCaptor;

    @Captor
    protected ArgumentCaptor<JsonEnvelope> enforcementAreaQueryEnvelopeCaptor;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        createEnveloperWithEvents();
        context = envelopeFrom(
                metadataWithDefaults().withName("name"), // this value does not matter as the requester will set the name for the call it's issuing
                createObjectBuilder().build()
        );
    }


    protected void mockQuery(final ArgumentCaptor<Envelope<JsonValue>> envelopeCaptor, final Object result) {
        final JsonEnvelope resultEnvelope = mock(JsonEnvelope.class);
        JsonObject organisationalUnitJson = objectToJsonObjectConverter.convert(result);
        when(resultEnvelope.payloadAsJsonObject()).thenReturn(organisationalUnitJson);
        when(requester.request(envelopeCaptor.capture())).thenReturn(resultEnvelope);
    }

    protected void mockAdminQuery(final ArgumentCaptor<JsonEnvelope> envelopeCaptor, final Object result) {
        final JsonEnvelope resultEnvelope = mock(JsonEnvelope.class);
        JsonObject organisationalUnitJson = objectToJsonObjectConverter.convert(result);
        when(resultEnvelope.payloadAsJsonObject()).thenReturn(organisationalUnitJson);
        when(requester.requestAsAdmin(envelopeCaptor.capture())).thenReturn(resultEnvelope);
    }


}
