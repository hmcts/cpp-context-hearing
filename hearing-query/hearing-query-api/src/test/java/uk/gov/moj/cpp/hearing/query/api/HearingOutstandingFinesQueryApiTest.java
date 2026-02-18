package uk.gov.moj.cpp.hearing.query.api;

import static java.util.Collections.singletonList;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OutstandingFinesQuery;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class HearingOutstandingFinesQueryApiTest {

    private static final UUID COURT_CENTRE_ID = UUID.randomUUID();
    private static final UUID COURT_ROOM_ID = UUID.randomUUID();
    private static final LocalDate HEARING_DATE = LocalDate.now();

    @Mock
    private Requester requester;

    @Mock
    private HearingQueryView hearingQueryView;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;

    @Test
    public void should_return_empty_payload_and_not_call_staging_enforcement_when_defendant_info_is_empty() {
        final JsonEnvelope queryEnvelope = createQueryEnvelope();
        final OutstandingFinesQuery outstandingFinesQuery = OutstandingFinesQuery.newBuilder()
                .withCourtCentreId(COURT_CENTRE_ID)
                .withCourtRoomIds(singletonList(COURT_ROOM_ID))
                .withHearingDate(HEARING_DATE)
                .build();

        when(jsonObjectToObjectConverter.convert(any(JsonObject.class), eq(OutstandingFinesQuery.class)))
                .thenReturn(outstandingFinesQuery);

        final JsonEnvelope emptyDefendantInfoEnvelope = createEnvelope("hearing.defendant.info",
                Json.createObjectBuilder().build());
        when(hearingQueryView.getDefendantInfoFromCourtHouseId(any(JsonEnvelope.class)))
                .thenReturn(emptyDefendantInfoEnvelope);

        final JsonEnvelope result = hearingQueryApi.getHearingOutstandingFines(queryEnvelope);

        verify(requester, never()).requestAsAdmin(any(), eq(JsonObject.class));
        assertThat(result.payloadAsJsonObject().isEmpty(), is(true));
    }

    @Test
    public void should_call_staging_enforcement_and_return_result_when_defendant_info_is_not_empty() {
        final JsonEnvelope queryEnvelope = createQueryEnvelope();
        final OutstandingFinesQuery outstandingFinesQuery = OutstandingFinesQuery.newBuilder()
                .withCourtCentreId(COURT_CENTRE_ID)
                .withCourtRoomIds(singletonList(COURT_ROOM_ID))
                .withHearingDate(HEARING_DATE)
                .build();

        when(jsonObjectToObjectConverter.convert(any(JsonObject.class), eq(OutstandingFinesQuery.class)))
                .thenReturn(outstandingFinesQuery);

        final JsonObject defendantInfoPayload = createObjectBuilder()
                .add("courtCentreId", COURT_CENTRE_ID.toString())
                .add("courtRoomIds", COURT_ROOM_ID.toString())
                .add("hearingDate", HEARING_DATE.toString())
                .build();
        final JsonEnvelope defendantInfoEnvelope = createEnvelope("hearing.defendant.info", defendantInfoPayload);
        when(hearingQueryView.getDefendantInfoFromCourtHouseId(any(JsonEnvelope.class)))
                .thenReturn(defendantInfoEnvelope);

        final JsonObject stagingEnforcementResponse = createObjectBuilder()
                .add("courtRooms", Json.createArrayBuilder().build())
                .build();
        @SuppressWarnings("unchecked")
        final Envelope<JsonObject> stagingEnvelope = (Envelope<JsonObject>) (Envelope<?>) Envelope.envelopeFrom(
                defendantInfoEnvelope.metadata(), stagingEnforcementResponse);
        when(requester.requestAsAdmin(any(JsonEnvelope.class), eq(JsonObject.class)))
                .thenReturn(stagingEnvelope);

        final JsonEnvelope result = hearingQueryApi.getHearingOutstandingFines(queryEnvelope);

        verify(requester, times(1)).requestAsAdmin(any(JsonEnvelope.class), eq(JsonObject.class));
        assertThat(result.payloadAsJsonObject(), is(stagingEnforcementResponse));
    }

    private static JsonEnvelope createQueryEnvelope() {
        final JsonObject queryPayload = createObjectBuilder()
                .add("courtCentreId", COURT_CENTRE_ID.toString())
                .add("courtRoomIds", Json.createArrayBuilder().add(COURT_ROOM_ID.toString()))
                .add("hearingDate", HEARING_DATE.toString())
                .build();
        return createEnvelope("hearing.query.outstanding-fines", queryPayload);
    }
}
