package uk.gov.moj.cpp.hearing.command.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionTimeCommandApiTest {
    @Mock
    private Sender sender;

    @InjectMocks
    private SessionTimeCommandApi commandApi;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeCaptor;

    @Test
    public void shouldRecordSessionTime() {
        final JsonEnvelope envelope = envelope()
                .with(metadataWithDefaults()
                        .withName("hearing.command.record-session-time"))
                .build();

        commandApi.recordSessionTime(envelope);

        verify(sender).send(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().metadata().name(), is("hearing.command.record-session-time"));
    }
}