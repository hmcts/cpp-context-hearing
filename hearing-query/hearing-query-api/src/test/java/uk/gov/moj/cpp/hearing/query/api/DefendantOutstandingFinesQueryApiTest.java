package uk.gov.moj.cpp.hearing.query.api;

import static javax.json.Json.createObjectBuilder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;

import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.HearingEventQueryView;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;
import uk.gov.moj.cpp.hearing.query.view.SessionTimeQueryView;

import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
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

    @Mock
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Mock
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Mock
    private HearingEventQueryView hearingEventQueryView;

    @Mock
    private HearingQueryView hearingQueryView;

    @Mock
    private SessionTimeQueryView sessionTimeQueryView;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;


    @Test
    public void should_return_outstanding_fines_when_defendant_id_is_known() {
        when(hearingQueryView.getOutstandingFinesQueryFromDefendantId(any())).thenReturn(jsonEnvelopeFromHearing);
        JsonObject responseFromHearingQueryView = getDefendantDetails();

        setUp(responseFromHearingQueryView);
        when(enveloper.withMetadataFrom(jsonEnvelopeFromHearing, STAGINGENFORCEMENT_QUERY_OUTSTANDING_FINES)).thenReturn(function);
        when(function.apply(any())).thenReturn(jsonEnvelopeFromHearing);
        when(requester.requestAsAdmin(any())).thenReturn(jsonEnvelopeFromStaging);
        when(jsonEnvelopeFromStaging.payloadAsJsonObject()).thenReturn(Json.createObjectBuilder().build());


        hearingQueryApi.getDefendantOutstandingFines(jsonEnvelopeFromHearing);

        verify(requester, times(1)).requestAsAdmin(any());

    }


    @Test
    public void should_return_NO_outstanding_fines_when_defendant_id_is_unknown() {
        when(hearingQueryView.getOutstandingFinesQueryFromDefendantId(any())).thenReturn(jsonEnvelopeFromHearing);
        JsonObject emptyResponseFromHearingQueryView = Json.createObjectBuilder().build();

        setUp(emptyResponseFromHearingQueryView);

        JsonEnvelope defendantOutstandingFines = hearingQueryApi.getDefendantOutstandingFines(jsonEnvelopeFromHearing);

        verify(requester, times(0)).requestAsAdmin(any());
        assertTrue(defendantOutstandingFines.payloadAsJsonObject().getJsonArray("outstandingFines").isEmpty());
    }

    private void setUp(JsonObject responseFromHearingQueryView) {
        when(jsonEnvelopeFromHearing.payloadAsJsonObject()).thenReturn(responseFromHearingQueryView);
    }


    private JsonObject getDefendantDetails() {
        return createObjectBuilder()
                .add("forename", "Max")
                .add("surename", "Tango")
                .build();
    }


}
