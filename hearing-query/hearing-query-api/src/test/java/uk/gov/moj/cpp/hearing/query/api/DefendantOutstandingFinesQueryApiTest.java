package uk.gov.moj.cpp.hearing.query.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;

import java.util.function.Function;

import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;


@RunWith(MockitoJUnitRunner.class)
public class DefendantOutstandingFinesQueryApiTest {

    public static final String STAGINGENFORCEMENT_QUERY_OUTSTANDING_FINES = "stagingenforcement.defendant.outstanding-fines";

    @Mock
    JsonEnvelope jsonEnvelopeFromHearing;

    @Mock
    JsonEnvelope jsonEnvelopeFromStaging;

    @Mock
    private Requester requester;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Mock
    private Function<Object, JsonEnvelope> function;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;


    @Test
    public void should_return_outstanding_fines_when_defendant_id_is_known() {
        when(requester.request(anyObject())).thenReturn(jsonEnvelopeFromHearing);
        JsonObject responseFromHearingQueryView = getDefendantDetails();

        setUp(responseFromHearingQueryView);

        hearingQueryApi.getDefendantOutstandingFines(jsonEnvelopeFromHearing);

        verify(requester, times(1)).requestAsAdmin(anyObject());

    }


    @Test
    public void should_return_NO_outstanding_fines_when_defendant_id_is_unknown() {
        when(requester.request(anyObject())).thenReturn(jsonEnvelopeFromHearing);
        JsonObject emptyResponseFromHearingQueryView = Json.createObjectBuilder().build();

        setUp(emptyResponseFromHearingQueryView);

        JsonEnvelope defendantOutstandingFines = hearingQueryApi.getDefendantOutstandingFines(jsonEnvelopeFromHearing);

        verify(requester, times(0)).requestAsAdmin(anyObject());
        assertTrue(defendantOutstandingFines.payloadAsJsonObject().getJsonArray("outstandingFines").isEmpty());
    }

    private void setUp(JsonObject responseFromHearingQueryView) {
        when(jsonEnvelopeFromHearing.payloadAsJsonObject()).thenReturn(responseFromHearingQueryView);
        when(enveloper.withMetadataFrom(jsonEnvelopeFromHearing, STAGINGENFORCEMENT_QUERY_OUTSTANDING_FINES)).thenReturn(function);
        when(function.apply(anyObject())).thenReturn(jsonEnvelopeFromHearing);
        when(requester.requestAsAdmin(anyObject())).thenReturn(jsonEnvelopeFromStaging);
        when(jsonEnvelopeFromStaging.payloadAsJsonObject()).thenReturn(Json.createObjectBuilder().build());
    }


    private JsonObject getDefendantDetails() {
        return createObjectBuilder()
                .add("forename", "Max")
                .add("surename", "Tango")
                .build();
    }


}
