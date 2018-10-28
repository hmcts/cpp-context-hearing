package uk.gov.moj.cpp.hearing.event;

import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import javax.json.Json;

@RunWith(MockitoJUnitRunner.class)
public class DefendantAttendanceEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private DefendantAttendanceEventProcessor processor;

    @Test
    public void processPublicDefendantAttendanceUpdated() {

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.hearing.defendant-attendance-updated"),
                Json.createObjectBuilder().build());

        processor.publishPublicDefendantAttendanceUpdatedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

    }

}