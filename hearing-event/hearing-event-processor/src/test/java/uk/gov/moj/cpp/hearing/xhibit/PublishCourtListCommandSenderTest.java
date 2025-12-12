package uk.gov.moj.cpp.hearing.xhibit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultJsonMetadata;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishCourtListCommandSenderTest {

    @Spy
    private UtcClock utcClock;

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<Envelope> envelopeArgumentCaptor;

    @InjectMocks
    private PublishCourtListCommandSender publishCourtListCommandSender;

    @Test
    public void shouldRecordHearingEventsExportSuccessful() {
        final String courtCentreId = UUID.randomUUID().toString();
        final String courtListFileName = "courtListFileName";

        publishCourtListCommandSender.recordCourtListExportSuccessful(courtCentreId,courtListFileName);
        verify(sender).send(envelopeArgumentCaptor.capture());

        final DefaultJsonMetadata metaData = (DefaultJsonMetadata) envelopeArgumentCaptor.getValue().metadata();
        assertThat(metaData.name(), is("hearing.command.record-court-list-export-successful"));

        final JsonObject payload = (JsonObject) envelopeArgumentCaptor.getValue().payload();
        assertThat(payload.getString("courtCentreId"), is(courtCentreId));
        assertThat(payload.getString("courtListFileName"), is(courtListFileName));
        assertThat(payload.getString("createdTime"), is(notNullValue()));
    }

    @Test
    public void shouldRecordHearingEventsExportFailed() {
        final String courtCentreId = UUID.randomUUID().toString();
        final String courtListFileName = "courtListFileName";
        final String errorMessage = "errorMessage";

        publishCourtListCommandSender.recordCourtListExportFailed(courtCentreId,courtListFileName,errorMessage);
        verify(sender).send(any(JsonEnvelope.class));
        verify(sender).send(envelopeArgumentCaptor.capture());

        final DefaultJsonMetadata metaData = (DefaultJsonMetadata) envelopeArgumentCaptor.getValue().metadata();
        assertThat(metaData.name(), is("hearing.command.record-court-list-export-failed"));

        final JsonObject payload = (JsonObject) envelopeArgumentCaptor.getValue().payload();
        assertThat(payload.getString("courtCentreId"), is(courtCentreId));
        assertThat(payload.getString("courtListFileName"), is(courtListFileName));
        assertThat(payload.getString("createdTime"), is(notNullValue()));
    }

    @Test
    public void shouldRequestToPublishHearingEvents() {
        final String courtCentreId = UUID.randomUUID().toString();
        final String createdTime = ZonedDateTime.now().toString();

        publishCourtListCommandSender.requestToPublishHearingEvents(courtCentreId,createdTime);
        verify(sender).send(any(JsonEnvelope.class));
        verify(sender).send(envelopeArgumentCaptor.capture());

        final DefaultJsonMetadata metaData = (DefaultJsonMetadata) envelopeArgumentCaptor.getValue().metadata();
        assertThat(metaData.name(), is("hearing.command.publish-court-list"));

        final JsonObject payload = (JsonObject) envelopeArgumentCaptor.getValue().payload();
        assertThat(payload.getString("courtCentreId"), is(courtCentreId));
        assertThat(payload.getString("createdTime"), is(notNullValue()));
    }
}