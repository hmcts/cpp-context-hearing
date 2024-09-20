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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.LinkedApplicationsSummary;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.query.api.service.progression.ProgressionService;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.PIEventMapperCache;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.XhibitEventMapperCache;
import uk.gov.moj.cpp.hearing.query.api.service.usergroups.UserGroupQueryService;
import uk.gov.moj.cpp.hearing.query.view.HearingEventQueryView;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;
import uk.gov.moj.cpp.hearing.query.view.SessionTimeQueryView;
import uk.gov.moj.cpp.hearing.query.view.response.SessionTimeResponse;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.GetShareResultsV2Response;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    private Envelope<JsonObject> mockJsonObjectEnvelope;

    @Mock
    private Envelope<TargetListResponse> mockTargetListResponseEnvelope;
    @Mock
    private Envelope<CrackedIneffectiveTrial> mockCrackedIneffectiveTrialEnvelope;
    @Mock
    private Envelope<GetShareResultsV2Response> mockGetShareResultsV2ResponseEnvelope;

    @Mock
    private Envelope<NowListResponse> mockNowListResponseEnvelope;

    @Mock
    private Envelope<GetHearings> mockGetHearingsEnvelope;

    @Mock
    private Envelope<SessionTimeResponse> mockSessionTimeResponse;

    @Mock
    private List<Prompt> prompts;

    @Mock
    private SessionTimeQueryView sessionTimeQueryView;

    @Mock
    private Envelope<ProsecutionCaseResponse> mockGetProsecutionCaseEnvelope;

    @Mock
    private JsonEnvelope mockJsonEnvelope;

    @Mock
    private Set<UUID> mockSetUUIDEnvelope;

    @Mock
    private XhibitEventMapperCache xhibitEventMapperCache;

    @Mock
    private EnvelopePayloadTypeConverter mockEnvelopePayloadTypeConverter;

    @Mock
    private JsonEnvelopeRepacker mockJsonEnvelopeRepacker;

    @Mock
    private PIEventMapperCache piEventMapperCache;

    @Inject
    private PIEventMapperCache piEventMapperCache1;

    @Mock
    private HearingEventQueryView hearingEventQueryView;

    @Mock
    private ProsecutionCase prosecutionCase;

    @Mock
    private CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;

    @Mock
    private UserGroupQueryService userGroupQueryService;

    private Map<String, String> apiMethodsToHandlerNames;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        apiMethodsToHandlerNames = stream(HearingQueryApi.class.getMethods())
                .filter(method -> method.getAnnotation(Handles.class) != null)
                .collect(toMap(Method::getName, method -> method.getAnnotation(Handles.class).value()));
        piEventMapperCache1 = new PIEventMapperCache();
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> ramlActionNames = readLines(new File(PATH_TO_RAML)).stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(NAME))
                .map(line -> line.replaceAll(NAME, "").trim())
                .collect(toList());

        //The below one is not implemented in HearingQueryApi, it is implemented in DefaultQueryApiHearingEventLogReportResource, so removing from map
        ramlActionNames.remove("hearing.get-hearing-event-log-extract-for-documents");

        assertThat(apiMethodsToHandlerNames.values(), containsInAnyOrder(ramlActionNames.toArray()));
    }

    @Test
    public void shouldReturnTimelineForApplication() {

        when(hearingQueryView.getTimelineByApplicationId(any(), any(), any())).thenReturn(mockApplicationTimelineEnvelope);
        when(mockEnvelopePayloadTypeConverter.convert(any(), any(Class.class))).thenReturn(mockJsonValueEnvelope);
        when(mockJsonEnvelopeRepacker.repack(mockJsonValueEnvelope)).thenReturn(mockJsonEnvelope);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.application.timeline.", createObjectBuilder()
                .add("id", APPLICATION_ID_1.toString())
                .build());

        final JsonEnvelope result = hearingQueryApi.getApplicationTimeline(query);

        verify(referenceDataService, times(1)).listAllCrackedIneffectiveVacatedTrialTypes();
        verify(referenceDataService, times(1)).getAllCourtRooms(any(JsonEnvelope.class));
        verify(hearingQueryView, times(1)).getTimelineByApplicationId(any(), any(), any());

        assertThat(result, is(mockJsonEnvelope));

    }

    @Test
    public void shouldReturnTimelineForCase() {
        final List<TimelineHearingSummary> timelineHearingSummaries = new ArrayList<>();
        final TimelineHearingSummary timelineHearingSummary = new TimelineHearingSummary.TimelineHearingSummaryBuilder().withHearingId(randomUUID()).build();
        timelineHearingSummaries.add(timelineHearingSummary);

        final Timeline expectedTimeline = new Timeline(timelineHearingSummaries);

        when(hearingQueryView.getTimeline(any(), any(), any())).thenReturn(mockCaseTimelineEnvelope);
        when(mockCaseTimelineEnvelope.payload()).thenReturn(expectedTimeline);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.case.timeline", createObjectBuilder()
                .add("id", CASE_ID.toString())
                .build());

        hearingQueryApi.getCaseTimeline(query);

        verify(referenceDataService, times(1)).listAllCrackedIneffectiveVacatedTrialTypes();
        verify(referenceDataService, times(1)).getAllCourtRooms(any(JsonEnvelope.class));
        verify(hearingQueryView, times(1)).getTimeline(any(), any(), any());
        verify(hearingQueryView, times(0)).getTimelineByApplicationId(any(JsonEnvelope.class), any(CrackedIneffectiveVacatedTrialTypes.class), any(JsonObject.class));
    }

    @Test
    public void shouldReturnTimelineForCaseWhenApplicationSummaryIsNull() {
        final List<TimelineHearingSummary> timelineHearingSummaries = new ArrayList<>();
        final TimelineHearingSummary timelineHearingSummary = new TimelineHearingSummary.TimelineHearingSummaryBuilder().withHearingType("Review").withHearingId(randomUUID()).build();
        timelineHearingSummaries.add(timelineHearingSummary);

        final Timeline expectedTimeline = new Timeline(timelineHearingSummaries);

        when(progressionService.getProsecutionCaseDetails(CASE_ID)).thenReturn(ProsecutionCase.prosecutionCase().build());
        when(hearingQueryView.getTimeline(any(), any(), any())).thenReturn(mockCaseTimelineEnvelope);
        when(mockCaseTimelineEnvelope.payload()).thenReturn(expectedTimeline);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.case.timeline", createObjectBuilder()
                .add("id", CASE_ID.toString())
                .build());

        hearingQueryApi.getCaseTimeline(query);

        verify(referenceDataService, times(1)).listAllCrackedIneffectiveVacatedTrialTypes();
        verify(referenceDataService, times(1)).getAllCourtRooms(any(JsonEnvelope.class));
        verify(hearingQueryView, times(1)).getTimeline(any(JsonEnvelope.class), any(), any());
        verify(hearingQueryView, times(0)).getTimelineByApplicationId(any(JsonEnvelope.class), any(CrackedIneffectiveVacatedTrialTypes.class), any(JsonObject.class));
    }

    @Test
    public void shouldReturnTimelineForCaseWhenApplicationSummaryIsNotNull() {
        final List<TimelineHearingSummary> timelineHearingSummaries = new ArrayList<>();
        final TimelineHearingSummary timelineHearingSummary = new TimelineHearingSummary.TimelineHearingSummaryBuilder().withHearingType("Review").withHearingId(randomUUID()).build();
        timelineHearingSummaries.add(timelineHearingSummary);

        final Timeline expectedTimeline = new Timeline(timelineHearingSummaries);

        when(progressionService.getProsecutionCaseDetails(CASE_ID)).thenReturn(ProsecutionCase.prosecutionCase().build());
        when(hearingQueryView.getTimeline(any(), any(), any())).thenReturn(mockCaseTimelineEnvelope);
        when(mockCaseTimelineEnvelope.payload()).thenReturn(expectedTimeline);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.case.timeline", createObjectBuilder()
                .add("id", CASE_ID.toString())
                .build());

        List<LinkedApplicationsSummary> linkedApplicationsSummary = new ArrayList<>();
        linkedApplicationsSummary.add(new LinkedApplicationsSummary.Builder().withApplicationId(UUID.randomUUID()).build());

        when(progressionService.getProsecutionCaseDetails(CASE_ID)).thenReturn(prosecutionCase);
        when(prosecutionCase.getLinkedApplicationsSummary()).thenReturn(linkedApplicationsSummary);

        hearingQueryApi.getCaseTimeline(query);

        verify(referenceDataService, times(1)).listAllCrackedIneffectiveVacatedTrialTypes();
        verify(referenceDataService, times(1)).getAllCourtRooms(any(JsonEnvelope.class));
        verify(hearingQueryView, times(1)).getTimeline(any(), any(), any());
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
        verify(hearingQueryView, times(1)).findHearingsForFuture(any(), any());
    }

    @Test
    public void shouldGetFutureHearingsByCaseIds() {
        final String caseId1 = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String caseId2 = "c0a03dfd-f6f2-4590-a026-17f1cf5268e1";
        final String caseIdString = caseId1 + "," + caseId2;

        when(hearingQueryView.getFutureHearingsByCaseIds(any(JsonEnvelope.class))).thenReturn(mockGetHearingsEnvelope);
        when(mockEnvelopePayloadTypeConverter.convert(any(), any(Class.class))).thenReturn(mockJsonValueEnvelope);
        when(mockJsonEnvelopeRepacker.repack(mockJsonValueEnvelope)).thenReturn(mockJsonEnvelope);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get.hearings", createObjectBuilder()
                .add(FIELD_CASE_IDS, caseIdString)
                .build());

        final JsonEnvelope result = hearingQueryApi.getFutureHearingsByCaseIds(query);

        verify(hearingQueryView).getFutureHearingsByCaseIds(any(JsonEnvelope.class));
        assertThat(result, is(mockJsonEnvelope));
    }

   /* @Test
    public void shouldGetCasesByPersonDefendant(){
        final JsonEnvelope envelope = EnvelopeFactory.createEnvelope("hearing.get.cases-by-person-defendant", createObjectBuilder()
                .add("firstName", randomAlphabetic(5))
                .add("lastName", randomAlphabetic(5))
                .add("dateOfBirth", now().minusYears(25).toString())
                .add("hearingDate", now().toString())
                .add("caseIds", randomUUID().toString() +","+ randomUUID().toString())
                .build());
        hearingQueryApi.getCasesByPersonDefendant(envelope);
        verify(hearingQueryView).getCasesByPersonDefendant(envelope);
    }

    @Test
    public void shouldGetCasesByOrganisationDefendant(){
        final JsonEnvelope envelope = EnvelopeFactory.createEnvelope("hearing.get.cases-by-organisation-defendant", createObjectBuilder()
                .add("organisationName", randomAlphabetic(5))
                .add("hearingDate", now().toString())
                .add("caseIds", randomUUID().toString() +","+ randomUUID().toString())
                .build());
        hearingQueryApi.getCasesByOrganisationDefendant(envelope);
        verify(hearingQueryView).getCasesByOrganisationDefendant(envelope);
    }*/

    @Test
    public void shouldGetProsecutionCaseIfHearingEventIsPresnet() {
        final String hearingId = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String hearingEventId = "abdaeb88-8952-4c07-99c4-d27c39d4e63a";

        when(piEventMapperCache.getCppHearingEventIds()).thenReturn(buildPIEventCache());
        when(hearingQueryView.getProsecutionCaseForHearing(any(JsonEnvelope.class))).thenReturn(mockGetProsecutionCaseEnvelope);
        when(mockEnvelopePayloadTypeConverter.convert(any(), any(Class.class))).thenReturn(mockJsonValueEnvelope);
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

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.prosecution-case-by-hearingid", createObjectBuilder()
                .add(FIELD_HEARING_ID, hearingId)
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, hearingEventId)
                .build());

        final JsonEnvelope result = hearingQueryApi.getProsecutionCaseForHearing(query);

        assertThat(result, is(query));
    }

    @Test
    public void shouldProcessGetHearingEventLogForCdesDocument() {
        when(userGroupQueryService.doesUserBelongsToHmctsOrganisation(any())).thenReturn(true);
        when(hearingEventQueryView.getHearingEventLogForDocuments(any())).thenReturn(null);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-log-for-cdes-document", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .build());

        hearingQueryApi.getHearingEventLogForDocuments(query);

        verify(hearingEventQueryView, times(1)).getHearingEventLogForDocuments(any(JsonEnvelope.class));
    }

    @Test
    public void shouldNotProcessGetHearingEventLogForCdesDocumentForNonHMCTSUser() {
        when(userGroupQueryService.doesUserBelongsToHmctsOrganisation(any())).thenReturn(false);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-log-for-cdes-document", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .build());

        hearingQueryApi.getHearingEventLogForDocuments(query);

        verify(hearingEventQueryView, times(0)).getHearingEventLogForDocuments(any(JsonEnvelope.class));
    }

    @Test
    public void shouldProcessGetHearingEventLogCount() {
        when(userGroupQueryService.doesUserBelongsToHmctsOrganisation(any())).thenReturn(true);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-log-count", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("hearingDate", LocalDate.now().toString())
                .build());

        hearingQueryApi.getHearingEventLogCount(query);

        verify(hearingEventQueryView, times(1)).getHearingEventLogCount(any(JsonEnvelope.class));
    }

    @Test
    public void shouldNotProcessGetHearingEventLogCountForNonHMCTSUser() {
        when(userGroupQueryService.doesUserBelongsToHmctsOrganisation(any())).thenReturn(false);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-log-count", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("hearingDate", LocalDate.now().toString())
                .build());

        hearingQueryApi.getHearingEventLogCount(query);

        verify(hearingEventQueryView, times(0)).getHearingEventLogCount(any(JsonEnvelope.class));
    }

    private Set<UUID> buildPIEventCache() {
        final UUID cpHearingEventId_1 = randomUUID();
        final UUID cpHearingEventId_2 = UUID.fromString("abdaeb88-8952-4c07-99c4-d27c39d4e63a");
        final Set<UUID> hearingEventIds = new HashSet();
        hearingEventIds.add(cpHearingEventId_1);
        hearingEventIds.add(cpHearingEventId_2);
        return hearingEventIds;
    }

    @Test
    public void shouldFindHearingsForToday(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get.hearings-for-today", createObjectBuilder()
                .add("userId", UUID.randomUUID().toString())
                .build());
        when(hearingQueryView.findHearingsForToday(query)).thenReturn(mockGetHearingsEnvelope);
        hearingQueryApi.findHearingsForToday(query);

        verify(hearingQueryView, times(1)).findHearingsForToday(query);
    }

    @Test
    public void shouldGetHearingEventDefinitionsVersionTwo(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-definitions", createObjectBuilder()
                .add("userId", UUID.randomUUID().toString())
                .build());
        when(hearingEventQueryView.getHearingEventDefinitions(query)).thenReturn(mockJsonObjectEnvelope);
        hearingQueryApi.getHearingEventDefinitionsVersionTwo(query);

        verify(hearingEventQueryView, times(1)).getHearingEventDefinitions(query);
    }

    @Test
    public void shouldGetHearingEventDefinition(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-definition", createObjectBuilder()
                .add("hearingEventDefinitionId", UUID.randomUUID().toString())
                .build());
        when(hearingEventQueryView.getHearingEventDefinition(query)).thenReturn(mockJsonObjectEnvelope);
        hearingQueryApi.getHearingEventDefinition(query);

        verify(hearingEventQueryView, times(1)).getHearingEventDefinition(query);
    }

    @Test
    public void shouldGetHearingEventLog(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-log", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("date", LocalDate.now().toString())
                .build());
        when(hearingEventQueryView.getHearingEventLog(query)).thenReturn(mockJsonObjectEnvelope);
        hearingQueryApi.getHearingEventLog(query);

        verify(hearingEventQueryView, times(1)).getHearingEventLog(query);
    }

    @Test
    public void shouldGetResults(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-results", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("hearingDay", "3")
                .build());
        when(hearingQueryView.getResults(query)).thenReturn(mockTargetListResponseEnvelope);
        hearingQueryApi.getResults(query);

        verify(hearingQueryView, times(1)).getResults(query);
    }

    @Test
    public void shouldFindNows(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get.nows", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("hearingDay", "3")
                .build());
        when(hearingQueryView.findNows(query)).thenReturn(mockNowListResponseEnvelope);
        hearingQueryApi.findNows(query);

        verify(hearingQueryView, times(1)).findNows(query);
    }

    @Test
    public void shouldGetActiveHearingsForCourtRoom(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-active-hearings-for-court-room", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("eventDate", LocalDate.now().toString())
                .build());
        when(hearingEventQueryView.getActiveHearingsForCourtRoom(query)).thenReturn(mockJsonObjectEnvelope);
        hearingQueryApi.getActiveHearingsForCourtRoom(query);

        verify(hearingEventQueryView, times(1)).getActiveHearingsForCourtRoom(query);
    }

    @Test
    public void shouldGetCrackedIneffectiveTrialReason(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-cracked-ineffective-reason", createObjectBuilder()
                .add("trialTypeId", UUID.randomUUID().toString())
                .build());
        when(referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes()).thenReturn(crackedIneffectiveVacatedTrialTypes);
        when(hearingQueryView.getCrackedIneffectiveTrialReason(query, crackedIneffectiveVacatedTrialTypes)).thenReturn(mockCrackedIneffectiveTrialEnvelope);
        hearingQueryApi.getCrackedIneffectiveTrialReason(query);

        verify(referenceDataService, times(1)).listAllCrackedIneffectiveVacatedTrialTypes();
        verify(hearingQueryView, times(1)).getCrackedIneffectiveTrialReason(query,crackedIneffectiveVacatedTrialTypes);
    }

    @Test
    public void shouldGetDraftResult(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-draft-result", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .build());
        when(hearingQueryView.getDraftResult(query)).thenReturn(mockTargetListResponseEnvelope);
        hearingQueryApi.getDraftResult(query);

        verify(hearingQueryView, times(1)).getDraftResult(query);
    }

    @Test
    public void shouldGetDraftResultV2(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-draft-result-v2", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("hearingDay", "3")
                .build());
        when(hearingQueryView.getDraftResultV2(query)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.getDraftResultV2(query);

        verify(hearingQueryView, times(1)).getDraftResultV2(query);
    }

    @Test
    public void shouldGetShareResultV2(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-share-result-v2", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("hearingDay", "3")
                .build());
        when(hearingQueryView.getShareResultsV2(query)).thenReturn(mockGetShareResultsV2ResponseEnvelope);
        hearingQueryApi.getShareResultV2(query);

        verify(hearingQueryView, times(1)).getShareResultsV2(query);
    }

    @Test
    public void shouldSearchByMaterialId(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.query.search-by-material-id", createObjectBuilder()
                .add("q", "query_test")
                .build());
        when(hearingQueryView.searchByMaterialId(query)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.searchByMaterialId(query);

        verify(hearingQueryView, times(1)).searchByMaterialId(query);
    }

    @Test
    public void shouldRetrieveSubscriptions(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.retrieve-subscriptions", createObjectBuilder()
                .add("referenceDate", "referenceDate")
                .add("nowTypeId", "nowTypeId")
                .build());
        when(hearingQueryView.retrieveSubscriptions(query)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.retrieveSubscriptions(query);

        verify(hearingQueryView, times(1)).retrieveSubscriptions(query);
    }

    @Test
    public void shouldPublishCourtListStatus(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.court.list.publish.status", createObjectBuilder()
                .add("courtCentreId", "courtCentreId")
                .build());
        when(hearingQueryView.getCourtListPublishStatus(query)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.publishCourtListStatus(query);

        verify(hearingQueryView, times(1)).getCourtListPublishStatus(query);
    }

    @Test
    public void shouldGetHeringsByCourtCentre(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.latest-hearings-by-court-centres", createObjectBuilder()
                .add("courtCentreIds", "courtCentreIds")
                .add("dateOfHearing", "dateOfHearing")
                .build());
        when(xhibitEventMapperCache.getCppHearingEventIds()).thenReturn(mockSetUUIDEnvelope);
        when(hearingQueryView.getLatestHearingsByCourtCentres(query, mockSetUUIDEnvelope)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.getHeringsByCourtCentre(query);

        verify(hearingQueryView, times(1)).getLatestHearingsByCourtCentres(query, mockSetUUIDEnvelope);
    }

    @Test
    public void shouldGetHearingsForCourtCentreForDate(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.hearings-court-centres-for-date", createObjectBuilder()
                .add("courtCentreIds", "courtCentreIds")
                .add("dateOfHearing", "dateOfHearing")
                .build());
        when(xhibitEventMapperCache.getCppHearingEventIds()).thenReturn(mockSetUUIDEnvelope);
        when(hearingQueryView.getHearingsForCourtCentresForDate(query, mockSetUUIDEnvelope)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.getHearingsForCourtCentreForDate(query);

        verify(hearingQueryView, times(1)).getHearingsForCourtCentresForDate(query, mockSetUUIDEnvelope);
    }

    @Test
    public void shouldGetHearingDefendantInfo(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.defendant.info", createObjectBuilder()
                .add("courtCentreIds", "courtCentreIds")
                .add("courtRoomIds", "courtRoomIds")
                .add("dateOfHearing", "dateOfHearing")
                .build());
        when(hearingQueryView.getDefendantInfoFromCourtHouseId(query)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.getHearingDefendantInfo(query);

        verify(hearingQueryView, times(1)).getDefendantInfoFromCourtHouseId(query);
    }

    @Test
    public void shouldGetReusableInfo(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.query.reusable-info", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("orderedDate", "orderedDate")
                .build());
        Map<String, String> map = Stream.of(new String[][] {
                { "c1","country1" },
                { "c2","country2" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        when(referenceDataService.getCountryCodesMap()).thenReturn(map);
        when(referenceDataService.getCacheableResultPrompts(getString(query.payloadAsJsonObject(), "orderedDate"))).thenReturn(prompts);
        when(hearingQueryView.getReusableInformation(query, prompts, map)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.getReusableInfo(query);

        verify(referenceDataService, times(1)).getCountryCodesMap();
        verify(referenceDataService, times(1)).getCacheableResultPrompts(getString(query.payloadAsJsonObject(), "orderedDate"));
        verify(hearingQueryView, times(1)).getReusableInformation(query, prompts, map);
    }

    @Test
    public void shouldRetrieveCustodyTimeLimit(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.custody-time-limit", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("offenceId", UUID.randomUUID().toString())
                .add("hearingDay", UUID.randomUUID().toString())
                .add("bailStatusCode", "bailStatusCode")
                .build());
        when(hearingQueryView.retrieveCustodyTimeLimit(query)).thenReturn(mockJsonEnvelope);
        hearingQueryApi.retrieveCustodyTimeLimit(query);

        verify(hearingQueryView, times(1)).retrieveCustodyTimeLimit(query);
    }

    @Test
    public void shouldGetSessionTime(){
        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.query.session-time", createObjectBuilder()
                .add("hearingId", UUID.randomUUID().toString())
                .add("offenceId", UUID.randomUUID().toString())
                .add("hearingDay", UUID.randomUUID().toString())
                .add("bailStatusCode", "bailStatusCode")
                .build());
        when(sessionTimeQueryView.getSessionTime(mockEnvelopePayloadTypeConverter.convert(query, JsonObject.class))).thenReturn(mockSessionTimeResponse);
        hearingQueryApi.sessionTime(query);

        verify(sessionTimeQueryView, times(1)).getSessionTime(mockEnvelopePayloadTypeConverter.convert(query, JsonObject.class));
    }

    @Test
    public void shouldInitPIEventMapperCacheAndReturnCppHearingEventIds(){
        Set<UUID> set =  piEventMapperCache1.getCppHearingEventIds();
        assertThat(set.size(),is(32));
    }
}
