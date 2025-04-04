package uk.gov.moj.cpp.hearing.event;


import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourtApplicationDeletedProcessorTest {

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderJsonEnvelopeCaptor;

    @InjectMocks
    private CourtApplicationDeletedProcessor courtApplicationDeletedProcessor;

    @Test
    public void shouldHandleCourtApplicationDeletedPublicEvent(){
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        final String hearingId = randomUUID().toString();
        final String applicationId = randomUUID().toString();
        objectBuilder
                .add("hearingId", hearingId)
                .add("applicationId", applicationId);
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.events.court-application-deleted"),
                objectBuilder.build());

        courtApplicationDeletedProcessor.handleCourtApplicationDeletedPublicEvent(event);

        verify(this.sender).send(this.senderJsonEnvelopeCaptor.capture());
        final JsonEnvelope commandEvent = this.senderJsonEnvelopeCaptor.getValue();
        final JsonObject jsonObject = commandEvent.payloadAsJsonObject();
        assertThat(jsonObject.getString("hearingId"), is(hearingId));
        assertThat(jsonObject.getString("applicationId"), is(applicationId));
        assertThat(commandEvent.metadata().name(), is("hearing.command.delete-court-application-hearing"));
    }

}
