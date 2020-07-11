package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AllAlcoholLevelMethods;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlcoholLevelMethodsReferenceDataLoaderTest {

    @Mock
    private Envelope<Object> alcholLevelMethodsEnvelope;

    @Mock
    private AllAlcoholLevelMethods allAlcoholLevelMethods;

    @Mock
    private Requester requester;

    @InjectMocks
    private AlcoholLevelMethodsReferenceDataLoader alcoholLevelMethodsReferenceDataLoader;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeCaptor;

    @Test
    public void shouldGetAllAlcoholLevelMethods() {


        when(requester.request(any(), any())).thenReturn(alcholLevelMethodsEnvelope);
        when(alcholLevelMethodsEnvelope.payload()).thenReturn(allAlcoholLevelMethods);

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(
                metadataWithDefaults().withName("hearing.results-shared"),
                createObjectBuilder()
                        .build()
        );

        alcoholLevelMethodsReferenceDataLoader.getAllAlcoholLevelMethods(jsonEnvelope);

        verify(requester).request(envelopeCaptor.capture(), any());

        assertThat(envelopeCaptor.getValue().metadata().name(), is("referencedata.query.alcohol-level-methods"));
    }
}
