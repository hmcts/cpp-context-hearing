package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.verdicttype.AllVerdictTypes;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VerdictTypeReferenceDataLoaderTest {

    @Mock
    private Envelope<Object> verdictTypesEnvelope;

    @Mock
    private AllVerdictTypes allVerdictTypes;

    @Mock
    private Requester requester;

    @InjectMocks
    private VerdictTypesReferenceDataLoader verdictTypesReferenceDataLoader;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeCaptor;

    @Test
    public void shouldGetAllVerdictTypes() {


        when(requester.request(any(), any())).thenReturn(verdictTypesEnvelope);
        when(verdictTypesEnvelope.payload()).thenReturn(allVerdictTypes);

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(
                metadataWithDefaults().withName("hearing.results-shared"),
                createObjectBuilder()
                        .build()
        );

        verdictTypesReferenceDataLoader.getAllVerdictTypes(jsonEnvelope);

        verify(requester).request(envelopeCaptor.capture(), any());

        assertThat(envelopeCaptor.getValue().metadata().name(), is("referencedata.query.verdict-types"));
    }
}
