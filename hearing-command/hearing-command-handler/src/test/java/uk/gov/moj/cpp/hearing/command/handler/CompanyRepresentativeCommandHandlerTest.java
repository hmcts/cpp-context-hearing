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

import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.hearing.courts.AddCompanyRepresentative;
import uk.gov.justice.hearing.courts.RemoveCompanyRepresentative;
import uk.gov.justice.hearing.courts.UpdateCompanyRepresentative;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeAdded;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeUpdated;
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
public class CompanyRepresentativeCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CompanyRepresentativeAdded.class,
            CompanyRepresentativeUpdated.class,
            CompanyRepresentativeRemoved.class,
            CompanyRepresentativeChangeIgnored.class
    );
    private final FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();
    @InjectMocks
    private CompanyRepresentativeCommandHandler companyRepresentativeCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;

    @Test
    public void shouldAddCompanyRepresentativeIfTheCompanyRepresentativeIsNotAvailableInHearing() throws EventStreamException, IOException {

        final AddCompanyRepresentative addCompanyRepresentative = fileResourceObjectMapper.convertFromFile("add-company-representative.json", AddCompanyRepresentative.class);
        final UUID streamId = UUID.fromString("0b9c2ae0-c14a-4892-8f45-4a5fdaa2baf5");
        final Metadata metadata = metadataFor("hearing.command.add-company-representative", UUID.randomUUID());
        final Envelope<AddCompanyRepresentative> envelope = envelopeFrom(metadata, addCompanyRepresentative);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(new HearingAggregate());

        companyRepresentativeCommandHandler.addCompanyRepresentative(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals("hearing.company-representative-added", actualEventProduced.metadata().name());
    }

    @Test
    public void shouldNotAddCompanyRepresentativeIfTheCompanyRepresentativeIsAlreadyAvailableInHearing() throws EventStreamException, IOException {

        final AddCompanyRepresentative addCompanyRepresentative = fileResourceObjectMapper.convertFromFile("add-company-representative.json", AddCompanyRepresentative.class);

        final UUID streamId = UUID.fromString("0b9c2ae0-c14a-4892-8f45-4a5fdaa2baf5");
        final Metadata metadata = metadataFor("hearing.command.add-company-representative", UUID.randomUUID());
        final Envelope<AddCompanyRepresentative> envelope = envelopeFrom(metadata, addCompanyRepresentative);
        final CompanyRepresentative companyRepresentative = mock(CompanyRepresentative.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final CompanyRepresentativeAdded companyRepresentativeAdded = mock(CompanyRepresentativeAdded.class);

        when(companyRepresentative.getId()).thenReturn(envelope.payload().getCompanyRepresentative().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(companyRepresentativeAdded.getCompanyRepresentative()).thenReturn(companyRepresentative);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(hearingAggregate);

        hearingAggregate.apply(companyRepresentativeAdded);

        companyRepresentativeCommandHandler.addCompanyRepresentative(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals("hearing.company-representative-change-ignored", actualEventProduced.metadata().name());
    }

    @Test
    public void shouldUpdateCompanyRepresentativeOnlyIfCompanyRepresentativeIsAvailableInHearing() throws EventStreamException, IOException {

        final UpdateCompanyRepresentative updateCompanyRepresentative = fileResourceObjectMapper.convertFromFile("update-company-representative.json", UpdateCompanyRepresentative.class);

        final UUID streamId = UUID.fromString("0b9c2ae0-c14a-4892-8f45-4a5fdaa2baf5");
        final Metadata metadata = metadataFor("hearing.command.update-company-representative", UUID.randomUUID());
        final Envelope<UpdateCompanyRepresentative> envelope = envelopeFrom(metadata, updateCompanyRepresentative);
        final CompanyRepresentative companyRepresentative = mock(CompanyRepresentative.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final CompanyRepresentativeAdded companyRepresentativeAdded = mock(CompanyRepresentativeAdded.class);

        when(companyRepresentative.getId()).thenReturn(envelope.payload().getCompanyRepresentative().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(companyRepresentativeAdded.getCompanyRepresentative()).thenReturn(companyRepresentative);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(hearingAggregate);

        hearingAggregate.apply(companyRepresentativeAdded);

        companyRepresentativeCommandHandler.updateCompanyRepresentative(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals("hearing.company-representative-updated", actualEventProduced.metadata().name());
    }

    @Test
    public void shouldNotUpdateCompanyRepresentativeIfCompanyRepresentativeIsNotAvailableInHearing() throws EventStreamException, IOException {

        final UpdateCompanyRepresentative updateCompanyRepresentative = fileResourceObjectMapper.convertFromFile("update-company-representative.json", UpdateCompanyRepresentative.class);
        final UUID streamId = UUID.fromString("0b9c2ae0-c14a-4892-8f45-4a5fdaa2baf5");
        final Metadata metadata = metadataFor("hearing.command.update-company-representative", UUID.randomUUID());
        final Envelope<UpdateCompanyRepresentative> envelope = envelopeFrom(metadata, updateCompanyRepresentative);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(new HearingAggregate());

        companyRepresentativeCommandHandler.updateCompanyRepresentative(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals("hearing.company-representative-change-ignored", actualEventProduced.metadata().name());
    }

    @Test
    public void shouldRemoveCompanyRepresentativeIfCompanyRepresentativeIsAvailableInHearing() throws EventStreamException, IOException {

        final RemoveCompanyRepresentative removeCompanyRepresentative = fileResourceObjectMapper.convertFromFile("remove-company-representative.json", RemoveCompanyRepresentative.class);
        final UUID streamId = UUID.fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d");
        final Metadata metadata = metadataFor("hearing.command.remove-company-representative", UUID.randomUUID());
        final Envelope<RemoveCompanyRepresentative> envelope = envelopeFrom(metadata, removeCompanyRepresentative);
        final CompanyRepresentative companyRepresentative = mock(CompanyRepresentative.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final CompanyRepresentativeAdded companyRepresentativeAdded = mock(CompanyRepresentativeAdded.class);

        when(companyRepresentative.getId()).thenReturn(envelope.payload().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(companyRepresentativeAdded.getCompanyRepresentative()).thenReturn(companyRepresentative);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(hearingAggregate);

        hearingAggregate.apply(companyRepresentativeAdded);

        companyRepresentativeCommandHandler.removeCompanyRepresentative(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals("hearing.company-representative-removed", actualEventProduced.metadata().name());
    }

    @Test
    public void shouldNotRemoveCompanyRepresentativeIfCompanyRepresentativeIsNotAvailableInHearing() throws EventStreamException, IOException {
        final RemoveCompanyRepresentative removeCompanyRepresentative = fileResourceObjectMapper.convertFromFile("remove-company-representative.json", RemoveCompanyRepresentative.class);
        final UUID streamId = UUID.fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d");
        final Metadata metadata = metadataFor("hearing.command.remove-company-representative", UUID.randomUUID());
        final Envelope<RemoveCompanyRepresentative> envelope = envelopeFrom(metadata, removeCompanyRepresentative);
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(new HearingAggregate());

        companyRepresentativeCommandHandler.removeCompanyRepresentative(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals("hearing.company-representative-change-ignored", actualEventProduced.metadata().name());
    }
}