package uk.gov.moj.cpp.hearing.command.handler;

import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingsPleaAggregate;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProgressionCommandHandlerTest {

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private ProgressionCommandHandler progressionCommandHandler;

    @Mock
    private JsonEnvelope command;

    @Mock
    private HearingsPleaAggregate hearingsPleaAggregate;

    @Mock
    private JsonObject payload;

    private UUID caseId = UUID.randomUUID();

    @Before
    public void setup() {
        when(this.eventSource.getStreamById(caseId)).thenReturn(this.eventStream);
        when(this.aggregateService.get(this.eventStream, HearingsPleaAggregate.class)).thenReturn(hearingsPleaAggregate);
    }

    @Mock
    private Stream<Object> events;

    @Mock
    private Stream<JsonEnvelope> mappedStream;

    @Mock
    private Enveloper enveloper;

    @Test
    public void testRecordSendingSheetComplete() throws Exception {
           Mockito.when( command.payloadAsJsonObject()  ).thenReturn(payload);
           Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).build();
           SendingSheetCompleted sendingSheetCompleted = (new SendingSheetCompleted.Builder()).withHearing(hearing).build();
           Mockito.when( jsonObjectToObjectConverter.convert(payload, SendingSheetCompleted.class)).thenReturn(sendingSheetCompleted);
           Mockito.when( hearingsPleaAggregate.recordSendingSheetComplete(sendingSheetCompleted) ).thenReturn(events);
           //TODO test the mapping function factory call
           Mockito.when (events.map(Mockito.any(Function.class))).thenReturn(mappedStream);
           progressionCommandHandler.recordSendingSheetComplete(command);
           //check that the mappedStream gets appended to the events
           ArgumentCaptor<Stream> mappedStreamArgCapture = ArgumentCaptor.forClass(Stream.class);
           Mockito.verify(eventStream).append(mappedStreamArgCapture.capture());
           Assert.assertEquals(mappedStreamArgCapture.getValue(), mappedStream);
    }

    @Test
    public void testRecordMagsCourtHearings() throws Exception {
        Mockito.when( command.payloadAsJsonObject()  ).thenReturn(payload);
        Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).build();
        RecordMagsCourtHearingCommand recordMagsCourtHearingCommand = new RecordMagsCourtHearingCommand(hearing);
        Mockito.when( jsonObjectToObjectConverter.convert(payload, RecordMagsCourtHearingCommand.class)).thenReturn(recordMagsCourtHearingCommand);
        Mockito.when( hearingsPleaAggregate.recordMagsCourtHearing(recordMagsCourtHearingCommand.getHearing()) ).thenReturn(events);
        //TODO test the mapping function factory call
        Mockito.when (events.map(Mockito.any(Function.class))).thenReturn(mappedStream);
        progressionCommandHandler.recordMagsCourtHearing(command);
        //check that the mappedStream gets appended to the events
        ArgumentCaptor<Stream> mappedStreamArgCapture = ArgumentCaptor.forClass(Stream.class);
        Mockito.verify(eventStream).append(mappedStreamArgCapture.capture());
        Assert.assertEquals(mappedStreamArgCapture.getValue(), mappedStream);
    }

}
