package uk.gov.moj.cpp.hearing.query.view;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.ApprovalType.CHANGE;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.CourtRoom;
import uk.gov.moj.cpp.hearing.domain.DefendantDetail;
import uk.gov.moj.cpp.hearing.domain.DefendantInfoQueryResult;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationMainConverter;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ReusableInfoService;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.CTLExpiryDateCalculatorService;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatusResult;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.test.FileUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.persistence.NoResultException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingQueryViewTest {

    private static final UUID COURT_CENTRE_ID = randomUUID();
    private static final UUID HEARING_ID = randomUUID();
    private static final String HEARING_DAY = "2021-03-01";
    private static final LocalDate HEARING_DAY_LOCAL_DATE = LocalDate.parse(HEARING_DAY);
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_COURTCENTRE_ID = "courtCentreId";
    private static final String FIELD_COURTROOM_IDS = "courtRoomIds";
    private static final String FIELD_HEARING_DATE = "hearingDate";
    private static final String COURT_CENTRE_QUERY_PARAMETER = "courtCentreId";
    private static final String COURT_CENTRE_IDS_QUERY_PARAMETER = "courtCentreIds";
    private static final String LAST_MODIFIED_TIME = "dateOfHearing";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_DAY = "hearingDay";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_CUSTODY_TIME_LIMIT = "custodyTimeLimit";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();
    @Spy
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(objectMapper);
    @Spy
    private Enveloper enveloper = createEnveloper();
    @Mock
    private CourtListRepository courtListRepository;
    @Mock
    private DefendantRepository defendantRepository;
    @Mock
    private HearingService hearingService;
    @Mock
    private ReusableInformationMainConverter reusableInformationMainConverter;
    @Mock
    private ReusableInfoService reusableInfoService;

    @Mock
    private List<UUID> prosecutionCasesIdsWithAccess;

    @Mock
    private CTLExpiryDateCalculatorService ctlExpiryDateCalculatorService;

    @InjectMocks
    private HearingQueryView target;

    @Test
    public void shouldReturnCorrectPublishCourtListStatus() {

        when(courtListRepository.courtListPublishStatuses(COURT_CENTRE_ID))
                .thenReturn(publishCourtListStatuses());

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.court.list.publish.status"),
                createObjectBuilder()
                        .add(COURT_CENTRE_QUERY_PARAMETER, COURT_CENTRE_ID.toString())
                        .build());

        final JsonEnvelope results = target.getCourtListPublishStatus(query);

        verify(courtListRepository).courtListPublishStatuses(COURT_CENTRE_ID);
        assertThat(results, is(jsonEnvelope(withMetadataEnvelopedFrom(query).withName("hearing.court.list.publish.status"), payloadIsJson(
                allOf(
                        withJsonPath("$.publishCourtListStatus.publishStatus", equalTo(EXPORT_SUCCESSFUL.name()))
                )))));

    }

    @Test
    public void shouldGetLatestHearingsByCourtCentres() {
        final String testPageName = "testPageName";

        final String courtCentreId1 = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String courtCentreId2 = "c0a03dfd-f6f2-4590-a026-17f1cf5268e1";

        final String courtCentreIdStr = courtCentreId1 + "," + courtCentreId2;

        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(fromString(courtCentreId1));
        courtCentreIds.add(fromString(courtCentreId2));

        final Optional<CurrentCourtStatus> currentCourtStatus = of(currentCourtStatus()
                .withPageName(testPageName)
                .build());

        final LocalDate now = LocalDate.now();
        Set<UUID> hearingEventIds = new HashSet<>();
        when(hearingService.getHearingsForWebPage(courtCentreIds, now, hearingEventIds)).thenReturn(currentCourtStatus);

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.latest-hearings-by-court-centres"),
                createObjectBuilder()
                        .add(COURT_CENTRE_IDS_QUERY_PARAMETER, courtCentreIdStr)
                        .add(LAST_MODIFIED_TIME, now.toString())
                        .build());


        final JsonEnvelope results = target.getLatestHearingsByCourtCentres(query, new HashSet<>());

        verify(hearingService).getHearingsForWebPage(courtCentreIds, now, hearingEventIds);
        assertThat(results.metadata().name(), is("hearing.get-latest-hearings-by-court-centres"));
        assertThat(results.payloadAsJsonObject().getString("pageName"), is(testPageName));
    }

    @Test
    public void shouldGetLatestHearingApprovalRequests() {
        final UUID hearingId = randomUUID();
        final UUID userId = randomUUID();
        final ZonedDateTime requestApprovalTime = ZonedDateTime.now();

        final uk.gov.justice.core.courts.Hearing hearing = hearing(hearingId, userId, requestApprovalTime);
        final HearingDetailsResponse hearingDetailsResponse = new HearingDetailsResponse(hearing, HearingState.INITIALISED, randomUUID());
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes1 = getCrackedIneffectiveVacatedTrialTypes();

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.get-hearing"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .build());

        when(hearingService.getHearingDetailsResponseById(hearingId, crackedIneffectiveVacatedTrialTypes1, prosecutionCasesIdsWithAccess, false)).thenReturn(hearingDetailsResponse);

        final Envelope<HearingDetailsResponse> hearingEnvelope = target.findHearing(query, crackedIneffectiveVacatedTrialTypes1, prosecutionCasesIdsWithAccess, false);
        final uk.gov.justice.core.courts.Hearing actualHearing = hearingEnvelope.payload().getHearing();

        verify(hearingService).getHearingDetailsResponseById(hearingId, crackedIneffectiveVacatedTrialTypes1, prosecutionCasesIdsWithAccess, false);
        assertThat(hearingEnvelope.metadata().name(), is("hearing.get-hearing"));

        assertThat(actualHearing.getId(), is(hearingId));
        final List<uk.gov.justice.core.courts.ApprovalRequest> approvalsRequested = actualHearing.getApprovalsRequested();
        final uk.gov.justice.core.courts.ApprovalRequest approvalRequested = approvalsRequested.get(0);
        assertThat(approvalRequested.getHearingId(), is(hearingId));
        assertThat(approvalRequested.getUserId(), is(userId));
        assertThat(approvalRequested.getRequestApprovalTime(), is(requestApprovalTime));
    }

    private uk.gov.justice.core.courts.Hearing hearing(UUID hearingId, UUID userId, ZonedDateTime requestApprovalTime) {
        final List<uk.gov.justice.core.courts.ApprovalRequest> approvalsRequested = new ArrayList();
        final uk.gov.justice.core.courts.ApprovalRequest approvalRequested = new uk.gov.justice.core.courts.ApprovalRequest(CHANGE, hearingId, requestApprovalTime, userId);
        approvalsRequested.add(approvalRequested);
        final uk.gov.justice.core.courts.Hearing hearing = new uk.gov.justice.core.courts.Hearing.Builder().withId(hearingId).withApprovalsRequested(approvalsRequested).build();
        hearing.setApprovalsRequested(approvalsRequested);
        return hearing;
    }

    private CrackedIneffectiveVacatedTrialTypes getCrackedIneffectiveVacatedTrialTypes() {
        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = new CrackedIneffectiveVacatedTrialType(randomUUID(), "", "", "", LocalDate.now());

        final List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = new ArrayList();
        crackedIneffectiveVacatedTrialTypes.add(crackedIneffectiveVacatedTrialType);

        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes1 = new CrackedIneffectiveVacatedTrialTypes();
        crackedIneffectiveVacatedTrialTypes1.setCrackedIneffectiveVacatedTrialTypes(crackedIneffectiveVacatedTrialTypes);
        return crackedIneffectiveVacatedTrialTypes1;
    }

    @Test
    public void shouldReturnEmptyResult() {
        final String courtCentreId1 = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String courtCentreId2 = "c0a03dfd-f6f2-4590-a026-17f1cf5268e1";

        final String courtCentreIdStr = courtCentreId1 + "," + courtCentreId2;
        final Optional<CurrentCourtStatus> currentCourtStatus = empty();

        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(fromString(courtCentreId1));
        courtCentreIds.add(fromString(courtCentreId2));


        final LocalDate now = LocalDate.now();
        Set<UUID> hearingEventIds = new HashSet<>();
        when(hearingService.getHearingsForWebPage(courtCentreIds, now, hearingEventIds)).thenReturn(currentCourtStatus);

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.get-latest-hearings-by-court-centres"),
                createObjectBuilder()
                        .add(COURT_CENTRE_IDS_QUERY_PARAMETER, courtCentreIdStr)
                        .add(LAST_MODIFIED_TIME, now.toString())
                        .build());


        final JsonEnvelope results = target.getLatestHearingsByCourtCentres(query, new HashSet<>());

        verify(hearingService).getHearingsForWebPage(courtCentreIds, now, hearingEventIds);
        assertThat(results.metadata().name(), is("hearing.get-latest-hearings-by-court-centres"));
        assertTrue(results.payloadAsJsonObject().isEmpty());
    }

    @Test
    public void should_send_payload_when_defendant_exists() {
        final Optional<UUID> anExistingDefendantId = Optional.of(randomUUID());

        when(defendantRepository.getDefendantDetailsForSearching(anExistingDefendantId.get())).thenReturn(createDefendantSearch());
        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.defendant.outstanding-fines"),
                createObjectBuilder()
                        .add(FIELD_DEFENDANT_ID, anExistingDefendantId.get().toString())
                        .build());
        final JsonEnvelope outstandingFromDefendantIdEnvelope = target.getOutstandingFromDefendantId(query);

        assertThat(outstandingFromDefendantIdEnvelope.metadata().name(), is("hearing.defendant.outstanding-fines"));

        final DefendantSearch actualDefendantSearch = jsonObjectToObjectConverter.convert(outstandingFromDefendantIdEnvelope.payloadAsJsonObject(), DefendantSearch.class);
        verify(defendantRepository).getDefendantDetailsForSearching(anExistingDefendantId.get());
        assertThat(actualDefendantSearch.getForename(), is("Tony"));
        assertThat(actualDefendantSearch.getSurname(), is("Stark"));
        assertThat(actualDefendantSearch.getNationalInsuranceNumber(), is("12345"));
        assertThat(actualDefendantSearch.getDateOfBirth(), is(LocalDate.of(1985, 6, 1)));
    }


    @Test
    public void should_send_an_empty_payload_when_defendant_does_not_exists() {
        final Optional<UUID> unknownDefendantId = Optional.of(randomUUID());

        when(defendantRepository.getDefendantDetailsForSearching(unknownDefendantId.get())).thenThrow(NoResultException.class);
        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.defendant.outstanding-fines"),
                createObjectBuilder()
                        .add(FIELD_DEFENDANT_ID, unknownDefendantId.get().toString())
                        .build());
        final JsonEnvelope outstandingFromDefendantIdEnvelope = target.getOutstandingFromDefendantId(query);

        verify(defendantRepository).getDefendantDetailsForSearching(unknownDefendantId.get());
        assertThat(outstandingFromDefendantIdEnvelope.metadata().name(), is("hearing.defendant.outstanding-fines"));
        assertTrue(outstandingFromDefendantIdEnvelope.payloadAsJsonObject().isEmpty());
    }

    @Test
    public void should_send_an_empty_payload_when_no_result_from_courtroom() {

        final UUID courtCentreId = randomUUID();
        final List<UUID> courtRoomIds = asList(new UUID[]{randomUUID(), randomUUID()});
        final LocalDate hearingDate = LocalDate.now();

        when(hearingService.getHearingsByCourtRoomList(hearingDate, courtCentreId, courtRoomIds)).thenThrow(NoResultException.class);

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.defendant.info"),
                createObjectBuilder()
                        .add(FIELD_COURTCENTRE_ID, courtCentreId.toString())
                        .add(FIELD_COURTROOM_IDS, courtRoomIds.stream().map(e -> e.toString()).collect(Collectors.joining(",")))
                        .add(FIELD_HEARING_DATE, hearingDate.toString())
                        .build());

        final JsonEnvelope result = target.getDefendantInfoFromCourtHouseId(query);

        verify(hearingService).getHearingsByCourtRoomList(hearingDate, courtCentreId, courtRoomIds);
        assertThat(result.metadata().name(), is("hearing.defendant.info"));
        assertTrue(result.payloadAsJsonObject().isEmpty());
    }

    @Test
    public void should_send_payload_when_defendant_found_with_courtroom() {

        final LocalDate hearingDate = LocalDate.now();
        final UUID courtHouseId = randomUUID();
        final UUID roomId1 = randomUUID();
        final UUID roomId2 = randomUUID();

        when(hearingService.getHearingsByCourtRoomList(hearingDate, courtHouseId, asList(roomId1, roomId2))).thenReturn(createDefendantInfo());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.defendant.info"),
                createObjectBuilder()
                        .add(FIELD_COURTCENTRE_ID, courtHouseId.toString())
                        .add(FIELD_COURTROOM_IDS, roomId1 + "," + roomId2)
                        .add(FIELD_HEARING_DATE, hearingDate.toString())
                        .build());

        final JsonEnvelope result = target.getDefendantInfoFromCourtHouseId(query);
        verify(hearingService).getHearingsByCourtRoomList(hearingDate, courtHouseId, asList(roomId1, roomId2));
        assertThat(result.metadata().name(), is("hearing.defendant.info"));
        assertEquals(1, result.payloadAsJsonObject().getJsonArray("courtRooms").size());
        assertTrue(result.payloadAsJsonObject().getJsonArray("courtRooms").getJsonObject(0).getString("courtRoomName").equalsIgnoreCase("Room-1"));

    }

    @Test
    public void shouldNotGetNowsByNonExistingId() {
        when(hearingService.getHearingById(HEARING_ID)).thenReturn(Optional.empty());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.get-now"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID.toString())
                        .build());

        final Envelope<NowListResponse> nows = target.findNows(query);

        verify(hearingService).getHearingById(HEARING_ID);
        assertThat(nows.payload(), is((JsonObject) null));
        assertThat(nows.metadata().name(), is("hearing.get-nows"));
    }

    @Test
    public void shouldNotGetDraftResultByNonExistingId() {
        when(hearingService.getTargets(HEARING_ID)).thenReturn(new TargetListResponse());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.get-draft-result"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID.toString())
                        .build());

        final Envelope<TargetListResponse> draftResult = target.getDraftResult(query);

        verify(hearingService).getTargets(HEARING_ID);
        assertThat(draftResult.payload().getTargets(), Matchers.empty());
        assertThat(draftResult.metadata().name(), is("hearing.get-draft-result"));
    }

    @Test
    public void shouldNotGetDraftResultByNonExistingHearingDay() {
        when(hearingService.getTargetsByDate(HEARING_ID, HEARING_DAY)).thenReturn(new TargetListResponse());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.results"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID.toString())
                        .add(FIELD_HEARING_DAY, HEARING_DAY)
                        .build());

        final Envelope<TargetListResponse> draftResult = target.getResults(query);

        verify(hearingService).getTargetsByDate(HEARING_ID, HEARING_DAY);
        assertThat(draftResult.payload().getTargets(), Matchers.empty());
        assertThat(draftResult.metadata().name(), is("hearing.results"));
    }

    @Test
    public void shouldGetDraftResultByHearingIdAndHearingDay() {
        when(hearingService.getTargetsByDate(HEARING_ID, HEARING_DAY)).thenReturn(buildTargetListResponse());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.results"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID.toString())
                        .add(FIELD_HEARING_DAY, HEARING_DAY)
                        .build());

        final Envelope<TargetListResponse> draftResult = target.getResults(query);

        verify(hearingService).getTargetsByDate(HEARING_ID, HEARING_DAY);
        assertThat(draftResult.payload().getTargets(), Matchers.hasSize(1));
        assertThat(draftResult.metadata().name(), is("hearing.results"));
    }

    @Test
    public void shouldGetViewStoreReusableInfoOnly() {
        final UUID masterDefendantId = UUID.fromString("2e576a1b-2c62-476d-a556-4c24d6bbc1a2");
        final List<Prompt> resultPrompts = new ArrayList<>();
        final UUID hearingId = randomUUID();
        final Hearing hearing = Hearing.hearing().withProsecutionCases(asList(ProsecutionCase.prosecutionCase().withDefendants(asList(Defendant.defendant().withId(randomUUID()).withMasterDefendantId(masterDefendantId).build())).build())).build();
        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.query.reusable-info"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, String.valueOf(hearingId))
                        .build());
        JsonObject reusableInfo = null;
        try {
            JsonNode payload = objectMapper.readTree(FileUtil.getPayload("reusable-info-singledefendant.json"));
            reusableInfo = objectMapper.treeToValue(payload, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        when(reusableInfoService.getViewStoreReusableInformation(anyList(), anyList())).thenReturn(reusableInfo);
        when(hearingService.getHearingDomainById(hearingId)).thenReturn(Optional.of(hearing));

        final JsonEnvelope resultEnvelope = target.getReusableInformation(query, resultPrompts, emptyMap());

        assertThat(resultEnvelope.payloadAsJsonObject().getJsonArray("reusablePrompts").size(), is(2));
    }

    @Test
    public void shouldGetViewStoreAndCaseDetailReusableInfoCombined() {

        final UUID promptId = randomUUID();
        final List<Prompt> resultPrompts = prepareResultPromptsData(promptId);

        final UUID masterDefendantId = UUID.fromString("2e576a1b-2c62-476d-a556-4c24d6bbc1a2");
        final UUID hearingId = randomUUID();
        final Hearing hearing = Hearing.hearing().withId(hearingId).withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                .withDefendants(asList(Defendant.defendant().withId(randomUUID()).withMasterDefendantId(masterDefendantId).build())).build())).build();
        final JsonObject promptData = createObjectBuilder()
                .add("value", "Brent Borough Council")
                .add("masterDefendantId", masterDefendantId.toString())
                .add("promptRef", "designatedLocalAuthority")
                .build();
        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.query.reusable-info"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, String.valueOf(hearingId))
                        .build());
        final Map<Defendant, List<JsonObject>> caseDetailInfo = new HashMap<>();
        final Defendant defendant = Defendant.defendant().withId(randomUUID()).withMasterDefendantId(masterDefendantId).build();
        caseDetailInfo.put(defendant, asList(promptData));
        JsonObject reusableInfo = null;
        try {
            JsonNode payload = objectMapper.readTree(FileUtil.getPayload("reusable-info-singledefendant.json"));
            reusableInfo = objectMapper.treeToValue(payload, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        when(reusableInfoService.getCaseDetailReusableInformation(anyList(), anyList(), anyMap())).thenReturn(asList(promptData));
        when(reusableInfoService.getViewStoreReusableInformation(anyList(), eq(asList(promptData)))).thenReturn(reusableInfo);
        when(hearingService.getHearingDomainById(hearingId)).thenReturn(Optional.of(hearing));

        final JsonObject result = target.getReusableInformation(query, resultPrompts, emptyMap()).payloadAsJsonObject();
        JsonArray reusablePrompts = result.getJsonArray("reusablePrompts");
        JsonArray reusableResults = result.getJsonArray("reusableResults");

        assertThat(reusablePrompts.size(), is(2));
        assertThat(reusableResults.size(), is(1));
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenHearingNotFound() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();

        when(hearingService.getHearingById(hearingId)).thenThrow(new RuntimeException("Hearing not found for hearing id: " + hearingId));

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.custody-time-limit"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId.toString())
                        .add(FIELD_OFFENCE_ID, offenceId.toString())
                        .add(FIELD_HEARING_DAY, HEARING_DAY)
                        .build());

        exception.expect(RuntimeException.class);
        exception.expectMessage("Hearing not found for hearing id: " + hearingId);

        target.retrieveCustodyTimeLimit(query);

    }

    @Test
    public void shouldThrowRuntimeExceptionWhenOffenceNotFound() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(randomUUID(), hearingId));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendant = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing = new uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(singleton(prosecutionCase));

        when(hearingService.getHearingById(hearingId)).thenReturn(Optional.of(hearing));
        when(ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId)).thenReturn(false);

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.custody-time-limit"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId.toString())
                        .add(FIELD_OFFENCE_ID, offenceId.toString())
                        .add(FIELD_HEARING_DAY, HEARING_DAY)
                        .build());

        exception.expect(RuntimeException.class);
        exception.expectMessage("Offence not found for offence id: " + offenceId);

        target.retrieveCustodyTimeLimit(query);

    }

    @Test
    public void shouldReturnEmptyResponseWhenCalculationWasAvoided() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(randomUUID(), hearingId));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendant = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing = new uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(singleton(prosecutionCase));

        when(hearingService.getHearingById(hearingId)).thenReturn(Optional.of(hearing));
        when(ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId)).thenReturn(true);

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.custody-time-limit"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId.toString())
                        .add(FIELD_OFFENCE_ID, offenceId.toString())
                        .add(FIELD_HEARING_DAY, HEARING_DAY)
                        .build());

        final JsonEnvelope response = target.retrieveCustodyTimeLimit(query);

        verify(hearingService).getHearingById(hearingId);
        verify(ctlExpiryDateCalculatorService).avoidCalculation(hearing, offenceId);

        assertThat(response.metadata().name(), is("hearing.custody-time-limit"));
        assertThat(response.payloadAsJsonObject().isEmpty(), is(true));

    }

    @Test
    public void shouldReturnEmptyResponseWhenExpiryDateIsNotThere() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendant = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing = new uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(singleton(prosecutionCase));

        when(hearingService.getHearingById(hearingId)).thenReturn(Optional.of(hearing));
        when(ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId)).thenReturn(false);
        when(ctlExpiryDateCalculatorService.calculateCTLExpiryDate(offence, HEARING_DAY_LOCAL_DATE)).thenReturn(Optional.empty());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.custody-time-limit"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId.toString())
                        .add(FIELD_OFFENCE_ID, offenceId.toString())
                        .add(FIELD_HEARING_DAY, HEARING_DAY)
                        .build());

        final JsonEnvelope response = target.retrieveCustodyTimeLimit(query);

        verify(hearingService).getHearingById(hearingId);
        verify(ctlExpiryDateCalculatorService).avoidCalculation(hearing, offenceId);
        verify(ctlExpiryDateCalculatorService).calculateCTLExpiryDate(offence, HEARING_DAY_LOCAL_DATE);

        assertThat(response.metadata().name(), is("hearing.custody-time-limit"));
        assertThat(response.payloadAsJsonObject().isEmpty(), is(true));

    }

    @Test
    public void shouldReturnExpiryDateWhenExpiryDateIsAvailable() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final LocalDate expiryDate = LocalDate.now();

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendant = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setOffences(singleton(offence));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setDefendants(singleton(defendant));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing = new uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(singleton(prosecutionCase));

        when(hearingService.getHearingById(hearingId)).thenReturn(Optional.of(hearing));
        when(ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId)).thenReturn(false);
        when(ctlExpiryDateCalculatorService.calculateCTLExpiryDate(offence, HEARING_DAY_LOCAL_DATE)).thenReturn(Optional.of(expiryDate));

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.custody-time-limit"),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId.toString())
                        .add(FIELD_OFFENCE_ID, offenceId.toString())
                        .add(FIELD_HEARING_DAY, HEARING_DAY)
                        .build());

        final JsonEnvelope response = target.retrieveCustodyTimeLimit(query);

        verify(hearingService).getHearingById(hearingId);
        verify(ctlExpiryDateCalculatorService).avoidCalculation(hearing, offenceId);
        verify(ctlExpiryDateCalculatorService).calculateCTLExpiryDate(offence, HEARING_DAY_LOCAL_DATE);

        assertThat(response.metadata().name(), is("hearing.custody-time-limit"));
        assertThat(response.payloadAsJsonObject().getString(FIELD_CUSTODY_TIME_LIMIT), is(expiryDate.toString()));

    }

    private List<Prompt> prepareResultPromptsData(final UUID promptId) {
        final Prompt prompt1 = new Prompt();
        prompt1.setId(promptId);
        prompt1.setCacheDataPath("CacheDataPath");
        prompt1.setCacheable(1);

        return asList(prompt1);
    }

    private DefendantSearch createDefendantSearch() {
        final DefendantSearch defendantSearch = new DefendantSearch();
        defendantSearch.setSurname("Stark");
        defendantSearch.setNationalInsuranceNumber("12345");
        defendantSearch.setForename("Tony");
        defendantSearch.setDateOfBirth(LocalDate.of(1985, 6, 1));

        return defendantSearch;
    }

    private DefendantInfoQueryResult createDefendantInfo() {
        final DefendantInfoQueryResult defendantInfoQueryResult = new DefendantInfoQueryResult();
        defendantInfoQueryResult.getCourtRooms().add(
                CourtRoom.courtRoom().withDefendantDetails(
                        asList(
                                DefendantDetail.defendantDetail().withDefendantId(randomUUID()).withDateOfBirth("1980-06-25 00:00:00").withFirstName("Mr").withLastName("Brown").build(),
                                DefendantDetail.defendantDetail().withDefendantId(randomUUID()).withFirstName("Mrs").withLastName("Brown").withNationalInsuranceNumber("AB123456Z").build(),
                                DefendantDetail.defendantDetail().withDefendantId(randomUUID()).withLegalEntityOrganizationName("ACME").build()
                        )
                )
                        .withCourtRoomName("Room-1")
                        .build()
        );

        return defendantInfoQueryResult;
    }

    private Optional<CourtListPublishStatusResult> publishCourtListStatuses() {
        final UUID courtCentreId = randomUUID();
        final CourtListPublishStatusResult publishCourtListStatus = new CourtListPublishStatusResult(courtCentreId, now(), EXPORT_SUCCESSFUL);
        return of(publishCourtListStatus);
    }

    private TargetListResponse buildTargetListResponse() {

        return TargetListResponse.builder().
                withTargets(Arrays.asList(Target.target()
                        .withHearingId(HEARING_ID)
                        .withHearingDay(LocalDate.parse(HEARING_DAY))
                        .withTargetId(randomUUID())
                        .build())).build();
    }
}
