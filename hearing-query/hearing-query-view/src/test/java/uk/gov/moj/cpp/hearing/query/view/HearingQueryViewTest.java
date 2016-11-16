package uk.gov.moj.cpp.hearing.query.view;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;


@RunWith(MockitoJUnitRunner.class)
public class HearingQueryViewTest {

    @Mock
    private JsonEnvelope query;

    @Mock
    private JsonEnvelope response;

    @Mock
    private JsonObject jsonObject;

    @Mock
    private Enveloper enveloper;

    @Mock
    private ListToJsonArrayConverter helperService;

    @Mock
    private List<HearingView> hearings;

    @Mock
    private HearingView hearingView;

    @Mock
    private JsonArray jsonArrayHearings;

    @Mock
    private HearingService hearingService;

    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private JsonObject listOfHearingViewResponce;

    @Mock
    private JsonEnvelope responceJson;

    @InjectMocks
    private HearingQueryView hearingsQueryView;

    @Test
    public void shouldFindHearings() {
        UUID caseId = UUID.randomUUID();
        String now = LocalDate.now().toString();
        JsonObject jsonObject = Json.createObjectBuilder()
                        .add(HearingQueryView.FIELD_CASE_ID, caseId.toString())
                        .add(HearingQueryView.FIELD_FROM_DATE, now)
                        .add(HearingQueryView.FIELD_HEARING_TYPE, "PTP").build();
        when(query.payloadAsJsonObject()).thenReturn(jsonObject);

        when(hearingService.getHearingsForCase(caseId, Optional.of(now), Optional.of("PTP")))
                        .thenReturn(hearings);

        JsonArray arrayOfHearings =
                        Json.createArrayBuilder().add(Json.createObjectBuilder().build()).build();

        when(helperService.convert(hearings)).thenReturn(arrayOfHearings);

        when(enveloper.withMetadataFrom(query, HearingQueryView.NAME_RESPONSE_HEARING_LIST))
                        .thenReturn(function);

        when(function.apply(Json.createObjectBuilder().add("hearings", arrayOfHearings).build()))
                        .thenReturn(responceJson);

        assertThat(hearingsQueryView.findHearings(query), equalTo(responceJson));
    }

    @Test
    public void shouldFindHearing() {
        UUID hearingId = UUID.randomUUID();
        String now = LocalDate.now().toString();
        JsonObject jsonObject =
                        Json.createObjectBuilder()
                                        .add(HearingQueryView.FIELD_HEARING_ID,
                                                        hearingId.toString())
                                        .add(HearingQueryView.FIELD_FROM_DATE, now)
                                        .add(HearingQueryView.FIELD_HEARING_TYPE, "PTP").build();
        when(query.payloadAsJsonObject()).thenReturn(jsonObject);

        when(hearingService.getHearingById(hearingId)).thenReturn(hearingView);

        when(enveloper.withMetadataFrom(query, HearingQueryView.NAME_RESPONSE_HEARING))
                        .thenReturn(function);

        when(function.apply(hearingView)).thenReturn(responceJson);

        assertThat(hearingsQueryView.findHearing(query), equalTo(responceJson));
    }

}
