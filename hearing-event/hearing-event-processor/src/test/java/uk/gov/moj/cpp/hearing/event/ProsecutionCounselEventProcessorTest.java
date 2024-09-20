package uk.gov.moj.cpp.hearing.event;

import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProsecutionCounselEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private ProsecutionCounselEventProcessor processor;

    @Test
    public void publishPublicDefenceCounselAddedEvent()
    {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.hearing.prosecution-counsel-added"),
                Json.createObjectBuilder().build());

        processor.publishPublicProsecutionCounselAddedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
    }
}
