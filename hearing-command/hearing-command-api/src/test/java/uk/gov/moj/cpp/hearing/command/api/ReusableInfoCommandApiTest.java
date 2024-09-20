package uk.gov.moj.cpp.hearing.command.api;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReusableInfoCommandApiTest {

    @Spy
    private final Enveloper enveloper = EnveloperFactory.createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeCaptor;

    @InjectMocks
    private ReusableInfoCommandApi reusableInfoApi;

    @Test
    public void shouldPassThroughReusableInfoRequestToCommandHandler() {
        final JsonObject requestPayload = createObjectBuilder().build();

        final JsonEnvelope commandJsonEnvelope = envelopeFrom(metadataWithRandomUUID("hearing.reusable-info"), requestPayload);

        reusableInfoApi.reusableInfo(commandJsonEnvelope);

        verify(sender).send(envelopeCaptor.capture());

        final DefaultEnvelope actualSentEnvelope = envelopeCaptor.getValue();
        assertThat(actualSentEnvelope.metadata().name(), is("hearing.command.reusable-info"));
        assertThat(actualSentEnvelope.payload(), is(requestPayload));

    }
}
