package uk.gov.moj.cpp.hearing.event;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.AttendeeDeleted;

public class AttendeeDeletedEventProcessorTest {

    @InjectMocks
    private AttendeeDeletedEventProcessor attendeeDeletedEventProcessor;

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

   
    @Test
    public void shouldRaiseAnPublicEventAtendeeDeleted() {

        final AttendeeDeleted attendeeDeleted = new AttendeeDeleted(randomUUID(), randomUUID(), now());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.events.attendee-deleted"),
                objectToJsonObjectConverter.convert(attendeeDeleted));

        this.attendeeDeletedEventProcessor.onAttendeeDeleted(envelope);

        final ArgumentCaptor<JsonEnvelope> jsonEnvelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(sender).send(jsonEnvelopeCaptor.capture());

        assertThat(jsonEnvelopeCaptor.getValue().metadata().name(), is("public.hearing.events.attendee-deleted"));
        assertThat(jsonEnvelopeCaptor.getValue().payloadAsJsonObject().getString("attendeeId"), is(attendeeDeleted.getAttendeeId().toString()));

    }
}