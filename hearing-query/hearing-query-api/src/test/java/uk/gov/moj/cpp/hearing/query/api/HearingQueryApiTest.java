package uk.gov.moj.cpp.hearing.query.api;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.LinkedApplicationsSummary;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.query.api.service.progression.ProgressionService;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingQueryApiTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-query-api.raml";
    private static final String NAME = "name:";
    private static final UUID CASE_ID = randomUUID();
    private static final UUID APPLICATION_ID_1 = randomUUID();
    private static final UUID APPLICATION_ID_2 = randomUUID();

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Mock
    private HearingQueryView hearingQueryView;

    @Mock
    private ProgressionService progressionService;

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private Envelope<Timeline> mockCaseTimelineEnvelope;

    @Mock
    private Envelope<Timeline> mockApplicationTimelineEnvelope;

    @Mock
    private Envelope<JsonValue> mockJsonValueEnvelope;

    @Mock
    private JsonEnvelope mockJsonEnvelope;

    @Mock
    private EnvelopePayloadTypeConverter mockEnvelopePayloadTypeConverter;

    @Mock
    private JsonEnvelopeRepacker mockJsonEnvelopeRepacker;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;

    private Map<String, String> apiMethodsToHandlerNames;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        apiMethodsToHandlerNames = stream(HearingQueryApi.class.getMethods())
                .filter(method -> method.getAnnotation(Handles.class) != null)
                .collect(toMap(Method::getName, method -> method.getAnnotation(Handles.class).value()));
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> ramlActionNames = readLines(new File(PATH_TO_RAML)).stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(NAME))
                .map(line -> line.replaceAll(NAME, "").trim())
                .collect(toList());

        assertThat(apiMethodsToHandlerNames.values(), containsInAnyOrder(ramlActionNames.toArray()));
    }

    @Test
    public void shouldReturnTimelineForApplication() {

        when(hearingQueryView.getTimelineByApplicationId(any(JsonEnvelope.class), any(CrackedIneffectiveVacatedTrialTypes.class), any(JsonObject.class))).thenReturn(mockApplicationTimelineEnvelope);
        when(mockEnvelopePayloadTypeConverter.convert(any(JsonEnvelope.class), any(Class.class))).thenReturn(mockJsonValueEnvelope);
        when(mockJsonEnvelopeRepacker.repack(mockJsonValueEnvelope)).thenReturn(mockJsonEnvelope);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.application.timeline.", createObjectBuilder()
                .add("id", APPLICATION_ID_1.toString())
                .build());

        final JsonEnvelope result = hearingQueryApi.getApplicationTimeline(query);

        verify(referenceDataService, times(1)).listAllCrackedIneffectiveVacatedTrialTypes();
        verify(referenceDataService, times(1)).getAllCourtRooms(any(JsonEnvelope.class));
        verify(hearingQueryView, times(1)).getTimelineByApplicationId(any(JsonEnvelope.class), any(CrackedIneffectiveVacatedTrialTypes.class), any(JsonObject.class));

        assertThat(result, is(mockJsonEnvelope));

    }

    @Test
    public void shouldReturnTimelineForCase() {
        final List<TimelineHearingSummary> timelineHearingSummaries = new ArrayList<>();
        final TimelineHearingSummary timelineHearingSummary = new TimelineHearingSummary.TimelineHearingSummaryBuilder().withHearingId(randomUUID()).build();
        timelineHearingSummaries.add(timelineHearingSummary);

        final Timeline expectedTimeline = new Timeline(timelineHearingSummaries);

        when(progressionService.getProsecutionCaseDetails(CASE_ID)).thenReturn(null);
        when(hearingQueryView.getTimeline(any(JsonEnvelope.class), any(CrackedIneffectiveVacatedTrialTypes.class), any(JsonObject.class))).thenReturn(mockCaseTimelineEnvelope);
        when(mockCaseTimelineEnvelope.payload()).thenReturn(expectedTimeline);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.case.timeline", createObjectBuilder()
                .add("id", CASE_ID.toString())
                .build());

        hearingQueryApi.getCaseTimeline(query);

        verify(referenceDataService, times(1)).listAllCrackedIneffectiveVacatedTrialTypes();
        verify(referenceDataService, times(1)).getAllCourtRooms(any(JsonEnvelope.class));
        verify(hearingQueryView, times(1)).getTimeline(any(JsonEnvelope.class), any(CrackedIneffectiveVacatedTrialTypes.class), any(JsonObject.class));
        verify(hearingQueryView, times(0)).getTimelineByApplicationId(any(JsonEnvelope.class), any(CrackedIneffectiveVacatedTrialTypes.class), any(JsonObject.class));
    }

}
