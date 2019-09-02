package uk.gov.moj.cpp.hearing.command.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.hearing.courts.AddApplicantCounsel;
import uk.gov.justice.hearing.courts.RemoveApplicantCounsel;
import uk.gov.justice.hearing.courts.UpdateApplicantCounsel;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;
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
public class ApplicantCounselCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            ApplicantCounselAdded.class,
            ApplicantCounselRemoved.class,
            ApplicantCounselUpdated.class
    );
    @InjectMocks
    private ApplicantCounselCommandHandler applicantCounselCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;

    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();


    @Test
    public void addApplicantCounsel() throws EventStreamException, IOException {

        final AddApplicantCounsel addApplicantCounsel = fileResourceObjectMapper.convertFromFile("add-applicant-counsel.json", AddApplicantCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.add-applicant-counsel", UUID.randomUUID());
        final Envelope<AddApplicantCounsel> envelope = envelopeFrom(metadata, addApplicantCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new HearingAggregate());

        applicantCounselCommandHandler.addApplicantCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals( "hearing.applicant-counsel-added",actualEventProduced.metadata().name());

    }

    @Test
    public void removeApplicantCounsel() throws EventStreamException, IOException {

        final RemoveApplicantCounsel removeApplicantCounsel = fileResourceObjectMapper.convertFromFile("applicant-counsel-removed.json", RemoveApplicantCounsel.class);

        final UUID streamId = UUID.fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d");
        final Metadata metadata = metadataFor("hearing.remove-applicant-counsel", UUID.randomUUID());
        final Envelope<RemoveApplicantCounsel> envelope = envelopeFrom(metadata, removeApplicantCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new HearingAggregate());

        applicantCounselCommandHandler.removeApplicantCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals( "hearing.applicant-counsel-removed",actualEventProduced.metadata().name());

    }

    @Test
    public void updateApplicantCounsel() throws EventStreamException, IOException {

        final UpdateApplicantCounsel updateApplicantCounsel = fileResourceObjectMapper.convertFromFile("update-applicant-counsel.json", UpdateApplicantCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.update-applicant-counsel", UUID.randomUUID());
        final Envelope<UpdateApplicantCounsel> envelope = envelopeFrom(metadata, updateApplicantCounsel);
        final ApplicantCounsel applicantCounsel = mock(ApplicantCounsel.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();

        ApplicantCounselAdded applicantCounselAdded = mock(ApplicantCounselAdded.class);

        when(applicantCounsel.getId()).thenReturn(envelope.payload().getApplicantCounsel().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);

        when(applicantCounselAdded.getApplicantCounsel()).thenReturn(applicantCounsel);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        hearingAggregate.apply(applicantCounselAdded);

        applicantCounselCommandHandler.updateApplicantCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        Assert.assertEquals( "hearing.applicant-counsel-updated",actualEventProduced.metadata().name());

    }
}