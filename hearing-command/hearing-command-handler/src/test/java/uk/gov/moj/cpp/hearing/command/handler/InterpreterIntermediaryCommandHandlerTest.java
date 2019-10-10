package uk.gov.moj.cpp.hearing.command.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.hearing.courts.AddInterpreterIntermediary;
import uk.gov.justice.hearing.courts.RemoveInterpreterIntermediary;
import uk.gov.justice.hearing.courts.UpdateInterpreterIntermediary;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryUpdated;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class InterpreterIntermediaryCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            InterpreterIntermediaryAdded.class,
            InterpreterIntermediaryRemoved.class,
            InterpreterIntermediaryUpdated.class
    );
    @InjectMocks
    private InterpreterIntermediaryCommandHandler interpreterIntermediaryCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;

    @Mock
    private HearingAggregateMomento hearingAggregateMomento;
    @Mock
    private AggregateService aggregateService;

    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();


    @Test
    public void addInterpreterIntermediary() throws EventStreamException, IOException {

        final AddInterpreterIntermediary addInterpreterIntermediary = fileResourceObjectMapper.convertFromFile("add-interpreter-intermediary.json", AddInterpreterIntermediary.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.add-interpreter-intermediary", UUID.randomUUID());
        final Envelope<AddInterpreterIntermediary> envelope = envelopeFrom(metadata, addInterpreterIntermediary);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new HearingAggregate());

        interpreterIntermediaryCommandHandler.addInterpreterIntermediary(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals("hearing.interpreter-intermediary-added", actualEventProduced.metadata().name());

    }

    @Test
    public void removeInterpreterIntermediary() throws EventStreamException, IOException {

        final RemoveInterpreterIntermediary removeInterpreterIntermediary = fileResourceObjectMapper.convertFromFile("remove-interpreter-intermediary.json", RemoveInterpreterIntermediary.class);

        final UUID streamId = UUID.fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d");
        final Metadata metadata = metadataFor("hearing.remove-interpreter-intermediary", UUID.randomUUID());
        final Envelope<RemoveInterpreterIntermediary> envelope = envelopeFrom(metadata, removeInterpreterIntermediary);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new HearingAggregate());

        interpreterIntermediaryCommandHandler.removeInterpreterIntermediary(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals("hearing.interpreter-intermediary-removed", actualEventProduced.metadata().name());

    }

    @Test
    public void updateInterpreterIntermediary() throws EventStreamException, IOException {

        final UpdateInterpreterIntermediary updateInterpreterIntermediary = fileResourceObjectMapper.convertFromFile("update-interpreter-intermediary.json", UpdateInterpreterIntermediary.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.update-interpreter-intermediary", UUID.randomUUID());
        final Envelope<UpdateInterpreterIntermediary> envelope = envelopeFrom(metadata, updateInterpreterIntermediary);
        final InterpreterIntermediary defenceCounselTest = mock(InterpreterIntermediary.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();

        InterpreterIntermediaryAdded defenceCounselAdded = mock(InterpreterIntermediaryAdded.class);

        when(defenceCounselTest.getId()).thenReturn(envelope.payload().getInterpreterIntermediary().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);

        when(defenceCounselAdded.getInterpreterIntermediary()).thenReturn(defenceCounselTest);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        hearingAggregate.apply(defenceCounselAdded);

        interpreterIntermediaryCommandHandler.updateInterpreterIntermediary(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals("hearing.interpreter-intermediary-updated", actualEventProduced.metadata().name());

    }

}