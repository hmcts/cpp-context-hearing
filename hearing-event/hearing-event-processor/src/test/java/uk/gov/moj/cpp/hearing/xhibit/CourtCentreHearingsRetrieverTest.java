package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;

import java.time.ZonedDateTime;
import java.util.Optional;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CourtCentreHearingsRetrieverTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Enveloper enveloper;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private Requester requester;

    @InjectMocks
    private CourtCentreHearingsRetriever courtCentreHearingsRetriever;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldRetrieveHearingData() {

        final String courtCentreId = randomUUID().toString();
        final ZonedDateTime latestCourtListUploadTime = now();
        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);
        final JsonEnvelope currentCourtStatusEnvelope = getCurrentCourtStatusEnvelope();

        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(currentCourtStatusEnvelope);

        final Optional<CurrentCourtStatus> courtStatus = courtCentreHearingsRetriever.getHearingData(courtCentreId, latestCourtListUploadTime, currentCourtStatusEnvelope);

        assertThat(courtStatus.get().getPageName(), is("testPageName"));
    }

    @Test
    public void shouldReturnEmptyHearingData() {

        final String courtCentreId = randomUUID().toString();
        final ZonedDateTime latestCourtListUploadTime = now();
        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);
        final JsonEnvelope currentCourtStatusEnvelope = getEmptyPayloadEnvelope();

        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(currentCourtStatusEnvelope);

        final Optional<CurrentCourtStatus> courtStatus = courtCentreHearingsRetriever.getHearingData(courtCentreId, latestCourtListUploadTime, currentCourtStatusEnvelope);

        assertTrue(!courtStatus.isPresent());
    }

    private JsonEnvelope getCurrentCourtStatusEnvelope() {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(currentCourtStatus().withPageName("testPageName").build());

        return envelopeFrom(
                metadataBuilder().
                        withName("hearing.get-hearings-by-court-centre").
                        withId(randomUUID()),
                jsonObject
        );
    }

    private JsonEnvelope getEmptyPayloadEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("hearing.get-hearings-by-court-centre").
                        withId(randomUUID()),
                createObjectBuilder().build()
        );
    }
}