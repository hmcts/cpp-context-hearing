package uk.gov.moj.cpp.hearing.command.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.hearing.courts.AddRespondentCounsel;
import uk.gov.justice.hearing.courts.RemoveRespondentCounsel;
import uk.gov.justice.hearing.courts.UpdateRespondentCounsel;
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
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselUpdated;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class RespondentCounselCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            RespondentCounselAdded.class,
            RespondentCounselRemoved.class,
            RespondentCounselUpdated.class
    );

    @InjectMocks
    private RespondentCounselCommandHandler respondentCounselCommandHandler;
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
    public void addRespondentCounsel() throws EventStreamException, IOException {

        final AddRespondentCounsel addRespondentCounsel = fileResourceObjectMapper.convertFromFile("add-respondent-counsel.json", AddRespondentCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.add-respondent-counsel", UUID.randomUUID());
        final Envelope<AddRespondentCounsel> envelope = envelopeFrom(metadata, addRespondentCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(new HearingAggregate());

        respondentCounselCommandHandler.addRespondentCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.respondent-counsel-added",actualEventProduced.metadata().name());

    }

    @Test
    public void removeRespondentCounsel() throws EventStreamException, IOException {

        final RemoveRespondentCounsel removeRespondentCounsel = fileResourceObjectMapper.convertFromFile("remove-respondent-counsel.json", RemoveRespondentCounsel.class);

        final UUID streamId = UUID.fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d");
        final Metadata metadata = metadataFor("hearing.remove-respondent-counsel", UUID.randomUUID());
        final Envelope<RemoveRespondentCounsel> envelope = envelopeFrom(metadata, removeRespondentCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new HearingAggregate());

        respondentCounselCommandHandler.removeRespondentCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.respondent-counsel-removed",actualEventProduced.metadata().name());

    }

    @Test
    public void updateRespondentCounsel() throws EventStreamException, IOException {

        final UpdateRespondentCounsel updateRespondentCounsel = fileResourceObjectMapper.convertFromFile("update-respondent-counsel.json", UpdateRespondentCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.update-respondent-counsel", UUID.randomUUID());
        final Envelope<UpdateRespondentCounsel> envelope = envelopeFrom(metadata, updateRespondentCounsel);
        final RespondentCounsel respondentCounselTest = mock(RespondentCounsel.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();

        RespondentCounselAdded respondentCounselAdded = mock(RespondentCounselAdded.class);

        when(respondentCounselTest.getId()).thenReturn(envelope.payload().getRespondentCounsel().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);

        when(respondentCounselAdded.getRespondentCounsel()).thenReturn(respondentCounselTest);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        hearingAggregate.apply(respondentCounselAdded);

        respondentCounselCommandHandler.updateRespondentCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.respondent-counsel-updated",actualEventProduced.metadata().name());

    }
}