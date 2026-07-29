package uk.gov.moj.cpp.hearing.query.view;

import static java.time.LocalDate.parse;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;

import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingQueryViewTest {

    private static final String RESPONSE_NAME_GET_HEARINGS_CHECK_IN = "hearing.get.hearings-check-in";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_ROOM_ID = "roomId";

    private static final LocalDate HEARING_DATE = parse("2026-07-16");
    private static final UUID COURT_CENTRE_ID = randomUUID();
    private static final UUID ROOM_ID = randomUUID();

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private HearingQueryView target;

    @Test
    public void shouldGetHearingsForCheckInWhenRoomIdIsPresent() {
        final List<UUID> accessibleIds = singletonList(randomUUID());
        final GetHearings expectedResponse = GetHearings.getHearings().build();

        when(hearingService.getHearingsForCheckIn(HEARING_DATE, COURT_CENTRE_ID, ROOM_ID, accessibleIds, true))
                .thenReturn(expectedResponse);

        final JsonEnvelope query = envelopeFrom(HEARING_DATE, COURT_CENTRE_ID, ROOM_ID);

        final Envelope<GetHearings> result = target.getHearingCheckIn(query, accessibleIds, true);

        verify(hearingService).getHearingsForCheckIn(HEARING_DATE, COURT_CENTRE_ID, ROOM_ID, accessibleIds, true);
        assertThat(result.metadata().name(), is(RESPONSE_NAME_GET_HEARINGS_CHECK_IN));
        assertThat(result.payload(), is(sameInstance(expectedResponse)));
    }

    @Test
    public void shouldGetHearingsForCheckInWithNullRoomIdWhenRoomIdIsAbsent() {
        final List<UUID> accessibleIds = singletonList(randomUUID());
        final GetHearings expectedResponse = GetHearings.getHearings().build();

        when(hearingService.getHearingsForCheckIn(eq(HEARING_DATE), eq(COURT_CENTRE_ID), isNull(), eq(accessibleIds), eq(false)))
                .thenReturn(expectedResponse);

        final JsonEnvelope query = envelopeFrom(HEARING_DATE, COURT_CENTRE_ID, null);

        final Envelope<GetHearings> result = target.getHearingCheckIn(query, accessibleIds, false);

        final ArgumentCaptor<UUID> roomIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(hearingService).getHearingsForCheckIn(eq(HEARING_DATE), eq(COURT_CENTRE_ID), roomIdCaptor.capture(), eq(accessibleIds), eq(false));
        assertThat(roomIdCaptor.getValue(), is(nullValue()));
        assertThat(result.metadata().name(), is(RESPONSE_NAME_GET_HEARINGS_CHECK_IN));
        assertThat(result.payload(), is(sameInstance(expectedResponse)));
    }

    @Test
    public void shouldPassAccessibleCaseAndApplicationIdsThroughToService() {
        final UUID accessibleId = randomUUID();
        final List<UUID> accessibleIds = singletonList(accessibleId);
        final GetHearings expectedResponse = GetHearings.getHearings().build();

        when(hearingService.getHearingsForCheckIn(HEARING_DATE, COURT_CENTRE_ID, ROOM_ID, accessibleIds, false))
                .thenReturn(expectedResponse);

        final JsonEnvelope query = envelopeFrom(HEARING_DATE, COURT_CENTRE_ID, ROOM_ID);

        target.getHearingCheckIn(query, accessibleIds, false);

        final ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(hearingService).getHearingsForCheckIn(eq(HEARING_DATE), eq(COURT_CENTRE_ID), eq(ROOM_ID), idsCaptor.capture(), eq(false));
        assertThat(idsCaptor.getValue(), contains(accessibleId));
    }

    private JsonEnvelope envelopeFrom(final LocalDate date, final UUID courtCentreId, final UUID roomId) {
        final var payloadBuilder = createObjectBuilder()
                .add(FIELD_DATE, date.toString())
                .add(FIELD_COURT_CENTRE_ID, courtCentreId.toString());
        if (roomId != null) {
            payloadBuilder.add(FIELD_ROOM_ID, roomId.toString());
        }
        return uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom(
                metadataWithRandomUUIDAndName(),
                payloadBuilder.build());
    }
}
