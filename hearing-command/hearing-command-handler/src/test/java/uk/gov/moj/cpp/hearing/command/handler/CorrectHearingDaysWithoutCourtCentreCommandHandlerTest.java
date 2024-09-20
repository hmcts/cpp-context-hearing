package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.moj.cpp.hearing.test.FileUtil.givenPayload;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesQueried;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CorrectHearingDaysWithoutCourtCentreCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(OutstandingFinesQueried.class);

    @InjectMocks
    private CorrectHearingDaysWithoutCourtCentreCommandHandler correctHearingDaysWithoutCourtCentreCommandHandler;

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Mock
    private Function<Object, JsonEnvelope> enveloperFunction;

    @Test
    public void shouldCorrectHearingDaysWithoutCourtCenter() throws EventStreamException, IOException {

        final JsonObject json = givenPayload("/hearing.command.correct-hearing-days-without-court-centre.json");
        final UUID streamId = fromString("d7756d94-b5a5-4b61-9b3b-50ad9b3a7d0f");
        final JsonEnvelope commandEnvelope = createEnvelope("hearing.command.correct-hearing-days-without-court-centre", json);

        when(eventSource.getStreamById(streamId)).thenReturn(eventStream);
        when(enveloper.withMetadataFrom(commandEnvelope)).thenReturn(enveloperFunction);

        correctHearingDaysWithoutCourtCentreCommandHandler.correctHearingDaysWithoutCourtCentre(commandEnvelope);

        verify(eventSource).getStreamById(streamId);
        verify(enveloper).withMetadataFrom(commandEnvelope);
    }
}