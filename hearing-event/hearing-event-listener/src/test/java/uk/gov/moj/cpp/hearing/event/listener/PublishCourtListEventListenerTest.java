package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed.publishCourtListExportFailed;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful.publishCourtListExportSuccessful;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListProduced.publishCourtListProduced;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested.publishCourtListRequested;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_PRODUCED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_REQUESTED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_FAILED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListProduced;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested;
import uk.gov.moj.cpp.hearing.repository.CourtList;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishCourtListEventListenerTest {

    private static final UUID COURT_CENTRE_ID = randomUUID();

    @Mock
    private UtcClock clock;

    @Mock
    private CourtListRepository courtListRepository;

    @InjectMocks
    private PublishCourtListEventListener publishCourtListEventListener;

    private final ArgumentCaptor<CourtList> notificationArgumentCaptor = ArgumentCaptor.forClass(CourtList.class);

    @Test
    public void shouldRecordPublishCourtListRequested() {
        final ZonedDateTime createdTime = new UtcClock().now();

        final PublishCourtListRequested publishCourtListRequested = publishCourtListRequested()
                .withCourtCentreId(COURT_CENTRE_ID)
                .withCreatedTime(createdTime)
                .build();

        final Envelope<PublishCourtListRequested> publishCourtListRequestedEnvelope =
                envelopeFrom(metadataWithDefaults(), publishCourtListRequested);

        publishCourtListEventListener.courtListPublishRequested(publishCourtListRequestedEnvelope);

        verify(courtListRepository).save(notificationArgumentCaptor.capture());

        final CourtList courtListArg = notificationArgumentCaptor.getValue();
        assertThat(courtListArg.getCourtListPK().getCourtCentreId(), is(COURT_CENTRE_ID));
        assertThat(courtListArg.getCourtListPK().getPublishStatus(), is(COURT_LIST_REQUESTED));
        assertThat(courtListArg.getLastUpdated(), is(createdTime));
    }

    @Test
    public void shouldRecordPublishCourtListProduced() {
        final ZonedDateTime createdTime = new UtcClock().now();
        final String courtListFileName = "Document Name";
        final UUID courtListFileId = randomUUID();

        final PublishCourtListProduced publishCourtListProduced = publishCourtListProduced()
                .withCourtCentreId(COURT_CENTRE_ID)
                .withCourtListFileId(courtListFileId)
                .withCourtListFileName(courtListFileName)
                .withCreatedTime(createdTime)
                .build();

        final Envelope<PublishCourtListProduced> publishCourtListProducedEvent = envelopeFrom(metadataWithDefaults(), publishCourtListProduced);

        publishCourtListEventListener.courtListPublishProduced(publishCourtListProducedEvent);

        verify(courtListRepository).save(notificationArgumentCaptor.capture());

        final CourtList courtListArg = notificationArgumentCaptor.getValue();
        assertThat(courtListArg.getCourtListPK().getCourtCentreId(), is(COURT_CENTRE_ID));
        assertThat(courtListArg.getCourtListFileId(), is(courtListFileId));
        assertThat(courtListArg.getCourtListPK().getPublishStatus(), is(COURT_LIST_PRODUCED));
        assertThat(courtListArg.getLastUpdated(), is(createdTime));
        assertThat(courtListArg.getCourtListFileName(), is(courtListFileName));
    }

    @Test
    public void shouldRecordPublishCourtListExportFailed() {
        final ZonedDateTime createdTimeStamp = new UtcClock().now();
        final String errorMessage = "some error";
        final String courtListFileName = "Document Name";
        final UUID courtListFileId = randomUUID();

        final PublishCourtListExportFailed publishCourtListExportFailed = publishCourtListExportFailed()
                .withCourtCentreId(COURT_CENTRE_ID)
                .withCourtListFileId(courtListFileId)
                .withCourtListFileName(courtListFileName)
                .withCreatedTime(createdTimeStamp)
                .withErrorMessage(errorMessage)
                .build();

        final Envelope<PublishCourtListExportFailed> publishCourtListExportFailedEvent = envelopeFrom(metadataWithDefaults(), publishCourtListExportFailed);

        publishCourtListEventListener.courtListPublishExportFailed(publishCourtListExportFailedEvent);

        verify(courtListRepository).save(notificationArgumentCaptor.capture());

        final CourtList courtListArg = notificationArgumentCaptor.getValue();
        assertThat(courtListArg.getErrorMessage(), is(errorMessage));
        assertThat(courtListArg.getCourtListPK().getCourtCentreId(), is(COURT_CENTRE_ID));
        assertThat(courtListArg.getCourtListFileId(), is(courtListFileId));
        assertThat(courtListArg.getCourtListPK().getPublishStatus(), is(EXPORT_FAILED));
        assertThat(courtListArg.getLastUpdated(), is(createdTimeStamp));
        assertThat(courtListArg.getCourtListFileName(), is(courtListFileName));
    }

    @Test
    public void shouldRecordPublishCourtListExportSuccessful() {
        final ZonedDateTime createdTime = new UtcClock().now();
        final String courtListFileName = "Document Name";
        final UUID courtListFileId = randomUUID();
        final PublishCourtListExportSuccessful publishCourtListExportSuccessful = publishCourtListExportSuccessful()
                .withCourtCentreId(COURT_CENTRE_ID)
                .withCourtListFileId(courtListFileId)
                .withCourtListFileName(courtListFileName)
                .withCreatedTime(createdTime)
                .build();

        final Envelope<PublishCourtListExportSuccessful> publishCourtListExportSuccessfulEvent = envelopeFrom(metadataWithDefaults(), publishCourtListExportSuccessful);

        publishCourtListEventListener.courtListPublishExportSuccessful(publishCourtListExportSuccessfulEvent);

        verify(courtListRepository).save(notificationArgumentCaptor.capture());

        final CourtList courtListArg = notificationArgumentCaptor.getValue();
        assertThat(courtListArg.getCourtListPK().getCourtCentreId(), is(COURT_CENTRE_ID));
        assertThat(courtListArg.getCourtListFileId(), is(courtListFileId));
        assertThat(courtListArg.getCourtListPK().getPublishStatus(), is(EXPORT_SUCCESSFUL));
        assertThat(courtListArg.getLastUpdated(), is(createdTime));
        assertThat(courtListArg.getCourtListFileName(), is(courtListFileName));
    }
}