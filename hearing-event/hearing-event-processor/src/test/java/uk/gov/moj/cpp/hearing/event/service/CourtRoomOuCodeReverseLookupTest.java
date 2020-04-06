package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Random;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CourtRoomOuCodeReverseLookupTest {

    private static final String REQUEST_OU_CODE = "B47GL89";
    private static final String OU_CODES = "ouCodes";
    private static final String OU_COURT_ROOM_CODES = "ouCourtRoomCodes";
    private static final String OU_CODE_1 = "B47GL00";
    private static final String OU_CODE_2 = "B47GL01";
    private static final String OU_CODE_3 = "B47GL02";

    @Mock
    private Requester requester;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @InjectMocks
    private CourtRoomOuCodeReverseLookup target;

    @Mock
    private JsonEnvelope context;

    private Integer courtRoomId;

    @Captor
    private ArgumentCaptor<JsonEnvelope> argumentCaptor;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        when(context.metadata()).thenReturn(Envelope.metadataBuilder().withId(randomUUID()).withName("courtRoomId").build());
        courtRoomId = new Random().nextInt();
    }

    @Test
    public void shouldFindFirstOuCourtRoomCodeThatMatchesFirstFourCharactersOfOuCodeInRequest() {

        final JsonArray ouCourtRoomCodes = createArrayBuilder()
                .add(OU_CODE_1)
                .add(OU_CODE_2)
                .add(OU_CODE_3)
                .build();

        final JsonObject responseBody = createObjectBuilder()
                .add(OU_COURT_ROOM_CODES, ouCourtRoomCodes)
                .build();

        mockReferenceDataResponse(responseBody);

        final String result = target.getcourtRoomOuCode(context, courtRoomId, REQUEST_OU_CODE);

        verifyReferenceDataRequest();
        assertThat(OU_CODE_1, is(result));
    }

    @Test
    public void shouldFindFirstOuCodeThatMatchesFirstFourCharactersOfOuCodeInRequest() {

        final JsonArray ouCodes = createArrayBuilder()
                .add(OU_CODE_1)
                .add(OU_CODE_2)
                .add(OU_CODE_3)
                .build();

        final JsonObject responseBody = createObjectBuilder()
                .add(OU_CODES, ouCodes)
                .build();

        mockReferenceDataResponse(responseBody);

        final String result = target.getcourtRoomOuCode(context, courtRoomId, REQUEST_OU_CODE);

        verifyReferenceDataRequest();
        assertThat(OU_CODE_1, is(result));
    }

    @Test
    public void shouldReturnEmptyStringIfNotFound() {

        final JsonObject responseBody = createObjectBuilder()
                .build();

        mockReferenceDataResponse(responseBody);

        final String result = target.getcourtRoomOuCode(context, courtRoomId, REQUEST_OU_CODE);

        verifyReferenceDataRequest();
        assertThat("", is(result));
    }

    private void verifyReferenceDataRequest() {
        verify(requester).requestAsAdmin(argumentCaptor.capture());
        final JsonEnvelope referenceDataRequest = argumentCaptor.getValue();
        assertThat(referenceDataRequest.metadata().name(), is("referencedata.query.get.police-opt-courtroom-ou-courtroom-code"));
        assertThat(referenceDataRequest.payloadAsJsonObject().getString("courtRoomId"), is(courtRoomId.toString()));
    }

    private void mockReferenceDataResponse(final JsonObject responseBody) {
        final JsonEnvelope resultEnvelope = mock(JsonEnvelope.class);
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(resultEnvelope);
        when(resultEnvelope.payloadAsJsonObject()).thenReturn(responseBody);
    }

}
