package uk.gov.moj.cpp.hearing.event.service;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.moj.cpp.hearing.event.service.ProsecutorDataLoader.GET_PROSECUTOR_BY_ID;

import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProsecutorDataLoaderTest {

    @Mock
    private Envelope<Object> prosecutorEnvelope;

    @Mock
    private Prosecutor prosecutor;

    @Mock
    private Requester requester;

    @InjectMocks
    private ProsecutorDataLoader target;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeCaptor;


    @Test
    public void testGetProsecutingAuthorityById() {


        when(requester.request(any(), any())).thenReturn(prosecutorEnvelope);
        when(prosecutorEnvelope.payload()).thenReturn(prosecutor);

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(
                metadataWithDefaults().withName(GET_PROSECUTOR_BY_ID),
                createObjectBuilder()
                        .build()
        );

        final UUID prosecutorId = randomUUID();
        target.getProsecutorById(jsonEnvelope,prosecutorId);

        verify(requester).request(envelopeCaptor.capture(), any());

        assertThat(envelopeCaptor.getValue().metadata().name(), is(GET_PROSECUTOR_BY_ID));

        assertThat(envelopeCaptor.getValue().metadata(), metadata().withName(GET_PROSECUTOR_BY_ID));
        assertThat(envelopeCaptor.getValue().payload(), payload().isJson(withJsonPath("$.id", is(prosecutorId.toString()))));
    }
}
