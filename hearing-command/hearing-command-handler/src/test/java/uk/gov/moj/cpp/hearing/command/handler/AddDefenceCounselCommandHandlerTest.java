package uk.gov.moj.cpp.hearing.command.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.RemoveDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.justice.core.courts.DefenceCounsel;
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
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;
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
public class AddDefenceCounselCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefenceCounselAdded.class,
            DefenceCounselRemoved.class,
            DefenceCounselUpdated.class
    );
    @InjectMocks
    private AddDefenceCounselCommandHandler defenceCounselCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;

    @Mock
    private HearingAggregateMomento hearingAggregateMomento ;
    @Mock
    private AggregateService aggregateService;

    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();


    @Test
    public void addDefenceCounsel() throws EventStreamException, IOException {

        final AddDefenceCounsel addDefenceCounsel = fileResourceObjectMapper.convertFromFile("add-defence-counsel.json", AddDefenceCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.add-defence-counsel", UUID.randomUUID());
        final Envelope<AddDefenceCounsel> envelope = envelopeFrom(metadata, addDefenceCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate());

        defenceCounselCommandHandler.addDefenceCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals( "hearing.defence-counsel-added",actualEventProduced.metadata().name());

    }

    @Test
    public void removeDefenceCounsel() throws EventStreamException, IOException {

        final RemoveDefenceCounsel removeDefenceCounsel = fileResourceObjectMapper.convertFromFile("remove-defence-counsel.json", RemoveDefenceCounsel.class);

        final UUID streamId = UUID.fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d");
        final Metadata metadata = metadataFor("hearing.remove-defence-counsel", UUID.randomUUID());
        final Envelope<RemoveDefenceCounsel> envelope = envelopeFrom(metadata, removeDefenceCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate());

        defenceCounselCommandHandler.removeDefenceCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals( "hearing.defence-counsel-removed",actualEventProduced.metadata().name());

    }

    @Test
    public void updateDefenceCounsel() throws EventStreamException, IOException {

        final UpdateDefenceCounsel updateDefenceCounsel = fileResourceObjectMapper.convertFromFile("update-defence-counsel.json", UpdateDefenceCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.update-defence-counsel", UUID.randomUUID());
        final Envelope<UpdateDefenceCounsel> envelope = envelopeFrom(metadata, updateDefenceCounsel);
        final DefenceCounsel defenceCounselTest = mock(DefenceCounsel.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();

        DefenceCounselAdded defenceCounselAdded = mock(DefenceCounselAdded.class);

        when(defenceCounselTest.getId()).thenReturn(envelope.payload().getDefenceCounsel().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);

        when(defenceCounselAdded.getDefenceCounsel()).thenReturn(defenceCounselTest);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        hearingAggregate.apply(defenceCounselAdded);

        defenceCounselCommandHandler.updateDefenceCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals( "hearing.defence-counsel-updated",actualEventProduced.metadata().name());

    }
}