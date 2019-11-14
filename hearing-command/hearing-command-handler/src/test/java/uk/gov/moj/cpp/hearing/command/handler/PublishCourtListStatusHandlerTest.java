package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed.publishCourtListExportFailed;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful.publishCourtListExportSuccessful;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListProduced.publishCourtListProduced;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested.publishCourtListRequested;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.CourtListAggregate;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListProduced;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishCourtListStatusHandlerTest {

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private CourtListAggregate courtListAggregate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectToObjectConverter();
    private ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(objectMapper);

    @InjectMocks
    PublishCourtListStatusHandler publishCourtListStatusHandler;

    @Test
    public void hearingCommandHandlerShouldTriggerExportFailedForPublishEvent() throws Exception {
        final UUID courtCenterId = UUID.randomUUID();
        final UUID courtListFileId = UUID.randomUUID();
        final String createdTime = "2016-09-09T08:31:40Z";
        final String errorMessage = "Unable to download the file from file service";
        final String courtListFileName = randomAlphanumeric(30).toString();
        when(eventSource.getStreamById(courtCenterId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CourtListAggregate.class)).thenReturn(courtListAggregate);
        when(courtListAggregate.recordCourtListExportFailed(any(UUID.class), any(UUID.class), any(String.class),
                any(ZonedDateTime.class), eq(errorMessage)))
                .thenReturn(Stream.of(publishCourtListExportFailed().build()));

        final String jsonString = givenPayload("/hearing.command.record-court-list-export-failed.json").toString()
                .replace("COURT_CENTRE_ID", courtCenterId.toString())
                .replace("COURT_LIST_FILE_ID", courtListFileId.toString())
                .replace("COURT_LIST_FILE_NAME", courtListFileName)
                .replace("ERROR_MESSAGE", errorMessage)
                .replace("CREATED_TIME", createdTime.toString());
        try {
            final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
            final JsonEnvelope commandEnvelope = createEnvelope("hearing.command.record-court-list-export-failed", jsonReader.readObject());
            publishCourtListStatusHandler.recordCourtListExportFailed(commandEnvelope);
            verify(courtListAggregate).recordCourtListExportFailed(any(UUID.class), any(UUID.class), any(String.class), any(ZonedDateTime.class), eq(errorMessage));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void hearingCommandHandlerShouldTriggerExportSuccessfulForPublishEvent() throws Exception {
        final UUID courtCenterId = UUID.randomUUID();
        final UUID courtListFileId = UUID.randomUUID();
        final String createdTime = "2016-09-09T08:31:40Z";
        final String courtListFileName = randomAlphanumeric(30).toString();
        when(eventSource.getStreamById(courtCenterId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CourtListAggregate.class)).thenReturn(courtListAggregate);
        when(courtListAggregate.recordCourtListExportSuccessful(any(UUID.class), any(UUID.class), any(String.class),
                any(ZonedDateTime.class)))
                .thenReturn(Stream.of(publishCourtListExportSuccessful().build()));
        final String jsonString = givenPayload("/hearing.command.record-court-list-export-successful.json").toString()
                .replace("COURT_CENTRE_ID", courtCenterId.toString())
                .replace("COURT_LIST_FILE_ID", courtListFileId.toString())
                .replace("COURT_LIST_FILE_NAME", courtListFileName)
                .replace("CREATED_TIME", createdTime.toString());

        try {
            final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
            final JsonEnvelope commandEnvelope = createEnvelope("hearing.command.record-court-list-export-successful", jsonReader.readObject());
            publishCourtListStatusHandler.recordCourtListExportSuccessful(commandEnvelope);
            verify(courtListAggregate).recordCourtListExportSuccessful(any(UUID.class), any(UUID.class), any(String.class), any(ZonedDateTime.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldCreatePublishCourtListRequestedEvent() throws Exception {
        final UUID courtCentreId = randomUUID();

        when(eventSource.getStreamById(courtCentreId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CourtListAggregate.class)).thenReturn(courtListAggregate);
        when(courtListAggregate.recordCourtListRequested(any(UUID.class), any(ZonedDateTime.class)))
                .thenReturn(Stream.of(publishCourtListRequested().build()));

        final String jsonString = givenPayload("/hearing.command.publish-court-list.json").toString()
                .replace("COURT_CENTRE_ID", courtCentreId.toString());

        final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        final JsonEnvelope commandEnvelope = createEnvelope("hearing.command.publish-court-list", jsonReader.readObject());
        publishCourtListStatusHandler.publishCourtList(commandEnvelope);
        verify(courtListAggregate).recordCourtListRequested(any(UUID.class), any(ZonedDateTime.class));
    }

    @Test
    public void shouldCreatePublishCourtListProducedEvent() throws Exception {
        final UUID courtCentreId = randomUUID();
        final PublishCourtListProduced publishCourtListProduced = publishCourtListProduced().build();

        when(eventSource.getStreamById(courtCentreId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CourtListAggregate.class)).thenReturn(courtListAggregate);
        when(courtListAggregate.recordCourtListProduced(any(UUID.class), any(UUID.class), any(String.class), any(ZonedDateTime.class)))
                .thenReturn(Stream.of(publishCourtListProduced));

        final String jsonString = givenPayload("/hearing.command.record-court-list-produced.json").toString()
                .replace("COURT_CENTRE_ID", courtCentreId.toString());

        final JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        final JsonEnvelope commandEnvelope = createEnvelope("hearing.command.record-court-list-produced", jsonReader.readObject());

        publishCourtListStatusHandler.recordCourtListProduced(commandEnvelope);

        verify(courtListAggregate).recordCourtListProduced(any(UUID.class), any(UUID.class), any(String.class), any(ZonedDateTime.class));
    }


    private static JsonObject givenPayload(final String filePath) {
        try (final InputStream inputStream = PublishCourtListStatusHandlerTest.class.getResourceAsStream(filePath)) {
            final JsonReader jsonReader = createReader(inputStream);
            return jsonReader.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}