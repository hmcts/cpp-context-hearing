package uk.gov.moj.cpp.hearing.command.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import javax.json.JsonObject;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

@RunWith(MockitoJUnitRunner.class)
public class YouthCourtDefendantsCommandApiTest {

    @Spy
    private final Enveloper enveloper = EnveloperFactory.createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeCaptor;

    @InjectMocks
    private YouthCourtDefendantsCommandApi youthCourtDefendantsCommandApi;

    @Test
    public void shouldPassThroughYouthCourtDefendantsToCommandHandler() {
        final JsonObject requestPayload = createObjectBuilder().build();

        final JsonEnvelope commandJsonEnvelope = envelopeFrom(metadataWithRandomUUID("hearing.youth-court-defendants"), requestPayload);

        youthCourtDefendantsCommandApi.createYouthCourtDefendantsForHearing(commandJsonEnvelope);

        verify(sender).send(envelopeCaptor.capture());

        final DefaultEnvelope actualSentEnvelope = envelopeCaptor.getValue();
        assertThat(actualSentEnvelope.metadata().name(), is("hearing.command.youth-court-defendants"));
        assertThat(actualSentEnvelope.payload(), is(requestPayload));

    }
}
