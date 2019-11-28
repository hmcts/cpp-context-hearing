package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.*;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_FAILED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;


import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CourtListTimeUpdateRetrieverTest {

    @Mock
    private Requester requester;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Enveloper enveloper;

    @InjectMocks
    private CourtListTimeUpdateRetriever courtListTimeUpdateRetriever;


    @Test
    public void shouldReturnLatestSuccessfulTime() {

        final JsonObject publishStatus = createObjectBuilder()
                .add("publishStatus", EXPORT_SUCCESSFUL.name())
                .add("createdTime", "2016-09-09T08:31:40Z")
                .build();

        final JsonObject jsonObject = createObjectBuilder().add("publishCourtListStatus", publishStatus).build();

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUIDAndName(), jsonObject);
        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);

        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(jsonEnvelope);

        final ZonedDateTime latestCourtListUploadTime = courtListTimeUpdateRetriever.getLatestCourtListUploadTime(jsonEnvelope, "123");

        assertThat(latestCourtListUploadTime, notNullValue());
        assertThat(latestCourtListUploadTime, is(parse("2016-09-09T08:31:40Z")));

    }

    @Test
    public void shouldReturnLatestFailedTime() {

        final JsonObject publishStatus = createObjectBuilder()
                .add("publishStatus", EXPORT_FAILED.name())
                .add("createdTime", "2016-09-09T08:31:40Z")
                .build();

        final JsonObject jsonObject = createObjectBuilder().add("publishCourtListStatus", publishStatus).build();

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUIDAndName(), jsonObject);
        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);

        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(jsonEnvelope);

        final ZonedDateTime latestCourtListUploadTime = courtListTimeUpdateRetriever.getLatestCourtListUploadTime(jsonEnvelope, "123");

        assertThat(latestCourtListUploadTime, notNullValue());
        assertThat(latestCourtListUploadTime, is(parse("2016-09-09T08:31:40Z")));

    }

    @Test
    public void shouldReturnCurrentTimeMinusTenMinutes() {

        final JsonObject publishStatus = createObjectBuilder().build();

        final JsonObject jsonObject = createObjectBuilder().add("publishCourtListStatus", publishStatus).build();

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUIDAndName(), jsonObject);
        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);

        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(jsonEnvelope);

        final ZonedDateTime latestCourtListUploadTime = courtListTimeUpdateRetriever.getLatestCourtListUploadTime(jsonEnvelope, "123");

        assertTrue(latestCourtListUploadTime.isAfter(now().minusMinutes(11l)));
    }
}