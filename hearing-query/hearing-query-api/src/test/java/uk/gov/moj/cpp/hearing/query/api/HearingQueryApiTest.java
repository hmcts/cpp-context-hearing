package uk.gov.moj.cpp.hearing.query.api;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory;
import uk.gov.moj.cpp.hearing.domain.referencedata.HearingTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.query.api.service.progression.ProgressionService;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.PIEventMapperCache;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingQueryApiTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-query-api.raml";
    private static final String NAME = "name:";
    private static final UUID CASE_ID = randomUUID();
    private static final UUID APPLICATION_ID_1 = randomUUID();
    private static final String FIELD_CASE_IDS = "caseIds";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    @Mock
    private HearingQueryView hearingQueryView;

    @Mock
    private ProgressionService progressionService;

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private Envelope<Timeline> mockCaseTimelineEnvelope;

    @Mock
    private Envelope<Timeline> mockApplicationTimelineEnvelope;

    @Mock
    private Envelope<JsonValue> mockJsonValueEnvelope;

    @Mock
    private Envelope<GetHearings> mockGetHearingsEnvelope;

    @Mock
    private Envelope<ProsecutionCaseResponse> mockGetProsecutionCaseEnvelope;

    @Mock
    private JsonEnvelope mockJsonEnvelope;

    @Mock
    private EnvelopePayloadTypeConverter mockEnvelopePayloadTypeConverter;

    @Mock
    private JsonEnvelopeRepacker mockJsonEnvelopeRepacker;

    @Mock
    private PIEventMapperCache piEventMapperCache;

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

    @Test
    public void shouldReturnFutureHearings() {

        when(hearingQueryView.findHearingsForFuture(any(), any())).thenReturn(null);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get.hearings-for-future", createObjectBuilder()
                .add("defendantId", UUID.randomUUID().toString())
                .build());

        hearingQueryApi.findHearingsForFuture(query);

        verify(referenceDataService, times(1)).getAllHearingTypes();
        verify(hearingQueryView, times(1)).findHearingsForFuture(any(JsonEnvelope.class), any(HearingTypes.class));
    }

    @Test
    public void shouldGetFutureHearingsByCaseIds() {
        final String caseId1 = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String caseId2 = "c0a03dfd-f6f2-4590-a026-17f1cf5268e1";
        final String caseIdString = caseId1 + "," + caseId2;

        when(hearingQueryView.getFutureHearingsByCaseIds(any(JsonEnvelope.class))).thenReturn(mockGetHearingsEnvelope);
        when(mockEnvelopePayloadTypeConverter.convert(any(JsonEnvelope.class), any(Class.class))).thenReturn(mockJsonValueEnvelope);
        when(mockJsonEnvelopeRepacker.repack(mockJsonValueEnvelope)).thenReturn(mockJsonEnvelope);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get.hearings", createObjectBuilder()
                .add(FIELD_CASE_IDS, caseIdString)
                .build());

        final JsonEnvelope result = hearingQueryApi.getFutureHearingsByCaseIds(query);

        verify(hearingQueryView).getFutureHearingsByCaseIds(any(JsonEnvelope.class));
        assertThat(result, is(mockJsonEnvelope));
    }

    @Test
    public void shouldGetProsecutionCaseIfHearingEventIsPresnet() {
        final String hearingId = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String hearingEventId = "abdaeb88-8952-4c07-99c4-d27c39d4e63a";

        when(piEventMapperCache.getCppHearingEventIds()).thenReturn(buildPIEventCache());
        when(hearingQueryView.getProsecutionCaseForHearing(any(JsonEnvelope.class))).thenReturn(mockGetProsecutionCaseEnvelope);
        when(mockEnvelopePayloadTypeConverter.convert(any(JsonEnvelope.class), any(Class.class))).thenReturn(mockJsonValueEnvelope);
        when(mockJsonEnvelopeRepacker.repack(mockJsonValueEnvelope)).thenReturn(mockJsonEnvelope);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.prosecution-case-by-hearingid", createObjectBuilder()
                .add(FIELD_HEARING_ID, hearingId)
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, hearingEventId)
                .build());

        final JsonEnvelope result = hearingQueryApi.getProsecutionCaseForHearing(query);

        verify(hearingQueryView).getProsecutionCaseForHearing(any(JsonEnvelope.class));
        assertThat(result, is(mockJsonEnvelope));
    }

    @Test
    public void shouldNotGetProsecutionCaseIfHearingEventIsNoPresnet() {
        final String hearingId = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String hearingEventId = String.valueOf(randomUUID());

        when(piEventMapperCache.getCppHearingEventIds()).thenReturn(buildPIEventCache());
        when(mockEnvelopePayloadTypeConverter.convert(any(JsonEnvelope.class), any(Class.class))).thenReturn(mockJsonValueEnvelope);
        when(mockJsonEnvelopeRepacker.repack(mockJsonValueEnvelope)).thenReturn(mockJsonEnvelope);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.prosecution-case-by-hearingid", createObjectBuilder()
                .add(FIELD_HEARING_ID, hearingId)
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, hearingEventId)
                .build());

        final JsonEnvelope result = hearingQueryApi.getProsecutionCaseForHearing(query);

        assertThat(result, is(query));
    }

    private Set<UUID> buildPIEventCache() {
        final UUID cpHearingEventId_1 = randomUUID();
        final UUID cpHearingEventId_2 = UUID.fromString("abdaeb88-8952-4c07-99c4-d27c39d4e63a");
        final Set<UUID> hearingEventIds = new HashSet();
        hearingEventIds.add(cpHearingEventId_1);
        hearingEventIds.add(cpHearingEventId_2);
        return hearingEventIds;
    }
}
